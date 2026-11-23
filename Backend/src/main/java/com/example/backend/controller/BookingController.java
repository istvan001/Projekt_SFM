package com.example.backend.controller;

import com.example.backend.model.Booking;
import com.example.backend.service.AuthService;
import com.example.backend.service.BookingService;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.sql.SQLException;

/*
 * BookingController.java
 *
 * Ez az osztály a jegyfoglalások API-műveleteit kezeli.
 * Fogadja az új foglalásokat, ellenőrzi a felhasználót,
 * és lehetővé teszi a foglalások lemondását.
 */

@RestController
@RequestMapping("/api/foglalasok")
public class BookingController {

    // A foglalások kezeléséért felelős szolgáltatás
    private final BookingService bookingService;

    // A bejelentkezés és a token ellenőrzése
    private final AuthService authService;

    // A szükséges szolgáltatások átadása
    public BookingController(
            BookingService bookingService,
            AuthService authService) {

        this.bookingService = bookingService;
        this.authService = authService;
    }

    // Az új foglalás során érkező adatok
    public record BookingRequest(
            int jaratId,
            String utazasDatuma,
            String jegyTipus,
            int utasokSzama
    ) {}

    // Új foglalás létrehozása
    // POST /api/foglalasok

    @PostMapping
    public Booking foglalasLetrehozasa(
            @RequestHeader(value = "Authorization", required = false)
            String token,
            @RequestBody BookingRequest request
    ) throws SQLException {

        // A bejelentkezett felhasználó azonosítása a token alapján
        int felhasznaloId =
                authService.felhasznaloIdLekerdezese(token);

        try {

            // A foglalás adatainak átadása a BookingService részére
            return bookingService.foglalasLetrehozasa(
                    felhasznaloId,
                    request.jaratId(),
                    request.utazasDatuma(),
                    request.jegyTipus(),
                    request.utasokSzama()
            );

        } catch (IllegalArgumentException e) {

            // Hibás foglalási adatok esetén 400-as HTTP-válasz
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    e.getMessage()
            );
        }
    }

    // Foglalás lemondása
    // DELETE /api/foglalasok/{id}

    @DeleteMapping("/{id}")
    public void lemondas(
            @RequestHeader(value = "Authorization", required = false)
            String token,
            @PathVariable int id
    ) throws SQLException {

        // A bejelentkezett felhasználó azonosítása
        int felhasznaloId =
                authService.felhasznaloIdLekerdezese(token);

        // Csak a saját, aktív foglalását mondhatja le
        boolean sikeres = bookingService.lemondas(
                id,
                felhasznaloId
        );

        if (!sikeres) {

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "A foglalás nem található vagy már lemondták!"
            );
        }
    }
}
