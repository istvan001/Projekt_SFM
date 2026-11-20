package com.example.mozi.model;

import java.util.ArrayList;

public class Customer {
    private final String name;
    private final String email;
    private final String tellNum;
    private ArrayList<Ticket> ticket;

    private Customer(Builder builder) {
        this.name = builder.name;
        this.email = builder.email;
        this.tellNum = builder.tellNum;
        this.ticket = new ArrayList<>();

    }

    public static class Builder
    {
        private final String name;
        private final String email;
        private final String tellNum;
        private ArrayList<Ticket> ticket;

        public Builder(String name, String email, String tellNum) {
            this.name = name;
            this.email = email;
            this.tellNum = tellNum;
        }
        public Builder ticket(Ticket ticket)
        {
            this.ticket = new ArrayList<>();
            return this;
        }

        public Customer build()
        {
            return new Customer(this);
        }


    }

    public void addTicket(Ticket ticket) {this.ticket.add(ticket);}

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getTellNum() {
        return tellNum;
    }

    public ArrayList<Ticket> getTicket() {
        return ticket;
    }
}
