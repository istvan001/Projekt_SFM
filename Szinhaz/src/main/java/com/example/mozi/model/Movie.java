package com.example.mozi.model;


import jakarta.persistence.*;


@Entity
public class Movie {


    @jakarta.persistence.Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String title;
    private String author;
    private String length;
    private String genre;
    @ManyToOne
    private Room roomNumber;
    private boolean onlyForAdults;



    private Movie(Builder builder) {
        this.title = builder.title;
        this.author = builder.author;
        this.length = builder.length;
        this.genre = builder.genre;
        this.roomNumber = builder.roomNumber;
        this.onlyForAdults = builder.onlyForAdults;
    }

    protected Movie()
    {

    }


    public static class Builder {
        private final String title;
        private final String author;
        private final String length;
        private final String genre;

        private Room roomNumber;
        private boolean onlyForAdults;

        public Builder(String title, String author, String length, String genre) {
            this.title = title;
            this.author = author;
            this.length = length;
            this.genre = genre;
        }

        public Builder isForAdultsOnly(boolean onlyForAdults) {
            this.onlyForAdults = onlyForAdults;
            return this;
        }

        public Builder roomNumber(Room roomNumber)
        {
            this.roomNumber = roomNumber;
            return this;
        }

        public Movie build() {
            return new Movie(this);
        }

    }

    public void setRoom(Room roomNumber) {
        this.roomNumber = roomNumber;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getLength() {
        return length;
    }

    public String getGenre() {
        return genre;
    }

    public Room getRoomNumber() {
        return roomNumber;
    }

    public boolean isOnlyForAdults() {
        return onlyForAdults;
    }

    @Override
    public String toString() {
        return "model.Movie{" +
                "title='" + title + '\'' +
                ", author='" + author + '\'' +
                ", length='" + length + '\'' +
                ", genre='" + genre + '\'' +
                ", roomNumber=" + roomNumber +
                ", onlyForAdults=" + onlyForAdults +
                '}';
    }
}

