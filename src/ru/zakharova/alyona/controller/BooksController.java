package ru.zakharova.alyona.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import ru.zakharova.alyona.Helper;
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
    private Button updateBtn;

    @FXML
    private Button deleteBtn;

    @FXML
    private Button refreshBtn;

    private int selectedBookTypeId;
    private final ObservableList<Book> books = FXCollections.observableArrayList();
    private int selectedForUpdateBookId = -1;

    private void initNewBookTypeCB() {
        ObservableList<BookType> types = FXCollections.observableArrayList();
        Statement stmt = null;
        ResultSet rs = null;
        try {
            stmt = LoginController.connection.createStatement();
            rs = stmt.executeQuery(
                    "SELECT ID, NAME FROM BOOK_TYPES");
            while (rs.next()) {
                types.add(new BookType(
                        rs.getInt("ID"),
                        rs.getString("NAME")));
            }
        } catch (SQLException e) {
            Helper.showInfo(e.getMessage(), Alert.AlertType.WARNING);
        } finally {
            Helper.closeRsAndStmt(rs, stmt);
        }
        newBookTypeCB.setItems(types);
    }

    private void loadBooks() {
        String query = "SELECT B.ID, B.NAME AS BOOK_NAME, B.CNT, BT.NAME AS TYPE_NAME " +
                "FROM BOOKS B JOIN BOOK_TYPES BT on B.TYPE_ID = BT.ID";
        Statement stmt = null;
        ResultSet rs = null;
        try {
            stmt = LoginController.connection.createStatement();
            rs = stmt.executeQuery(query);
            while (rs.next()) {
                books.add(new Book(
                        rs.getInt("ID"),
                        rs.getString("BOOK_NAME"),
                        rs.getInt("CNT"),
                        rs.getString("TYPE_NAME")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Helper.showInfo(e.getMessage(), Alert.AlertType.WARNING);
        } finally {
            Helper.closeRsAndStmt(rs, stmt);
        }
    }

    private void fillTable() {
        books.clear();
        loadBooks();
        booksTable.setItems(books);
    }

    private void clearInput() {
        newBookName.setText("");
        newBookCount.setText("");
    }

    @FXML
    void initialize() {
        idColumn.setCellValueFactory(new PropertyValueFactory<Book, Integer>("id"));
        bookNameColumn.setCellValueFactory(new PropertyValueFactory<Book, String>("name"));
        countColumn.setCellValueFactory(new PropertyValueFactory<Book, Integer>("count"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<Book, String>("type"));

        initNewBookTypeCB();
        fillTable();

        newBookTypeCB.setOnAction(actionEvent -> {
            selectedBookTypeId = newBookTypeCB.getValue().getId();
        });

        addBtn.setOnAction(actionEvent -> {
            if (!newBookName.getText().isEmpty()
                    & !newBookCount.getText().isEmpty()
                    & newBookTypeCB.getValue() != null) {
                int count = -1;
                try {
                    count = Integer.parseInt(newBookCount.getText());
                } catch (NumberFormatException e) {
                    Helper.showInfo("Неверный ввод", Alert.AlertType.WARNING);
                    return;
                }
                String query = "INSERT INTO BOOKS (NAME, CNT, TYPE_ID) VALUES (?, ?, ?)";
                PreparedStatement pstmt = null;
                try {
                    pstmt = LoginController.connection.prepareStatement(query);
                    pstmt.setString(1, newBookName.getText());
                    pstmt.setInt(2, count);
                    pstmt.setInt(3, selectedBookTypeId);
                    pstmt.executeUpdate();
                } catch (SQLException e) {
                    e.printStackTrace();
                    return;
                } finally {
                    Helper.closePstmt(pstmt);
                }
                Helper.showInfo("Книга успешно добавлена", Alert.AlertType.INFORMATION);
                clearInput();
                fillTable();
            } else {
                Helper.showInfo("Необходимо заполнить все поля", Alert.AlertType.WARNING);
            }
        });

        booksTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            Book selectedBook = booksTable.getSelectionModel().getSelectedItem();
            if (selectedBook == null) {
                clearInput();
                return;
            }
            selectedForUpdateBookId = selectedBook.getId();
            newBookName.setText(selectedBook.getName());
            newBookCount.setText(String.valueOf(selectedBook.getCount()));
        });

        updateBtn.setOnAction(actionEvent -> {
            if (booksTable.getSelectionModel().getSelectedItem() == null) {
                Helper.showInfo("Ничего не выбрано", Alert.AlertType.WARNING);
            }
            int count = -1;
            try {
                count = Integer.parseInt(newBookCount.getText());
            } catch (NumberFormatException e) {
                Helper.showInfo("Проверьте ввод", Alert.AlertType.WARNING);
                return;
            }
            String query = "UPDATE BOOKS SET NAME='" + newBookName.getText() +
                    "', CNT=" + count + " WHERE ID=" + selectedForUpdateBookId;
            Statement stmt = null;
            try {
                stmt = LoginController.connection.createStatement();
                stmt.executeQuery(query);
                fillTable();
                Helper.showInfo("Данные обновлены", Alert.AlertType.INFORMATION);
                fillTable();
            } catch (SQLException e) {
                e.printStackTrace();
                Helper.showInfo(e.getMessage(), Alert.AlertType.WARNING);
            } finally {
                Helper.closeStmt(stmt);
            }
        });

        deleteBtn.setOnAction(actionEvent -> {
            Book selectedBook = booksTable.getSelectionModel().getSelectedItem();
            if (selectedBook != null) {
                int bookId = selectedBook.getId();
                String query = "DELETE FROM BOOKS WHERE ID=" + bookId;
                Statement stmt = null;
                try {
                    stmt = LoginController.connection.createStatement();
                    stmt.executeQuery(query);
                    fillTable();
                    Helper.showInfo("Книга успешно удалена", Alert.AlertType.INFORMATION);
                } catch (SQLIntegrityConstraintViolationException e) {
                    Helper.showInfo("Упси, эту книгу нельзя удалить, " +
                            "так как в журнале есть записи о ней", Alert.AlertType.WARNING);
                } catch (SQLException e) {
                    e.printStackTrace();
                    Helper.showInfo(e.getMessage(), Alert.AlertType.WARNING);
                } finally {
                    Helper.closeStmt(stmt);
                }
            } else {
                Helper.showInfo("Ничего не выбрано", Alert.AlertType.WARNING);
            }
        });

        refreshBtn.setOnAction(actionEvent -> {
            fillTable();
        });
    }
}
