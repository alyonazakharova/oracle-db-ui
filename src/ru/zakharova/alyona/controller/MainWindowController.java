package ru.zakharova.alyona.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Tab;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MainWindowController {

    @FXML
    private Tab journalTab;

    @FXML
    private Tab clientsTab;

    @FXML
    private Tab booksTab;

    @FXML
    private Tab bookTypesTab;

    public static Connection connection;

    public MainWindowController() {
        try {
            DriverManager.registerDriver(new oracle.jdbc.OracleDriver());
            connection = DriverManager.getConnection(
                    "jdbc:oracle:thin:@localhost:1521:XE",
                    "c##myuser", "mypass");
        } catch (SQLException e) {
            System.out.println("БАЛИН, ЧТО-ТО НЕ ТАК");
            return;
        }
    }

    @FXML
    void initialize() {
        try {
            System.out.println("MI TOOT");
            try {
                DriverManager.registerDriver(new oracle.jdbc.OracleDriver());
                connection = DriverManager.getConnection(
                        "jdbc:oracle:thin:@localhost:1521:XE",
                        "c##myuser", "mypass");
            } catch (SQLException e) {
                System.out.println("БАЛИН, ЧТО-ТО НЕ ТАК");
                return;
            }
            AnchorPane ap1 = FXMLLoader.load(getClass().getResource("../resources/journal.fxml"));
            journalTab.setContent(ap1);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            AnchorPane ap2 = FXMLLoader.load(getClass().getResource("../resources/clients.fxml"));
            clientsTab.setContent(ap2);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            AnchorPane ap3 = FXMLLoader.load(getClass().getResource("../resources/books.fxml"));
            booksTab.setContent(ap3);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            AnchorPane ap4 = FXMLLoader.load(getClass().getResource("../resources/book-types.fxml"));
            bookTypesTab.setContent(ap4);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
