package ru.zakharova.alyona.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import ru.zakharova.alyona.dto.BookType;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class BookTypesController {

    @FXML
    private TableView<BookType> bookTypesTable;

    @FXML
    private TableColumn<BookType, Integer> idColumn;

    @FXML
    private TableColumn<BookType, String> typeColumn;

    @FXML
    private TableColumn<BookType, Integer> daysColumn;

    @FXML
    private TableColumn<BookType, Double> fineColumn;

    @FXML
    private TextField typeField;

    @FXML
    private TextField daysField;

    @FXML
    private TextField fineField;

    @FXML
    private Button addBtn;

    @FXML
    private Button updateBtn;

    @FXML
    private Button deleteBtn;

    private final Connection connection;
    private final ObservableList<BookType> booksTypes = FXCollections.observableArrayList();

    public BookTypesController() {
        this.connection = MainWindowController.connection;
    }

    private void loadBookTypes() {
        String query = "SELECT ID, NAME, DAY_COUNT, FINE FROM BOOK_TYPES";
        Statement stmt = null;
        ResultSet rs = null;
        try {
            stmt = connection.createStatement();
            rs = stmt.executeQuery(query);
            while (rs.next()) {
                booksTypes.add(new BookType(
                        rs.getInt("ID"),
                        rs.getString("NAME"),
                        rs.getInt("DAY_COUNT"),
                        rs.getDouble("FINE")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            MainWindowController.showInfo("Кто-то чё-то плохо закодил, " +
                    "и типы книг не загрузились нормально", Alert.AlertType.WARNING);
        } finally {
            MainWindowController.closeRsAndStmt(rs, stmt);
        }
    }

    private void fillTable() {
        booksTypes.clear();
        loadBookTypes();
        bookTypesTable.setItems(booksTypes);
    }

    @FXML
    void initialize() {
        idColumn.setCellValueFactory(new PropertyValueFactory<BookType, Integer>("id"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<BookType, String>("name"));
        daysColumn.setCellValueFactory(new PropertyValueFactory<BookType, Integer>("days"));
        fineColumn.setCellValueFactory(new PropertyValueFactory<BookType, Double>("fine"));

        fillTable();

        addBtn.setOnAction(actionEvent -> {
            MainWindowController.showInfo("add button", Alert.AlertType.INFORMATION);
        });

        updateBtn.setOnAction(actionEvent -> {

        });

        deleteBtn.setOnAction(actionEvent -> {

        });
    }

}
