package com.example.myapplication;

public class JegyarKalkulatorTeszt
{

    public static void main(String[] args)
    {
        JegyarKalkulator kalkulator = new JegyarKalkulator();

        int vegosszeg = kalkulator.szamolVegosszeg(JegyarKalkulator.Jegytipus.DIAK, 3);

        System.out.println("Fizetendő: " + vegosszeg + " Ft");
    }
}