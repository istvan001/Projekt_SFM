package com.example.backend.model;

/*
 * Jarat.java
 *
 * Ez az osztály egy járat adatainak tárolásáért felel.
 * Tartalmazza a járat azonosítóját, indulási és érkezési
 * helyét, időpontjait, valamint a jegyárat.
 * A backend ezt a modellt használja a járatok kezeléséhez
 * és adatainak továbbításához a frontend felé.
 */

public class Jarat {

    // A járat adatai
    private int id;
    private String indulas;
    private String erkezes;
    private String indulasIdo;
    private String erkezesIdo;
    private int ar;

    // Konstruktor: létrehoz egy járatot a megadott adatokkal
    public Jarat(int id, String indulas, String erkezes,
                 String indulasIdo, String erkezesIdo, int ar) {

        this.id = id;
        this.indulas = indulas;
        this.erkezes = erkezes;
        this.indulasIdo = indulasIdo;
        this.erkezesIdo = erkezesIdo;
        this.ar = ar;
    }

    // Visszaadja a járat egyedi azonosítóját
    public int getId() {
        return id;
    }

    // Visszaadja az indulási települést
    public String getIndulas() {
        return indulas;
    }

    // Visszaadja az érkezési települést
    public String getErkezes() {
        return erkezes;
    }

    // Visszaadja az indulás időpontját
    public String getIndulasIdo() {
        return indulasIdo;
    }

    // Visszaadja az érkezés időpontját
    public String getErkezesIdo() {
        return erkezesIdo;
    }

    // Visszaadja a jegy árát
    public int getAr() {
        return ar;
    }
}
