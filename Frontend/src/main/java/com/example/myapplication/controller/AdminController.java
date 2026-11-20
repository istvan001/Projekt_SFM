package com.example.myapplication.controller;

import com.example.myapplication.Jarat;
import com.example.myapplication.service.ApiService;

import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.time.LocalTime;
import java.time.format.DateTimeParseException;

/*
 * AdminController.java
 *
 * Ez az osztály a JegyGo JavaFX alkalmazás
 * adminisztrátori felületének működéséért felel.
 * Megjeleníti az adatbázisban található járatokat,
 * lehetővé teszi új járatok hozzáadását és a meglévők törlését.
 * Ellenőrzi a felhasználó által megadott adatokat,
 * és az ApiService segítségével kommunikál a backenddel.
 */

public class AdminController {

    // Az új járat adatainak megadására szolgáló szövegmezők
    @FXML private TextField indulasField;
    @FXML private TextField erkezesField;
    @FXML private TextField indulasIdoField;
    @FXML private TextField erkezesIdoField;
    @FXML private TextField arField;

    // A járatok megjelenítésére szolgáló táblázat és oszlopai
    @FXML private TableView<Jarat> jaratokTable;
    @FXML private TableColumn<Jarat, Integer> idColumn;
    @FXML private TableColumn<Jarat, String> indulasColumn;
    @FXML private TableColumn<Jarat, String> erkezesColumn;
    @FXML private TableColumn<Jarat, String> indulasIdoColumn;
    @FXML private TableColumn<Jarat, String> erkezesIdoColumn;
    @FXML private TableColumn<Jarat, Integer> arColumn;

    // Az adminfelület megnyitásakor automatikusan lefutó metódus
    @FXML
    public void initialize() {

        // A táblázat oszlopainak igazítása a rendelkezésre álló szélességhez
        jaratokTable.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS
        );

        // A táblázat oszlopainak összekapcsolása a Jarat objektum adataival
        idColumn.setCellValueFactory(data ->
                new SimpleObjectProperty<>(data.getValue().getId())
        );

