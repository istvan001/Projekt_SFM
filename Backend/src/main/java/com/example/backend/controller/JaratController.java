package com.example.backend.controller;

import com.example.backend.model.Jarat;
import com.example.backend.service.AuthService;
import com.example.backend.service.JaratService;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.sql.SQLException;
import java.util.List;

/*
 * JaratController.java
 *
 * Ez az osztály a járatok kezeléséhez tartozó API-kérésekért felel.
 * Lehetővé teszi a járatok lekérését, hozzáadását és törlését.
 * A kéréseket a JaratService felé továbbítja, az adminisztrátori
 * műveleteknél pedig az AuthService segítségével ellenőrzi
 * a felhasználó jogosultságát.
 */

@RestController
@RequestMapping("/api/jaratok")
public class JaratController {

    // A járatok kezelését és a jogosultságok ellenőrzését végző szolgáltatások
    private final JaratService jaratService;
    private final AuthService authService;

    // A Spring automatikusan átadja a szükséges szolgáltatásokat
    public JaratController(
            JaratService jaratService,
            AuthService authService) {

        this.jaratService = jaratService;
        this.authService = authService;
    }

    // Az új járat hozzáadásakor érkező adatok
    public record JaratRequest(
            String indulas,
            String erkezes,
            String indulasIdo,
            String erkezesIdo,
            int ar) {}

    // Az összes járat lekérése a GET /api/jaratok végponton
    @GetMapping
    public List<Jarat> osszesJarat() throws SQLException {

        // A járatokat a JaratService kéri le az adatbázisból
        return jaratService.osszesJarat();
    }

    // Új járat hozzáadása a POST /api/jaratok végponton
    @PostMapping
    public void hozzaadas(
            @RequestHeader(value = "Authorization", required = false)
            String token,
            @RequestBody JaratRequest request) throws SQLException {

        // Ellenőrizzük, hogy a felhasználó rendelkezik-e adminjogosultsággal
        authService.adminEllenorzes(token);

        try {
            // Az új járat adatait átadjuk a JaratService-nek
            jaratService.hozzaadas(
                    request.indulas(),
                    request.erkezes(),
                    request.indulasIdo(),
                    request.erkezesIdo(),
                    request.ar()
            );

        } catch (IllegalArgumentException e) {

            // Hibás járatadatok esetén 400-as HTTP-hibát küldünk
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    // Járat törlése a DELETE /api/jaratok/{id} végponton
    @DeleteMapping("/{id}")
    public void torles(
            @RequestHeader(value = "Authorization", required = false)
            String token,
            @PathVariable int id) throws SQLException {

        // Járatot csak adminisztrátor törölhet
        authService.adminEllenorzes(token);

        // A megadott azonosító alapján töröljük a járatot
        if (!jaratService.torles(id)) {

            // Ha a járat nem létezik, 404-es HTTP-hibát küldünk
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "A járat nem található!");
        }
    }

    // Tesztvégpont a backend működésének ellenőrzésére
    @GetMapping("/teszt")
    public String teszt() {

        return "A JegyGo backend sikeresen működik!";
    }
}
