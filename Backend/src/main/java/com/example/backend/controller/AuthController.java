package com.example.backend.controller;

import com.example.backend.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.sql.SQLException;

/*
 * AuthController.java
 *
 * Ez az osztály a felhasználók regisztrációjáért és
 * bejelentkezéséért felel a backendben.
 * Fogadja a frontend HTTP-kéréseit, majd továbbítja
 * az adatokat az AuthService felé.
 * A bejelentkezés eredményét és az esetleges hibákat
 * visszaküldi a frontendnek.
 */

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    // A regisztrációt és bejelentkezést kezelő szolgáltatás
    private final AuthService authService;

    // Az AuthService példányát a Spring automatikusan átadja
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // A regisztráció során érkező adatok
    public record RegisterRequest(
            String nev, String email, String jelszo) {}

    // A bejelentkezés során érkező adatok
    public record LoginRequest(String nev, String jelszo) {}

    // Új felhasználó regisztrálása a POST /api/auth/register végponton
    @PostMapping("/register")
    public void register(@RequestBody RegisterRequest request)
            throws SQLException {

        try {
            // A regisztrációs adatokat átadjuk az AuthService-nek
            authService.register(
                    request.nev(), request.email(), request.jelszo());

        } catch (IllegalArgumentException e) {

            // Hibás adatok esetén 400-as HTTP-hibát küldünk
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, e.getMessage());

        } catch (SQLException e) {

            // Ha az e-mail-cím már létezik, 409-es hibát küldünk
            if (e.getErrorCode() == 19) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Ez az e-mail-cím már regisztrálva van!");
            }

            // Egyéb adatbázishibát továbbítunk
            throw e;
        }
    }

    // Bejelentkezés a POST /api/auth/login végponton
    @PostMapping("/login")
    public AuthService.LoginResult login(
            @RequestBody LoginRequest request) throws SQLException {

        // Ellenőrizzük a bejelentkezést az AuthService segítségével,
        // majd visszaadjuk a felhasználó adatait és a belépési tokent
        return authService.login(request.nev(), request.jelszo());
    }
}
