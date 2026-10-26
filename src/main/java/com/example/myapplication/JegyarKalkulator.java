package com.example.myapplication;

public class JegyarKalkulator
{

    public enum Jegytipus
    {
        FELNOTT(2500),
        DIAK(1800),
        NYUGDIJAS(2000);

        private final int ar;

        Jegytipus(int ar)
        {
            this.ar = ar;
        }

        public int getAr()
        {
            return ar;
        }
    }

    public int szamolVegosszeg(Jegytipus tipus, int darab)
    {
        if (tipus == null)
        {
            throw new IllegalArgumentException( "A jegytípust ki kell választani!" );
        }

        if (darab <= 0)
        {
            throw new IllegalArgumentException("Legalább egy jegyet ki kell választani!");
        }

        return tipus.getAr() * darab;
    }
}