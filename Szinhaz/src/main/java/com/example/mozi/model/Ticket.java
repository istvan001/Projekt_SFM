package com.example.mozi.model;

public class Ticket {
    private final Movie movie;
    private final Room room;
    private final Seat seat;
    private final int ticketId;

    public Ticket(Movie movie, Room roomNumber, Seat seat,int ticketId) {
        this.movie = movie;
        this.room = roomNumber;
        this.seat = seat;
        this.ticketId = ticketId;
    }

    public Movie getMovie() {
        return movie;
    }

    public Room getRoom() {
        return room;
    }

    public Seat getSeat() {
        return seat;
    }

    public int getTicketId() {
        return ticketId;
    }

    @Override
    public String toString() {
        return "model.Ticket{" +
                "movie=" + movie.getTitle() +
                ", roomNumber=" + room.getRoomNum() +
                ", seat=" + seat +
                ", ticketId=" + ticketId +
                '}';
    }
}
