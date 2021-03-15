package ru.zakharova.alyona.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import ru.zakharova.alyona.dto.Book;
import ru.zakharova.alyona.dto.BookType;

import java.sql.*;

public class BooksController {

    @FXML
    private TableView<Book> booksTable;

    @FXML
    private TableColumn<Book, Integer> idColumn;

    @FXML
    private TableColumn<Book, String> bookNameColumn;

    @FXML
    private TableColumn<Book, Integer> countColumn;

    @FXML
    private TableColumn<Book, String> typeColumn;

    @FXML
    private TextField newBookName;

    @FXML
    private ComboBox<BookType> newBookTypeCB;

    @FXML
    private TextField newBookCount;

    @FXML
    private Button addBtn;

    @FXML
    private Button deleteBtn;

    @FXML
    private Button refreshBtn;

    private Connection connection;
    private int selectedBookTypeId;
    private final ObservableList<Book> books = FXCollections.observableArrayList();

    public BooksController() {
        this.connection = MainWindowController.connection;
    }

    private void initNewBookTypeCB() throws SQLException {
        ObservableList<BookType> types = FXCollections.observableArrayList();
        ResultSet rs = connection.createStatement().executeQuery(
                "SELECT ID, NAME FROM BOOK_TYPES");
        while (rs.next()) {
            types.add(new BookType(
                    rs.getInt("ID"),
                    rs.getString("NAME")));
        }
        newBookTypeCB.setItems(types);
    }

    private void loadBooks() throws SQLException {
        String query = "SELECT B.ID, B.NAME AS BOOK_NAME, B.CNT, BT.NAME AS TYPE_NAME " +
                "FROM BOOKS B JOIN BOOK_TYPES BT on B.TYPE_ID = BT.ID";

        Statement statement = connection.createStatement();
        ResultSet rs = statement.executeQuery(query);
        while (rs.next()) {
            books.add(new Book(
                    rs.getInt("ID"),
                    rs.getString("BOOK_NAME"),
                    rs.getInt("CNT"),
                    rs.getString("TYPE_NAME"))
            );
        }
        rs.close();
        statement.close();
    }

    private void initColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<Book, Integer>("id"));
//        idColumn.setVisible(false);
        bookNameColumn.setCellValueFactory(new PropertyValueFactory<Book, String>("name"));
        countColumn.setCellValueFactory(new PropertyValueFactory<Book, Integer>("count"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<Book, String>("type"));
    }

    private void fillTable() throws SQLException {
        books.clear();
        loadBooks();
        booksTable.setItems(books);
    }

    private void clearInput() {
        newBookName.setText("");
        newBookCount.setText("");
//        newBookTypeCB
    }

    @FXML
    void initialize() {
        try {
            initNewBookTypeCB();
            initColumns();
            fillTable();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        newBookTypeCB.setOnAction(actionEvent -> {
            selectedBookTypeId = newBookTypeCB.getValue().getId();
        });

        addBtn.setOnAction(actionEvent -> {
            if (!newBookName.getText().isEmpty()
                    & !newBookCount.getText().isEmpty()
                    & newBookTypeCB.getValue() != null) {
                try {
                    int count = Integer.parseInt(newBookCount.getText());
                    Statement stmt = connection.createStatement();
                    String query = "INSERT INTO BOOKS (NAME, CNT, TYPE_ID) VALUES (?, ?, ?)";
                    PreparedStatement pstmt = connection.prepareStatement(query);
                    pstmt.setString(1, newBookName.getText());
                    pstmt.setInt(2, count);
                    pstmt.setInt(3, selectedBookTypeId);
                    pstmt.executeUpdate();
                    System.out.println("Книга добавлена");
                    clearInput();
                    fillTable();
                } catch (Exception e) {
                    System.out.println("Проверьте ввод");
                }
            } else {
                System.out.println("Заполните все поля");
            }
        });

        deleteBtn.setOnAction(actionEvent -> {
            Book selectedBook = booksTable.getSelectionModel().getSelectedItem();
            int bookId = selectedBook.getId();
            String query = "DELETE FROM BOOKS WHERE ID=" + bookId;
            try {
                Statement statement = connection.createStatement();
                statement.executeQuery(query);
                fillTable();
                System.out.println("КНИГА УДАЛЕНА");
            } catch (SQLException throwables) {
                System.out.println("ОЙ, ПОХОЖЕ ЭТУ КНИГУ НЕЛЬЗЯ УДАЛИТЬ");
            }
        });

        refreshBtn.setOnAction(actionEvent -> {
            try {
                fillTable();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        });
    }
}
