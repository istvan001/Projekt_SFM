package com.example.mozi.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.util.ArrayList;

@Entity
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private  ArrayList<Seat> Availableseats;
    private  ArrayList<String> screeningTimes;
    private  int roomNum;


    public Room(int roomNum) {

        this.Availableseats = new ArrayList<>();
        this.screeningTimes = new ArrayList<>();
        this.roomNum = roomNum;
    }

    public Room() {

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ArrayList<Seat> getAvailableseats() {
        return Availableseats;
    }

    public ArrayList<String> getScreeningTimes() {
        return screeningTimes;
    }

    public int getRoomNum() {
        return roomNum;
    }

    @Override
    public String toString() {
        return "model.Room{" +
                "seats=" + Availableseats +
                '}';
    }
}
