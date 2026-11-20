package com.example.myapplication.controller;

import com.example.myapplication.service.ApiService;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;

/*
 * LoginController.java
 *
 * Ez az osztály a JegyGo JavaFX alkalmazás
 * bejelentkezési felületének működéséért felel.
 * Kezeli a felhasználónév és jelszó megadását,
 * a bejelentkezési adatok ellenőrzésének kezdeményezését,
 * valamint a felhasználó jogosultságának megfelelő
 * felület megnyitását.
 * Lehetővé teszi a regisztrációs felület megnyitását
 * és a jelszó megjelenítését vagy elrejtését is.
 */

public class LoginController {

    // A felhasználónév megadására szolgáló mező
    @FXML private TextField userField;

    // A jelszó rejtett megadására szolgáló mező
    @FXML private PasswordField passwordField;

    // A jelszó látható megjelenítésére szolgáló mező
    @FXML private TextField visiblePasswordField;

    // A jelszó láthatóságát váltó gomb
    @FXML private Button passwordToggleButton;

    // A bejelentkezési gomb megnyomásakor lefutó metódus
    @FXML
    private void bejelentkezes() {

        // A felhasználónév és jelszó kiolvasása
        String felhasznalo = userField.getText().trim();
        String jelszo = passwordField.getText();

        // Ellenőrizzük, hogy mindkét mező ki van-e töltve
        if (felhasznalo.isEmpty() || jelszo.isEmpty()) {
            uzenet("Add meg a felhasználónevedet és a jelszavadat!");
            return;
        }

        try {

            // A bejelentkezési adatokat elküldjük a backendnek,
            // ahol megtörténik a felhasználó ellenőrzése
            ApiService.login(felhasznalo, jelszo);
            // A backend válasza alapján lekérjük az adminjogosultságot
            boolean admin = ApiService.isAdmin();

            // Admin esetén az adminfelületet,
            // normál felhasználó esetén a járatkeresőt nyitjuk meg
            String fxml = admin
                    ? "/com/example/myapplication/AdminView.fxml"
                    : "/com/example/myapplication/View.fxml";

            // A megfelelő FXML-fájl megkeresése
            URL location = getClass().getResource(fxml);

            // Ellenőrizzük, hogy a felület fájlja megtalálható-e
            if (location == null) {
                uzenet("Nem található a felület: " + fxml);
                return;
            }

            // A kiválasztott felület betöltése
            FXMLLoader loader = new FXMLLoader(location);

            // A jelenlegi alkalmazásablak lekérése
            Stage stage = (Stage) userField.getScene().getWindow();

            // A bejelentkezési felület lecserélése
            stage.setScene(new Scene(loader.load(), 950, 650));

            // Az ablak címének beállítása a jogosultság alapján
            stage.setTitle(admin
                    ? "JegyGo - Adminisztráció"
                    : "JegyGo - Járatkereső");

        } catch (Exception e) {

            // Sikertelen bejelentkezés vagy kapcsolódási hiba kezelése
            e.printStackTrace();

            uzenet("Sikertelen bejelentkezés! Ellenőrizd az adatokat és a backend kapcsolatát.");
        }
    }

    // A regisztrációs felület megnyitása
    @FXML
    private void regisztracioMegnyitasa() {

        try {

            // A regisztrációs felület FXML-fájljának betöltése
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(
                            "/com/example/myapplication/RegisterView.fxml"
                    )
            );

            // A jelenlegi alkalmazásablak lekérése
            Stage stage = (Stage) userField.getScene().getWindow();

            // Átváltás a regisztrációs felületre
            stage.setScene(new Scene(loader.load(), 900, 600));
            stage.setTitle("JegyGo - Regisztráció");

        } catch (Exception e) {

            // Hiba esetén tájékoztatjuk a felhasználót
            e.printStackTrace();
            uzenet("Nem sikerült megnyitni a regisztrációt.");
        }
    }

    // A bejelentkezési felület betöltésekor automatikusan lefut
    @FXML
    public void initialize() {

        // A látható és rejtett jelszómező tartalmát összekapcsoljuk,
        // így mindkettő mindig ugyanazt a jelszót tartalmazza
        visiblePasswordField.textProperty()
                .bindBidirectional(passwordField.textProperty());
    }

    // A jelszó láthatóságának be- és kikapcsolása
    @FXML
    private void jelszoLathatosag() {

        // Megállapítjuk, hogy jelenleg látható-e a jelszó
        boolean lathato = visiblePasswordField.isVisible();

        // A látható jelszómező megjelenítése vagy elrejtése
        visiblePasswordField.setVisible(!lathato);
        visiblePasswordField.setManaged(!lathato);

        // A rejtett jelszómező megjelenítése vagy elrejtése
        passwordField.setVisible(lathato);
        passwordField.setManaged(lathato);

        // A gomb ikonjának megváltoztatása
        passwordToggleButton.setText(lathato ? "👁" : "🙈");

        // A kurzort az éppen aktív jelszómező végére helyezzük
        if (lathato) {

            passwordField.requestFocus();
            passwordField.positionCaret(passwordField.getText().length());

        } else {

            visiblePasswordField.requestFocus();
            visiblePasswordField.positionCaret(
                    visiblePasswordField.getText().length()
            );
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
