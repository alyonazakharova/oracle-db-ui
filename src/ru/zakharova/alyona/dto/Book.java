package ru.zakharova.alyona.dto;

public class Book {
    private final int id;
    private String name;
    private int count;
    private String type;

    public Book(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public Book(int id, String name, int count, String type) {
        this.id = id;
        this.name = name;
        this.count = count;
        this.type = type;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getCount() {
        return count;
    }

    public String getType() {
        return type;
    }

    @Override
    public String toString() {
        return name;
    }
}
