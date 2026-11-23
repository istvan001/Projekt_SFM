package com.example.myapplication.controller;

import com.example.myapplication.Jarat;
import com.example.myapplication.service.ApiService;

import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.text.Collator;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

/*
 * Controller.java
 *
 * Ez az osztály a JegyGo JavaFX alkalmazás
 * felhasználói járatkereső felületének működéséért felel.
 * A backendtől lekéri a rendelkezésre álló járatokat,
 * feltölti az indulási és érkezési állomások listáját,
 * és lehetővé teszi a járatok keresését.
 * A keresési találatokat táblázatban jeleníti meg,
 * valamint kezeli a dátumválasztást és a hibaüzeneteket.
 */

public class Controller {

    // Az indulási és érkezési állomás kiválasztására szolgáló listák
    @FXML private ComboBox<String> indulasCombo;
    @FXML private ComboBox<String> erkezesCombo;

    // Az utazás dátumának kiválasztására szolgáló mező
    @FXML private DatePicker datumPicker;

    // A keresési találatok megjelenítésére szolgáló táblázat és oszlopai
    @FXML private TableView<Jarat> jaratokTable;
    @FXML private TableColumn<Jarat, String> indulasColumn;
    @FXML private TableColumn<Jarat, String> erkezesColumn;
    @FXML private TableColumn<Jarat, String> indulasIdoColumn;
    @FXML private TableColumn<Jarat, String> erkezesIdoColumn;
    @FXML private TableColumn<Jarat, Integer> arColumn;
    // A foglalási gombokat tartalmazó oszlop
    @FXML private TableColumn<Jarat, Void> foglalasColumn;

    // A backendtől lekért összes járat tárolása
    private List<Jarat> osszesJarat = new ArrayList<>();

    // A felület megnyitásakor automatikusan lefutó metódus
    @FXML
    public void initialize() {

        // Az oszlopok kitöltik a táblázat rendelkezésre álló szélességét
        jaratokTable.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS
        );

        // Alapértelmezett dátumként a mai napot állítjuk be
        datumPicker.setValue(LocalDate.now());

