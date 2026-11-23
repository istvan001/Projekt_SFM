package com.example.backend.service;

import com.example.backend.database.Database;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.sql.*;
import java.util.Base64;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/*
 * AuthService.java
 *
 * Ez az osztály a felhasználók regisztrációjáért,
 * bejelentkezéséért és jogosultságainak ellenőrzéséért felel.
 * Kezeli a jelszavak biztonságos tárolását, létrehozza
 * a bejelentkezési tokeneket, és ellenőrzi, hogy a
 * felhasználó rendelkezik-e adminisztrátori jogosultsággal.
 */

@Service
public class AuthService {

    // A jelszavak titkosítás helyetti biztonságos hash-elésére szolgál
    private final BCryptPasswordEncoder encoder =
            new BCryptPasswordEncoder();

    // Biztonságos véletlenszám-generátor a tokenek létrehozásához
    private final SecureRandom random = new SecureRandom();

    // A bejelentkezett felhasználók tokenjeit és azonosítóit tárolja
    private final Map<String, Integer> sessions =
            new ConcurrentHashMap<>();

    // A sikeres bejelentkezéskor visszaküldött adatok
    public record LoginResult(int userId, boolean admin, String token) {}

    // Új felhasználó regisztrálása
    public void register(String nev, String email, String jelszo)
            throws SQLException {

        // A felesleges szóközök eltávolítása és az e-mail normalizálása
        nev = nev.trim();
        email = email.trim().toLowerCase(Locale.ROOT);

        // Kötelező mezők ellenőrzése
        if (nev.isBlank() || email.isBlank() || jelszo.isBlank()) {
            throw new IllegalArgumentException(
                    "Minden mező kitöltése kötelező!");
        }

        // E-mail-cím formátumának ellenőrzése
        if (!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new IllegalArgumentException("Érvénytelen e-mail-cím!");
        }

        // Minimum 8 karakteres jelszó megkövetelése
        if (jelszo.length() < 8) {
            throw new IllegalArgumentException(
                    "A jelszó legalább 8 karakter legyen!");
        }

        // Az új felhasználó mentése alapértelmezett (nem admin) jogosultsággal
        String sql = """
                INSERT INTO felhasznalok
                (nev, email, jelszo, admin)
                VALUES (?, ?, ?, 0)
                """;

        try (Connection conn = Database.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, nev);
            stmt.setString(2, email);

            // A jelszót BCrypt-hash formájában tároljuk
            stmt.setString(3, encoder.encode(jelszo));

            stmt.executeUpdate();
        }
    }

    // Felhasználó bejelentkeztetése
    public LoginResult login(String nev, String jelszo)
            throws SQLException {

        // Felhasználó keresése név alapján, kis- és nagybetűktől függetlenül
        String sql = """
                SELECT id, jelszo, admin
                FROM felhasznalok
                WHERE nev = ? COLLATE NOCASE
                """;

        try (Connection conn = Database.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, nev.trim());

            try (ResultSet rs = stmt.executeQuery()) {

                // Ha nem létezik a felhasználó, elutasítjuk a bejelentkezést
                if (!rs.next()) {
                    throw new ResponseStatusException(
                            HttpStatus.UNAUTHORIZED,
                            "Hibás bejelentkezési adatok!");
                }

                // Felhasználói adatok kiolvasása
                int id = rs.getInt("id");
                boolean admin = rs.getInt("admin") == 1;
                String taroltJelszo = rs.getString("jelszo");

                // Megállapítjuk, hogy a jelszó régi, egyszerű szöveges formátumú-e
                boolean regiJelszo = taroltJelszo != null
                        && !taroltJelszo.startsWith("$2");

                // Ellenőrizzük a megadott jelszót
                boolean helyes = taroltJelszo != null
                        && (regiJelszo
                        ? taroltJelszo.equals(jelszo)
                        : encoder.matches(jelszo, taroltJelszo));

                if (!helyes) {
                    throw new ResponseStatusException(
                            HttpStatus.UNAUTHORIZED,
                            "Hibás bejelentkezési adatok!");
                }

                // A régi jelszót sikeres bejelentkezés után BCrypt-hashre cseréljük
                if (regiJelszo) {
                    try (PreparedStatement update = conn.prepareStatement(
                            "UPDATE felhasznalok SET jelszo = ? WHERE id = ?")) {

                        update.setString(1, encoder.encode(jelszo));
                        update.setInt(2, id);
                        update.executeUpdate();
                    }
                }

                // Véletlenszerű, 32 bájtos belépési token létrehozása
                byte[] bytes = new byte[32];
                random.nextBytes(bytes);

                String token = Base64.getUrlEncoder()
                        .withoutPadding()
                        .encodeToString(bytes);

                // A tokent összekapcsoljuk a felhasználó azonosítójával
                sessions.put(token, id);

                // Visszaadjuk a bejelentkezett felhasználó adatait
                return new LoginResult(id, admin, token);
            }
        }
    }

    // Adminisztrátori jogosultság ellenőrzése
    public void adminEllenorzes(String authorization)
            throws SQLException {

        // Ellenőrizzük, hogy érkezett-e megfelelő hitelesítési token
        if (authorization == null
                || !authorization.startsWith("Bearer ")) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Bejelentkezés szükséges!");
        }

        // A Bearer előtag eltávolítása után kinyerjük a tokent
        String token = authorization.substring(7);

        // Megkeressük a tokenhez tartozó felhasználót
        Integer userId = sessions.get(token);

        if (userId == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Érvénytelen munkamenet!");
        }

        // Az adatbázisban ellenőrizzük az adminjogosultságot
        try (Connection conn = Database.connect();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT admin FROM felhasznalok WHERE id = ?")) {

            stmt.setInt(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {

                // Csak adminisztrátor hajthat végre védett műveleteket
                if (!rs.next() || rs.getInt("admin") != 1) {
                    throw new ResponseStatusException(
                            HttpStatus.FORBIDDEN,
                            "Nincs admin jogosultság!");
                }
            }
        }
    }
    // A bejelentkezett felhasználó azonosítójának lekérése
    public int felhasznaloIdLekerdezese(String authorization) {

        // Ellenőrizzük, hogy van-e érvényes Authorization fejléc
        if (authorization == null
                || !authorization.startsWith("Bearer ")) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Bejelentkezés szükséges!"
            );
        }

        // A token kinyerése
        String token = authorization.substring(7);

        // A tokenhez tartozó felhasználó azonosítójának lekérése
        Integer userId = sessions.get(token);

        // Érvénytelen vagy lejárt munkamenet ellenőrzése
        if (userId == null) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Érvénytelen munkamenet!"
            );
        }

        return userId;
    }
}
