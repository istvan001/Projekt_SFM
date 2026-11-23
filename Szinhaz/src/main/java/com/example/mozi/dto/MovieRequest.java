package com.example.mozi.dto;

import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotBlank;

public class MovieRequest {

    @NotBlank(message = "A film címe nem lehet üres")
    @Size(max = 200, message = "A feladat címe maximum 200 karakter lehet")
    private String title;
    @NotBlank(message = "A film szerzőjét kötelező megadni")
    @Size(max = 200, message = "A szerző neve maximum 50 karakter lehet")
    private String author;
    @NotBlank(message = "Adja meg a film idejét óra:perc formátumban")
    @Size(max = 5, message = "Maximum 5 karaktert adjon meg a film idejénél")
    private String length;
    @NotBlank(message = "Adja meg a film típusát")
    @Size(max = 50, message = "A film típusa maximum 50 karakter lehet")
    private String genre;

    public MovieRequest() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getLength() {
        return length;
    }

    public void setLength(String length) {
        this.length = length;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

}