        // A múltbeli dátumok kiválasztásának tiltása
        datumPicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isBefore(LocalDate.now()));
            }
        });

        // A táblázat oszlopainak összekapcsolása a járatok adataival
        indulasColumn.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().getIndulas()
                )
        );

        erkezesColumn.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().getErkezes()
                )
        );

        indulasIdoColumn.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().getIndulasIdo()
                )
        );

        erkezesIdoColumn.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().getErkezesIdo()
                )
        );

        arColumn.setCellValueFactory(data ->
                new SimpleObjectProperty<>(
                        data.getValue().getAr()
                )
        );
        // Foglalás gomb létrehozása minden járat sorában
        foglalasColumn.setCellFactory(column -> new TableCell<>() {

            private final Button foglalasButton = new Button("Foglalás");

            {
                foglalasButton.setMaxWidth(Double.MAX_VALUE);

                foglalasButton.setOnAction(event -> {

                    // Az aktuális sorban szereplő járat lekérése
                    Jarat kivalasztottJarat =
                            getTableView().getItems().get(getIndex());

                    // Megnyitjuk a foglalási folyamatot
                    foglalasMegnyitasa(kivalasztottJarat);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);

                // Üres sornál ne jelenjen meg gomb
                setGraphic(empty ? null : foglalasButton);
            }
        });

        // Ha az indulási állomás megváltozik,
        // frissítjük az elérhető célállomásokat
        indulasCombo.valueProperty().addListener(
                (obs, regi, uj) -> {

                    // Töröljük a korábban kiválasztott célállomást
                    erkezesCombo.setValue(null);

                    // Betöltjük az új indulási helyhez tartozó célállomásokat
                    celallomasokBetoltese(uj);

                    // Töröljük a korábbi keresési találatokat
                    jaratokTable.getItems().clear();
                }
        );

        // A listákat addig letiltjuk, amíg a járatok be nem töltődnek
        erkezesCombo.setDisable(true);
        indulasCombo.setDisable(true);

        // A járatok lekérése a backendtől
        jaratokBetoltese();
    }

    // Az összes járat betöltése a backendből
    private void jaratokBetoltese() {

        // A HTTP-kérést külön szálon futtatjuk,
        // hogy a JavaFX felület közben ne akadjon meg
        Thread szal = new Thread(() -> {
            try {

                // Lekérjük a járatokat az ApiService segítségével
                List<Jarat> jaratok = ApiService.osszesJarat();

                // A felület frissítését a JavaFX szálán végezzük
                Platform.runLater(() -> {

                    // Eltároljuk a backendtől kapott járatokat
                    osszesJarat = jaratok;

                    // Feltöltjük az indulási állomások listáját
                    allomasokBetoltese();

                    // Engedélyezzük az indulási állomás kiválasztását
                    indulasCombo.setDisable(false);
                });

            } catch (Exception e) {

                // Kapcsolódási hiba esetén tájékoztatjuk a felhasználót
                e.printStackTrace();

                Platform.runLater(() ->
                        hiba("Nem sikerült kapcsolódni a backendhez!")
                );
            }
        });

        // A háttérszál ne akadályozza az alkalmazás bezárását
        szal.setDaemon(true);
        szal.start();
    }

    // Az indulási állomások listájának feltöltése
    private void allomasokBetoltese() {

        List<String> allomasok = new ArrayList<>();

        // Összegyűjtjük az összes különböző indulási települést
        for (Jarat jarat : osszesJarat) {

            // Egy település csak egyszer szerepeljen a listában
            if (!allomasok.contains(jarat.getIndulas())) {
                allomasok.add(jarat.getIndulas());
            }
        }

        // Magyar ábécé szerinti rendezés
        Collator rendezes =
                Collator.getInstance(Locale.forLanguageTag("hu-HU"));

        allomasok.sort(rendezes);

        // A rendezett állomások megjelenítése a legördülő listában
        indulasCombo.setItems(
                FXCollections.observableArrayList(allomasok)
        );
    }

    // Az adott indulási helyről elérhető célállomások betöltése
    private void celallomasokBetoltese(String indulas) {

        // A korábbi célállomások törlése
        erkezesCombo.getItems().clear();

        // Ha nincs indulási állomás, letiltjuk a célállomás kiválasztását
        if (indulas == null) {
            erkezesCombo.setDisable(true);
            return;
        }

        List<String> celallomasok = new ArrayList<>();

        // Csak a kiválasztott indulási helyről elérhető célállomásokat gyűjtjük össze
        for (Jarat jarat : osszesJarat) {

            if (jarat.getIndulas().equals(indulas)
                    && !celallomasok.contains(jarat.getErkezes())) {

                celallomasok.add(jarat.getErkezes());
            }
        }

        // A célállomások rendezése magyar ábécé szerint
        Collator rendezes =
                Collator.getInstance(Locale.forLanguageTag("hu-HU"));

        celallomasok.sort(rendezes);

        // A célállomások megjelenítése a legördülő listában
        erkezesCombo.setItems(
                FXCollections.observableArrayList(celallomasok)
        );

        // Ha nincs elérhető célállomás, letiltjuk a listát
        erkezesCombo.setDisable(celallomasok.isEmpty());
    }

    // Járatok keresése a kiválasztott indulási és érkezési hely alapján
    @FXML
    private void jaratokKeresese() {

        // A kiválasztott állomások lekérése
        String indulas = indulasCombo.getValue();
        String erkezes = erkezesCombo.getValue();

        // Ellenőrizzük, hogy mindkét állomás ki van-e választva
        if (indulas == null || erkezes == null) {
            hiba("Válaszd ki az indulási és célállomást!");
            return;
        }

        // Az indulási és érkezési állomás nem lehet azonos
        if (indulas.equals(erkezes)) {
            hiba("Az indulási és célállomás nem lehet azonos!");
            return;
        }

        // Lista létrehozása a keresési találatok számára
        List<Jarat> talalatok = new ArrayList<>();

        // Kiválasztjuk a megfelelő indulási és érkezési helyű járatokat
        for (Jarat jarat : osszesJarat) {

            if (jarat.getIndulas().equals(indulas)
                    && jarat.getErkezes().equals(erkezes)) {

                talalatok.add(jarat);
            }
        }

        // A találatok rendezése indulási idő szerint
        talalatok.sort(
                (a, b) -> a.getIndulasIdo()
                        .compareTo(b.getIndulasIdo())
        );

        // A megfelelő járatok megjelenítése a táblázatban
        jaratokTable.setItems(
                FXCollections.observableArrayList(talalatok)
        );

        // Ha nincs megfelelő járat, tájékoztató üzenetet jelenítünk meg
        if (talalatok.isEmpty()) {

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Nincs találat");
            alert.setHeaderText(null);
            alert.setContentText("Nincs megfelelő járat.");
            alert.showAndWait();
        }
    }
    // A kiválasztott járat foglalási folyamatának elindítása
    private void foglalasMegnyitasa(Jarat jarat) {

        try {
            // A foglalási felület betöltése
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(
                            "/com/example/myapplication/BookingView.fxml"
                    )
            );

            Parent root = loader.load();

            // A foglalási Controller elérése
            BookingController controller = loader.getController();

            // A kiválasztott járat és dátum átadása
            controller.adatokBeallitasa(
                    jarat,
                    datumPicker.getValue()
            );

            // Új ablak létrehozása
            Stage stage = new Stage();

            stage.setTitle("JegyGo - Jegyfoglalás");

            stage.setScene(new Scene(root));

            // A keresőablathoz kapcsolódó ablak legyen
            stage.initOwner(jaratokTable.getScene().getWindow());

            stage.initModality(Modality.WINDOW_MODAL);

            stage.setResizable(false);

            stage.showAndWait();

        } catch (IOException e) {

            e.printStackTrace();

            hiba("Nem sikerült megnyitni a jegyfoglalási ablakot!");
        }
    }

    // Hibaüzenet megjelenítése felugró ablakban
    private void hiba(String uzenet) {

        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Hiba");
        alert.setHeaderText(null);
        alert.setContentText(uzenet);
        alert.showAndWait();
    }
}
