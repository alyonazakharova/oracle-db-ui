package ru.zakharova.alyona.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Tab;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;

public class MainWindowController {

    @FXML
    private Tab journalTab;

    @FXML
    private Tab clientsTab;

    @FXML
    private Tab booksTab;

    @FXML
    private Tab bookTypesTab;

    @FXML
    private Tab extraTab;


    @FXML
    void initialize() {
        try {
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

        try {
            AnchorPane ap5 = FXMLLoader.load(getClass().getResource("../resources/extra.fxml"));
            extraTab.setContent(ap5);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
