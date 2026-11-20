package com.example.myapplication.controller;


import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import com.example.myapplication.service.ApiService;
/*
 * RegisterController.java
 *
 * Ez az osztály a JegyGo JavaFX alkalmazás
 * regisztrációs felületének működéséért felel.
 * Kezeli a felhasználónév, az e-mail-cím és a jelszó
 * megadását, valamint ellenőrzi a kötelező mezőket,
 * a jelszó hosszát és a két jelszó egyezését.
 * A regisztrációs adatokat az AuthService segítségével
 * továbbítja a backendnek, majd sikeres regisztráció
 * után visszairányítja a felhasználót a bejelentkezéshez.
 */

public class RegisterController {

    // A regisztrációs adatok megadására szolgáló mezők
    @FXML private TextField nevField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField passwordAgainField;

    // A regisztrációs gomb megnyomásakor lefutó metódus
    @FXML
    private void regisztracio() {

        // A felhasználó által megadott adatok kiolvasása
        String nev = nevField.getText().trim();
        String email = emailField.getText().trim();
        String jelszo = passwordField.getText();
        String jelszoUjra = passwordAgainField.getText();

        // Ellenőrizzük, hogy a két megadott jelszó megegyezik-e
        if (!jelszo.equals(jelszoUjra)) {
            uzenet("A két jelszó nem egyezik!");
            return;
        }

        // Ellenőrizzük, hogy minden kötelező mező ki van-e töltve
        if (nev.isBlank() || email.isBlank() || jelszo.isBlank()) {
            uzenet("Minden mező kitöltése kötelező!");
            return;
        }

        // Ellenőrizzük a jelszó minimális hosszát
        if (jelszo.length() < 8) {
            uzenet("A jelszó legalább 8 karakter legyen!");
            return;
        }

        try {

            // A regisztrációs adatokat elküldjük a backendnek
            // A backend ellenőrzi és elmenti az új felhasználót
            ApiService.register(nev, email, jelszo);

            // Sikeres regisztrációról tájékoztatjuk a felhasználót
            uzenet("Sikeres regisztráció!");

            // Visszalépünk a bejelentkezési felületre
            vissza();

        } catch (Exception e) {

            // Sikertelen regisztráció vagy kapcsolódási hiba kezelése
            e.printStackTrace();

            uzenet("Nem sikerült a regisztráció. Ellenőrizd az adatokat és a backend kapcsolatát.");
        }
    }

    // Visszalépés a bejelentkezési felületre
    @FXML
    private void vissza() {

        try {

            // A bejelentkezési felület FXML-fájljának betöltése
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(
                            "/com/example/myapplication/LoginView.fxml"
                    )
            );

            // A jelenlegi alkalmazásablak lekérése
            Stage stage = (Stage) nevField.getScene().getWindow();

            // A regisztrációs felület lecserélése a bejelentkezési felületre
            stage.setScene(new Scene(loader.load(), 900, 600));

        } catch (Exception e) {

            // Hiba esetén kiírjuk a részleteket a konzolra
            e.printStackTrace();
        }
    }

    // Tájékoztató üzenet megjelenítése felugró ablakban
    private void uzenet(String szoveg) {

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setContentText(szoveg);
        alert.showAndWait();
    }
}
