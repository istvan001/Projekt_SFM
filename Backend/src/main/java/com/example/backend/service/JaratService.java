package com.example.backend.service;

import com.example.backend.database.Database;
import com.example.backend.model.Jarat;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/*
 * JaratService.java
 *
 * Ez az osztály a járatok adatbázisban történő kezeléséért felel.
 * Lehetővé teszi az összes járat lekérését, új járatok hozzáadását
 * és a meglévő járatok törlését.
 * Új járat hozzáadásakor ellenőrzi a megadott adatokat,
 * majd az SQLite-adatbázisban végrehajtja a szükséges műveleteket.
 * A JaratController ezen az osztályon keresztül kezeli a járatokat.
 */

@Service
public class JaratService {

    // Az összes járat lekérése az adatbázisból
    public List<Jarat> osszesJarat() throws SQLException {

        // Lista létrehozása a lekért járatok tárolására
        List<Jarat> jaratok = new ArrayList<>();

        // A járatok lekérése indulási idő szerinti sorrendben
        String sql = "SELECT * FROM jaratok ORDER BY indulas_ido";

        // Kapcsolódás az adatbázishoz és az SQL-lekérdezés végrehajtása
        try (Connection conn = Database.connect();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            // Végigmegyünk az adatbázisból lekért járatokon
            while (rs.next()) {

                // Minden adatbázisrekordból létrehozunk egy Jarat objektumot
                jaratok.add(new Jarat(
                        rs.getInt("id"),
                        rs.getString("indulas"),
                        rs.getString("erkezes"),
                        rs.getString("indulas_ido"),
                        rs.getString("erkezes_ido"),
                        rs.getInt("ar")
                ));
            }
        }

        // Visszaadjuk a járatokat tartalmazó listát
        return jaratok;
    }

    // Új járat hozzáadása az adatbázishoz
    public void hozzaadas(
            String indulas, String erkezes,
            String indulasIdo, String erkezesIdo, int ar)
            throws SQLException {

        // Ellenőrizzük az indulási és érkezési állomásokat
        // A két állomás nem lehet üres vagy azonos
        if (indulas == null || erkezes == null
                || indulas.isBlank() || erkezes.isBlank()
                || indulas.equalsIgnoreCase(erkezes)) {

            throw new IllegalArgumentException(
                    "Érvénytelen indulási vagy célállomás!");
        }

        // Ellenőrizzük az indulási és érkezési időpont formátumát
        try {
            LocalTime.parse(indulasIdo);
            LocalTime.parse(erkezesIdo);

        } catch (RuntimeException e) {

            // Hibás időpont esetén hibaüzenetet küldünk
            throw new IllegalArgumentException(
                    "Az időpont formátuma HH:mm legyen!");
        }

        // Ellenőrizzük, hogy a jegyár pozitív szám-e
        if (ar <= 0) {
            throw new IllegalArgumentException(
                    "A jegyárnak pozitívnak kell lennie!");
        }

        // SQL-utasítás az új járat adatbázisba mentéséhez
        String sql = """
                INSERT INTO jaratok
                (indulas, erkezes, indulas_ido, erkezes_ido, ar)
                VALUES (?, ?, ?, ?, ?)
                """;

        // Adatbázis-kapcsolat létrehozása és az SQL-utasítás előkészítése
        try (Connection conn = Database.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Az új járat adatainak beállítása
            stmt.setString(1, indulas.trim());
            stmt.setString(2, erkezes.trim());
            stmt.setString(3, indulasIdo);
            stmt.setString(4, erkezesIdo);
            stmt.setInt(5, ar);

            // Az új járat mentése az adatbázisba
            stmt.executeUpdate();
        }
    }

    // Járat törlése az adatbázisból azonosító alapján
    public boolean torles(int id) throws SQLException {

        // SQL-utasítás a megadott azonosítójú járat törlésére
        try (Connection conn = Database.connect();
             PreparedStatement stmt = conn.prepareStatement(
                     "DELETE FROM jaratok WHERE id = ?")) {

            // A törlendő járat azonosítójának megadása
            stmt.setInt(1, id);

            // Törlés végrehajtása
            // Igaz értéket ad vissza, ha sikerült legalább egy rekordot törölni
            return stmt.executeUpdate() > 0;
        }
    }
}
