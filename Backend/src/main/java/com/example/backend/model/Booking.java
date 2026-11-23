package com.example.backend.model;

public class Booking {

    private int id;
    private int felhasznaloId;
    private int jaratId;
    private String utazasDatuma;
    private String jegyTipus;
    private int utasokSzama;
    private int vegosszeg;
    private String allapot;

    public Booking(int id, int felhasznaloId, int jaratId,
                   String utazasDatuma, String jegyTipus,
                   int utasokSzama, int vegosszeg,
                   String allapot) {

        this.id = id;
        this.felhasznaloId = felhasznaloId;
        this.jaratId = jaratId;
        this.utazasDatuma = utazasDatuma;
        this.jegyTipus = jegyTipus;
        this.utasokSzama = utasokSzama;
        this.vegosszeg = vegosszeg;
        this.allapot = allapot;
    }

    public int getId() {
        return id;
    }

    public int getFelhasznaloId() {
        return felhasznaloId;
    }

    public int getJaratId() {
        return jaratId;
    }

    public String getUtazasDatuma() {
        return utazasDatuma;
    }

    public String getJegyTipus() {
        return jegyTipus;
    }

    public int getUtasokSzama() {
        return utasokSzama;
    }

    public int getVegosszeg() {
        return vegosszeg;
    }

    public String getAllapot() {
        return allapot;
    }
}
