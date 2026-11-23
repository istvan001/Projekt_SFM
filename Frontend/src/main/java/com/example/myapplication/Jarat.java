package com.example.myapplication;

public class Jarat {

    private final int id;
    private final String indulas;
    private final String erkezes;
    private final String indulasIdo;
    private final String erkezesIdo;
    private final int ar;

    public Jarat(int id, String indulas,
                 String erkezes, String indulasIdo,
                 String erkezesIdo, int ar) {

        this.id = id;
        this.indulas = indulas;
        this.erkezes = erkezes;
        this.indulasIdo = indulasIdo;
        this.erkezesIdo =erkezesIdo;
        this.ar = ar;
    }

    public int getId() {
        return id;
    }

    public String getIndulas() {
        return indulas;
    }

    public String getErkezes() {
        return erkezes;
    }

    public String getIndulasIdo() {
        return indulasIdo;
    }

    public String getErkezesIdo() {
        return erkezesIdo;
    }

    public int getAr() {
        return ar;
    }
}
