package ru.zakharova.alyona.dto;

public class Client {
    private final int id;
    private String lastName;
    private String firstName;
    private String fatherName;

    private String passSeria;
    private String passNum;

    public Client(int id, String lastName, String firstName, String fatherName) {
        this.id = id;
        this.lastName = lastName;
        this.firstName = firstName;
        this.fatherName = fatherName;
    }

    public Client(int id, String lastName, String firstName, String fatherName, String passSeria, String passNum) {
        this.id = id;
        this.lastName = lastName;
        this.firstName = firstName;
        this.fatherName = fatherName;
        this.passSeria = passSeria;
        this.passNum = passNum;
    }

    public int getId() {
        return id;
    }

    public String getLastName() {
        return lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getFatherName() {
        return fatherName;
    }

    public String getPassSeria() {
        return passSeria;
    }

    public String getPassNum() {
        return passNum;
    }

    @Override
    public String toString() {
        return lastName + " " + firstName + " " + fatherName;
    }
}
