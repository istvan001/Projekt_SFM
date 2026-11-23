package com.example.backend.service;

import com.example.backend.database.Database;
import com.example.backend.model.Booking;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class BookingService {

    // A foglalások táblájának automatikus létrehozása
    private void tablaLetrehozasa(Connection conn) throws SQLException {

        // Külső kulcsok ellenőrzésének bekapcsolása
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON");

            String sql = """
                    CREATE TABLE IF NOT EXISTS foglalasok (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        felhasznalo_id INTEGER NOT NULL,
                        jarat_id INTEGER NOT NULL,
                        utazas_datuma TEXT NOT NULL,
                        jegy_tipus TEXT NOT NULL
                            CHECK (jegy_tipus IN ('TELJES_ARU', 'KEDVEZMENYES')),
                        utasok_szama INTEGER NOT NULL
                            CHECK (utasok_szama BETWEEN 1 AND 6),
                        vegosszeg INTEGER NOT NULL,
                        allapot TEXT NOT NULL DEFAULT 'AKTIV'
                            CHECK (allapot IN ('AKTIV', 'LEMONDVA')),
                        letrehozva TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,

                        FOREIGN KEY (felhasznalo_id)
                            REFERENCES felhasznalok(id),

                        FOREIGN KEY (jarat_id)
                            REFERENCES jaratok(id)
                    )
                    """;

            stmt.execute(sql);
        }
    }

    // Új foglalás létrehozása
    public Booking foglalasLetrehozasa(
            int felhasznaloId,
            int jaratId,
            String utazasDatuma,
            String jegyTipus,
            int utasokSzama
    ) throws SQLException {

        // Felhasználói azonosító ellenőrzése
        if (felhasznaloId <= 0) {
            throw new IllegalArgumentException(
                    "Érvénytelen felhasználói azonosító!");
        }

        // Járatazonosító ellenőrzése
        if (jaratId <= 0) {
            throw new IllegalArgumentException(
                    "Érvénytelen járatazonosító!");
        }

        // Utasszám ellenőrzése
        if (utasokSzama < 1 || utasokSzama > 6) {
            throw new IllegalArgumentException(
                    "1 és 6 közötti utasszám adható meg!");
        }

        // Utazási dátum ellenőrzése
        LocalDate datum;

        try {
            datum = LocalDate.parse(utazasDatuma);
        } catch (DateTimeParseException | NullPointerException e) {
            throw new IllegalArgumentException(
                    "Érvénytelen utazási dátum!");
        }

        if (datum.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException(
                    "Múltbeli dátumra nem lehet foglalni!");
        }

        // Jegytípus ellenőrzése
        if (jegyTipus == null) {
            throw new IllegalArgumentException(
                    "Válassz jegytípust!");
        }

        String tipus = jegyTipus.trim().toUpperCase(Locale.ROOT);

        if (!tipus.equals("TELJES_ARU")
                && !tipus.equals("KEDVEZMENYES")) {

            throw new IllegalArgumentException(
                    "Érvénytelen jegytípus!");
        }

        try (Connection conn = Database.connect()) {

            tablaLetrehozasa(conn);

            // A járat alapárának lekérése az adatbázisból
            String jaratSql =
                    "SELECT ar FROM jaratok WHERE id = ?";

            int alapAr;

            try (PreparedStatement stmt =
                         conn.prepareStatement(jaratSql)) {

                stmt.setInt(1, jaratId);

                try (ResultSet rs = stmt.executeQuery()) {

                    if (!rs.next()) {
                        throw new IllegalArgumentException(
                                "A kiválasztott járat nem található!");
                    }

                    alapAr = rs.getInt("ar");
                }
            }

            // Ellenőrizzük az alapárat
            if (alapAr <= 0) {
                throw new IllegalArgumentException(
                        "Érvénytelen jegyár!");
            }

            // Végösszeg kiszámítása a Backendben
            long osszeg = (long) alapAr * utasokSzama;

            // A kedvezmény egyelőre tesztjellegű, 50%
            if (tipus.equals("KEDVEZMENYES")) {
                osszeg = Math.round(osszeg * 0.5);
            }

            if (osszeg > Integer.MAX_VALUE) {
                throw new IllegalArgumentException(
                        "A foglalás összege túl magas!");
            }

            int vegosszeg = (int) osszeg;

            // Foglalás mentése az adatbázisba
            String sql = """
                    INSERT INTO foglalasok
                    (felhasznalo_id, jarat_id, utazas_datuma,
                     jegy_tipus, utasok_szama, vegosszeg, allapot)
                    VALUES (?, ?, ?, ?, ?, ?, 'AKTIV')
                    """;

            int foglalasId;

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setInt(1, felhasznaloId);
                stmt.setInt(2, jaratId);
                stmt.setString(3, datum.toString());
                stmt.setString(4, tipus);
                stmt.setInt(5, utasokSzama);
                stmt.setInt(6, vegosszeg);

                stmt.executeUpdate();
            }

            // A frissen létrehozott foglalás azonosítójának lekérése
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(
                         "SELECT last_insert_rowid()")) {

                rs.next();
                foglalasId = rs.getInt(1);
            }

            // Visszaadjuk a létrehozott foglalást
            return new Booking(
                    foglalasId,
                    felhasznaloId,
                    jaratId,
                    datum.toString(),
                    tipus,
                    utasokSzama,
                    vegosszeg,
                    "AKTIV"
            );
        }
    }

    // Egy felhasználó saját foglalásainak lekérése
    public List<Booking> sajatFoglalasok(
            int felhasznaloId
    ) throws SQLException {

        List<Booking> foglalasok = new ArrayList<>();

        try (Connection conn = Database.connect()) {

            tablaLetrehozasa(conn);

            String sql = """
                    SELECT * FROM foglalasok
                    WHERE felhasznalo_id = ?
                    ORDER BY id DESC
                    """;

            try (PreparedStatement stmt =
                         conn.prepareStatement(sql)) {

                stmt.setInt(1, felhasznaloId);

                try (ResultSet rs = stmt.executeQuery()) {

                    while (rs.next()) {

                        foglalasok.add(new Booking(
                                rs.getInt("id"),
                                rs.getInt("felhasznalo_id"),
                                rs.getInt("jarat_id"),
                                rs.getString("utazas_datuma"),
                                rs.getString("jegy_tipus"),
                                rs.getInt("utasok_szama"),
                                rs.getInt("vegosszeg"),
                                rs.getString("allapot")
                        ));
                    }
                }
            }
        }

        return foglalasok;
    }

    // Foglalás lemondása
    public boolean lemondas(
            int foglalasId,
            int felhasznaloId
    ) throws SQLException {

        try (Connection conn = Database.connect()) {

            tablaLetrehozasa(conn);

            String sql = """
                    UPDATE foglalasok
                    SET allapot = 'LEMONDVA'
                    WHERE id = ?
                    AND felhasznalo_id = ?
                    AND allapot = 'AKTIV'
                    """;

            try (PreparedStatement stmt =
                         conn.prepareStatement(sql)) {

                stmt.setInt(1, foglalasId);
                stmt.setInt(2, felhasznaloId);

                // Igaz, ha sikerült a foglalás lemondása
                return stmt.executeUpdate() > 0;
            }
        }
    }
}
