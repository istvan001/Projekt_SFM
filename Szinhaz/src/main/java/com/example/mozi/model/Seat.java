package com.example.mozi.model;

public class Seat {
    private final int row;
    private final int number;

    public Seat(int row, int number) {
        this.row = row;
        this.number = number;
    }

    public int getRow() {
        return row;
    }

    public int getNumber() {
        return number;
    }

    @Override
    public String toString() {
        return "model.Seat{" +
                "row=" + row +
                ", number=" + number +
                '}';
    }
}