        indulasColumn.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getIndulas())
        );

        erkezesColumn.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getErkezes())
        );

        indulasIdoColumn.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getIndulasIdo())
        );

        erkezesIdoColumn.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getErkezesIdo())
        );

        arColumn.setCellValueFactory(data ->
                new SimpleObjectProperty<>(data.getValue().getAr())
        );

        // Az adminfelület megnyitásakor betöltjük a járatokat
        jaratokBetoltese();
    }

    // Az összes járat lekérése a backendből
    @FXML
    private void jaratokBetoltese() {

        // A HTTP-kérést külön szálon futtatjuk,
        // hogy a JavaFX felülete közben ne akadjon meg
        Thread szal = new Thread(() -> {
            try {

                // Lekérjük az összes járatot az ApiService segítségével
                var jaratok = ApiService.osszesJarat();

                // A táblázat frissítését a JavaFX felületi szálán végezzük
                Platform.runLater(() ->
                        jaratokTable.setItems(
                                FXCollections.observableArrayList(jaratok)
                        )
                );

            } catch (Exception e) {

                // Hiba esetén megjelenítjük a hibaüzenetet
                e.printStackTrace();

                Platform.runLater(() ->
                        hiba("Nem sikerült betölteni a járatokat a backendből.")
                );
            }
        });

        // A háttérszál ne akadályozza az alkalmazás bezárását
        szal.setDaemon(true);
        szal.start();
    }

    // Új járat hozzáadása az adminisztrátori felületen
    @FXML
    private void jaratHozzaadasa() {

        // A felhasználó által megadott adatok kiolvasása
        String indulas = indulasField.getText().trim();
        String erkezes = erkezesField.getText().trim();
        String indulasIdo = indulasIdoField.getText().trim();
        String erkezesIdo = erkezesIdoField.getText().trim();
        String arSzoveg = arField.getText().trim();

        // Ellenőrizzük, hogy az állomások meg vannak-e adva
        if (indulas.isBlank() || erkezes.isBlank()) {
            hiba("Add meg az indulási és célállomást!");
            return;
        }

        // Az indulási és célállomás nem lehet azonos
        if (indulas.equalsIgnoreCase(erkezes)) {
            hiba("Az indulási és célállomás nem lehet azonos!");
            return;
        }

        int ar;

        try {

            // Az indulási és érkezési időpontok ellenőrzése
            LocalTime.parse(indulasIdo);
            LocalTime.parse(erkezesIdo);

            // A jegyár átalakítása egész számmá
            ar = Integer.parseInt(arSzoveg);

            // A jegyárnak pozitívnak kell lennie
            if (ar <= 0) {
                hiba("A jegyárnak pozitívnak kell lennie!");
                return;
            }

        } catch (DateTimeParseException e) {

            // Hibás időpontformátum esetén figyelmeztetés
            hiba("Az időpont formátuma HH:mm legyen!");
            return;

        } catch (NumberFormatException e) {

            // Ha a jegyár nem egész szám, hibaüzenetet jelenítünk meg
            hiba("A jegyár csak egész szám lehet!");
            return;
        }

        // Az új járat adatait elküldjük a backendnek
        Thread szal = new Thread(() -> {
            try {

                // A járat mentését a backend végzi
                ApiService.jaratHozzaadasa(
                        indulas, erkezes,
                        indulasIdo, erkezesIdo, ar
                );

                Platform.runLater(() -> {

                    // Sikeres hozzáadásról tájékoztatjuk a felhasználót
                    uzenet("A járat sikeresen hozzáadva!");

                    // A beviteli mezők kiürítése
                    indulasField.clear();
                    erkezesField.clear();
                    indulasIdoField.clear();
                    erkezesIdoField.clear();
                    arField.clear();

                    // A táblázat frissítése az új járattal
                    jaratokBetoltese();
                });

            } catch (Exception e) {

                // Sikertelen mentés esetén hibaüzenet megjelenítése
                e.printStackTrace();

                Platform.runLater(() ->
                        hiba("Nem sikerült hozzáadni a járatot.")
                );
            }
        });

        szal.setDaemon(true);
        szal.start();
    }

    // A táblázatban kijelölt járat törlése
    @FXML
    private void jaratTorlese() {

        // Lekérjük a felhasználó által kijelölt járatot
        Jarat kivalasztott =
                jaratokTable.getSelectionModel().getSelectedItem();

        // Ha nincs kijelölt járat, figyelmeztetést jelenítünk meg
        if (kivalasztott == null) {
            hiba("Először válassz ki egy járatot!");
            return;
        }

        // Megerősítő ablak létrehozása a véletlen törlés elkerülésére
        Alert megerosites =
                new Alert(Alert.AlertType.CONFIRMATION);

        megerosites.setTitle("Járat törlése");
        megerosites.setHeaderText(
                "Biztosan törölni szeretnéd?"
        );

        // Megjelenítjük a törlendő járat útvonalát
        megerosites.setContentText(
                kivalasztott.getIndulas() + " → "
                        + kivalasztott.getErkezes()
        );

        // Megvárjuk a felhasználó döntését
        var valasz = megerosites.showAndWait();

        // Ha nem erősíti meg a törlést, megszakítjuk a műveletet
        if (valasz.isEmpty() || valasz.get() != ButtonType.OK) {
            return;
        }

        // A törlési kérést külön háttérszálon küldjük el
        Thread szal = new Thread(() -> {
            try {

                // A backend az azonosító alapján törli a járatot
                ApiService.jaratTorlese(kivalasztott.getId());

                Platform.runLater(() -> {

                    // Frissítjük a táblázatot
                    jaratokBetoltese();

                    // Tájékoztatjuk a felhasználót a sikeres törlésről
                    uzenet("A járat sikeresen törölve!");
                });

            } catch (Exception e) {

                // Sikertelen törlés esetén hibaüzenetet jelenítünk meg
                e.printStackTrace();

                Platform.runLater(() ->
                        hiba("Nem sikerült törölni a járatot.")
                );
            }
        });

        szal.setDaemon(true);
        szal.start();
    }

    // Hibaüzenet megjelenítése felugró ablakban
    private void hiba(String szoveg) {

        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText(null);
        alert.setContentText(szoveg);
        alert.showAndWait();
    }

    // Tájékoztató üzenet megjelenítése felugró ablakban
    private void uzenet(String szoveg) {

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setContentText(szoveg);
        alert.showAndWait();
    }
}
