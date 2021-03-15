package ru.zakharova.alyona.dto;

public class BookType {
    private final int id;
    private String name;

    public BookType(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }
}
