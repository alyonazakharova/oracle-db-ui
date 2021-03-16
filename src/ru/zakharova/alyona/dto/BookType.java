package ru.zakharova.alyona.dto;

public class BookType {
    private final int id;
    private String name;
    private int days;
    private double fine;

    public BookType(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public BookType(int id, String name, int days, double fine) {
        this.id = id;
        this.name = name;
        this.days = days;
        this.fine = fine;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getDays() {
        return days;
    }

    public double getFine() {
        return fine;
    }

    @Override
    public String toString() {
        return name;
    }
}
