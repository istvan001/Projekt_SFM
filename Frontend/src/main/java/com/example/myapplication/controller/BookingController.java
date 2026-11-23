package com.example.myapplication.controller;

import com.example.myapplication.Jarat;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.LocalDate;
import com.example.myapplication.service.ApiService;
import com.fasterxml.jackson.databind.JsonNode;
import javafx.application.Platform;
import java.io.IOException;

public class BookingController {

    @FXML private Label utvonalLabel;
    @FXML private Label datumLabel;
    @FXML private Label idoLabel;
    @FXML private Label alapArLabel;
    @FXML private Label vegosszegLabel;

    @FXML private ComboBox<String> jegyTipusCombo;
    @FXML private Spinner<Integer> utasokSzamaSpinner;
    @FXML private Button foglalasButton;

    private Jarat kivalasztottJarat;
    private LocalDate utazasiDatum;

    // A foglalási ablak indulásakor lefut
    @FXML
    public void initialize() {

        // Jegytípusok betöltése
        jegyTipusCombo.setItems(
                FXCollections.observableArrayList(
                        "Teljes árú",
                        "Kedvezményes (50% - teszt)"
                )
        );

        jegyTipusCombo.getSelectionModel().selectFirst();

        // Utasok számának beállítása
        utasokSzamaSpinner.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(
                        1, 6, 1
                )
        );

        // Ha változik a jegytípus, újraszámoljuk az árat
        jegyTipusCombo.setOnAction(event -> vegosszegFrissitese());

        // Ha változik az utasok száma, újraszámoljuk az árat
        utasokSzamaSpinner.valueProperty().addListener(
                (obs, regi, uj) -> vegosszegFrissitese()
        );
    }

    // A kiválasztott járat adatainak átadása
    public void adatokBeallitasa(Jarat jarat, LocalDate datum) {

        this.kivalasztottJarat = jarat;
        this.utazasiDatum = datum;

        utvonalLabel.setText(
                jarat.getIndulas() + " → " + jarat.getErkezes()
        );

        datumLabel.setText(datum.toString());

        idoLabel.setText(
                jarat.getIndulasIdo() + " – " +
                        jarat.getErkezesIdo()
        );

        alapArLabel.setText(jarat.getAr() + " Ft");

        vegosszegFrissitese();
    }

    // A fizetendő összeg kiszámítása
    private void vegosszegFrissitese() {

        if (kivalasztottJarat == null) {
            return;
        }

        double kedvezmeny = 1.0;

        if ("Kedvezményes (50% - teszt)".equals(
                jegyTipusCombo.getValue())) {
            kedvezmeny = 0.5;
        }

        int utasokSzama = utasokSzamaSpinner.getValue();

        double osszeg =
                kivalasztottJarat.getAr()
                        * kedvezmeny
                        * utasokSzama;

        vegosszegLabel.setText(
                String.format("%,.0f Ft", osszeg)
        );
    }

    @FXML
    private void foglalasVeglegesitese() {

        // Ellenőrizzük, hogy minden szükséges adat rendelkezésre áll-e
        if (kivalasztottJarat == null || utazasiDatum == null) {
            hibaUzenet("Nincs kiválasztott járat vagy utazási dátum!");
            return;
        }

        String kivalasztottTipus = jegyTipusCombo.getValue();

        if (kivalasztottTipus == null) {
            hibaUzenet("Válassz jegytípust!");
            return;
        }

        // A Frontend jegytípusának átalakítása Backend-formátumra
        String backendTipus;

        if (kivalasztottTipus.equals("Teljes árú")) {
            backendTipus = "TELJES_ARU";
        } else if (kivalasztottTipus.equals("Kedvezményes (50% - teszt)")) {
            backendTipus = "KEDVEZMENYES";
        } else {
            hibaUzenet("Ismeretlen jegytípus!");
            return;
        }

        int utasok = utasokSzamaSpinner.getValue();
        int jaratId = kivalasztottJarat.getId();
        String datum = utazasiDatum.toString();

        // Dupla kattintás megakadályozása
        foglalasButton.setDisable(true);

        // A HTTP-kérést háttérszálon küldjük el
        Thread szal = new Thread(() -> {

            try {

                // Foglalás elküldése a Backendnek
                JsonNode valasz = ApiService.foglalasLetrehozasa(
                        jaratId,
                        datum,
                        backendTipus,
                        utasok
                );

                // A Backend által visszaadott adatok
                int foglalasId = valasz.path("id").asInt();
                int vegosszeg = valasz.path("vegosszeg").asInt();

                Platform.runLater(() -> {

                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Sikeres foglalás!");
                    alert.setHeaderText("A jegyfoglalás sikeresen létrejött!");

                    alert.setContentText(
                            "Foglalás azonosítója: " + foglalasId + "\n" +
                                    "Fizetendő összeg: " + vegosszeg + " Ft"
                    );

                    alert.showAndWait();

                    // Sikeres foglalás után bezárjuk az ablakot
                    Stage stage = (Stage) utvonalLabel
                            .getScene()
                            .getWindow();

                    stage.close();
                });

            } catch (IOException | InterruptedException e) {

                if (e instanceof InterruptedException) {
                    Thread.currentThread().interrupt();
                }

                Platform.runLater(() -> {

                    foglalasButton.setDisable(false);

                    hibaUzenet(
                            "Nem sikerült elmenteni a foglalást: "
                                    + e.getMessage()
                    );
                });
            }
        });

        szal.setDaemon(true);
        szal.start();
    }

    // Hibaüzenet megjelenítése
    private void hibaUzenet(String uzenet) {

        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Foglalási hiba");
        alert.setHeaderText(null);
        alert.setContentText(uzenet);
        alert.showAndWait();
    }

    // Foglalási ablak bezárása
    @FXML
    private void bezaras() {

        Stage stage = (Stage) utvonalLabel
                .getScene()
                .getWindow();

        stage.close();
    }
}
