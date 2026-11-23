package com.example.backend.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/*
 * Database.java
 *
 * Ez az osztály a backend és az SQLite-adatbázis
 * közötti kapcsolat létrehozásáért felel.
 * Meghatározza az adatbázis elérési útját,
 * megnyitja az adatbázis-kapcsolatot, és beállítja
 * a zárolás esetén alkalmazott várakozási időt.
 */

public class Database {

    // Az SQLite-adatbázis elérési útjának meghatározása
    // Az útvonal rendszerbeállítással is megadható
    private static final String URL =
            "jdbc:sqlite:" + System.getProperty(
                    "jegygo.db.path",
                    "../jegyfoglalo.db"
            );

    // Kapcsolat létrehozása az SQLite-adatbázissal
    public static Connection connect() throws SQLException {

        // Megnyitjuk az adatbázis-kapcsolatot
        Connection conn = DriverManager.getConnection(URL);

        // Ha az adatbázis foglalt, legfeljebb 5 másodpercet várunk
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA busy_timeout = 5000");

        } catch (SQLException e) {

            // Hiba esetén lezárjuk a megnyitott kapcsolatot
            conn.close();
            throw e;
        }

        // Visszaadjuk a használatra kész adatbázis-kapcsolatot
        return conn;
    }
}
