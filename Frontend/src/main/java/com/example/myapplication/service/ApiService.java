
package com.example.myapplication.service;

import com.example.myapplication.Jarat;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/*
 * ApiService.java
 *
 * Ez az osztály a JegyGo JavaFX frontend és a Spring Boot
 * backend közötti kommunikációért felel.
 * HTTP-kéréseket küld a backend API-végpontjaira,
 * és feldolgozza a JSON-formátumban érkező válaszokat.
 * Kezeli a regisztrációt, a bejelentkezést, a munkamenet
 * adatait, valamint a járatok lekérését, hozzáadását és törlését.
 * A frontend ezen az osztályon keresztül éri el a backend
 * szolgáltatásait, közvetlen adatbázis-kapcsolat nélkül.
 */

public class ApiService {

    // A backend API alapértelmezett címe
    private static final String BASE_URL =
            "http://localhost:8080/api";

    // HTTP-kliens létrehozása 5 másodperces kapcsolódási időkorláttal
    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    // A JSON-adatok átalakítására és feldolgozására szolgáló objektum
    private static final ObjectMapper JSON = new ObjectMapper();

    // A bejelentkezett felhasználó munkamenetének adatai
    private static String token;
    private static boolean admin;
    private static int userId = -1;

    // Visszaadja, hogy a bejelentkezett felhasználó adminisztrátor-e
    public static boolean isAdmin() {
        return admin;
    }

    // Visszaadja a bejelentkezett felhasználó azonosítóját
    public static int getUserId() {
        return userId;
    }

    // Közös metódus a backendnek küldött HTTP-kérések kezelésére
    private static JsonNode keres(
            String method, String path, Object adat)
            throws IOException, InterruptedException {

        // HTTP-kérés összeállítása a megadott API-végponthoz
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + path))
                .timeout(Duration.ofSeconds(15));

        // Ha van bejelentkezési token, elküldjük a kérés fejlécében
        if (token != null) {
            builder.header("Authorization", "Bearer " + token);
        }

        if (adat != null) {

            // JSON-adatok küldése esetén beállítjuk a tartalomtípust
            builder.header("Content-Type", "application/json");

            // Az adatokat JSON-szöveggé alakítjuk és a kéréshez csatoljuk
            builder.method(method, HttpRequest.BodyPublishers.ofString(
                    JSON.writeValueAsString(adat),
                    StandardCharsets.UTF_8
            ));

        } else {

            // Ha nincs küldendő adat, üres törzzsel hozzuk létre a kérést
            builder.method(method,
                    HttpRequest.BodyPublishers.noBody());
        }

        // A HTTP-kérés elküldése és a backend válaszának fogadása
        HttpResponse<String> response = CLIENT.send(
                builder.build(),
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)
        );

        // Ellenőrizzük, hogy a kérés sikeres volt-e
        // A 200 és 299 közötti HTTP-státuszkódok sikeres választ jelentenek
        if (response.statusCode() < 200
                || response.statusCode() >= 300) {

            throw new IOException(
                    "Backend hiba (HTTP "
                            + response.statusCode() + ")"
            );
        }

        // Ha a backend nem küldött választörzset,
        // egy üres JSON-objektumot adunk vissza
        if (response.body().isBlank()) {
            return JSON.createObjectNode();
        }

        // A backend JSON-válaszát feldolgozható objektummá alakítjuk
        return JSON.readTree(response.body());
    }

    // Új felhasználó regisztrálása a backend segítségével
    public static void register(
            String nev, String email, String jelszo)
            throws IOException, InterruptedException {

        // A regisztrációs adatok elküldése POST-kéréssel
        keres("POST", "/auth/register",
                Map.of(
                        "nev", nev,
                        "email", email,
                        "jelszo", jelszo
                ));
    }

    // Felhasználó bejelentkeztetése
    public static int login(String nev, String jelszo)
            throws IOException, InterruptedException {

        // A korábbi bejelentkezési munkamenet adatainak törlése
        token = null;
        admin = false;
        userId = -1;

        // A bejelentkezési adatok elküldése a backendnek
        JsonNode valasz = keres(
                "POST", "/auth/login",
                Map.of("nev", nev, "jelszo", jelszo)
        );

        // A backend válaszából kiolvassuk a munkamenet adatait
        token = valasz.path("token").asText();
        admin = valasz.path("admin").asBoolean();
        userId = valasz.path("userId").asInt();

        // Visszaadjuk a bejelentkezett felhasználó azonosítóját
        return userId;
    }

    // Az összes járat lekérése a backendből
    public static List<Jarat> osszesJarat()
            throws IOException, InterruptedException {

        // GET-kérés küldése a járatok lekéréséhez
        JsonNode valasz = keres("GET", "/jaratok", null);

        // Lista létrehozása a lekért járatok tárolására
        List<Jarat> jaratok = new ArrayList<>();

        // A JSON-válaszban található járatok feldolgozása
        for (JsonNode adat : valasz) {

            // Minden JSON-objektumból létrehozunk egy Jarat objektumot
            jaratok.add(new Jarat(
                    adat.path("id").asInt(),
                    adat.path("indulas").asText(),
                    adat.path("erkezes").asText(),
                    adat.path("indulasIdo").asText(),
                    adat.path("erkezesIdo").asText(),
                    adat.path("ar").asInt()
            ));
        }

        // Visszaadjuk a járatokat tartalmazó listát
        return jaratok;
    }

    // Új járat hozzáadása a backend segítségével
    public static void jaratHozzaadasa(
            String indulas, String erkezes,
            String indulasIdo, String erkezesIdo, int ar)
            throws IOException, InterruptedException {

        // Az új járat adatainak elküldése POST-kéréssel
        keres("POST", "/jaratok",
                Map.of(
                        "indulas", indulas,
                        "erkezes", erkezes,
                        "indulasIdo", indulasIdo,
                        "erkezesIdo", erkezesIdo,
                        "ar", ar
                ));
    }

    // Járat törlése az azonosítója alapján
    public static void jaratTorlese(int id)
            throws IOException, InterruptedException {

        // DELETE-kérés küldése a megadott járat törléséhez
        keres("DELETE", "/jaratok/" + id, null);
    }
}
