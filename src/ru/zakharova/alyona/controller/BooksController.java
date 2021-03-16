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

    private final Connection connection;
    private int selectedBookTypeId;
    private final ObservableList<Book> books = FXCollections.observableArrayList();

    public BooksController() {
        this.connection = MainWindowController.connection;
    }

    private void initNewBookTypeCB() {
        ObservableList<BookType> types = FXCollections.observableArrayList();
        Statement stmt = null;
        ResultSet rs = null;
        try {
            stmt = connection.createStatement();
            rs = stmt.executeQuery(
                    "SELECT ID, NAME FROM BOOK_TYPES");
            while (rs.next()) {
                types.add(new BookType(
                        rs.getInt("ID"),
                        rs.getString("NAME")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            MainWindowController.showInfo("Кто-то чё-то плохо закодил, " +
                    "и combobox с типами книг не отработал нормально", Alert.AlertType.WARNING);
        } finally {
            MainWindowController.closeRsAndStmt(rs, stmt);
        }
        newBookTypeCB.setItems(types);
    }

    private void loadBooks() {
        String query = "SELECT B.ID, B.NAME AS BOOK_NAME, B.CNT, BT.NAME AS TYPE_NAME " +
                "FROM BOOKS B JOIN BOOK_TYPES BT on B.TYPE_ID = BT.ID";
        Statement stmt = null;
        ResultSet rs = null;
        try {
            stmt = connection.createStatement();
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
            MainWindowController.showInfo("Кто-то чё-то плохо закодил, " +
                    "и книжки не смогли загрузиться нормально", Alert.AlertType.WARNING);
        } finally {
            MainWindowController.closeRsAndStmt(rs, stmt);
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
//        newBookTypeCB.getSelectionModel().clearSelection();
    }

    @FXML
    void initialize() {
        idColumn.setCellValueFactory(new PropertyValueFactory<Book, Integer>("id"));
//        idColumn.setVisible(false);
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
                    MainWindowController.showInfo("Неверный ввод", Alert.AlertType.WARNING);
                    return;
                }
                String query = "INSERT INTO BOOKS (NAME, CNT, TYPE_ID) VALUES (?, ?, ?)";
                PreparedStatement pstmt = null;
                try {
                    pstmt = connection.prepareStatement(query);
                    pstmt.setString(1, newBookName.getText());
                    pstmt.setInt(2, count);
                    pstmt.setInt(3, selectedBookTypeId);
                    pstmt.executeUpdate();
                } catch (SQLException e) {
                    e.printStackTrace();
                    return;
                } finally {
                    if (pstmt != null) {
                        try {
                            pstmt.close();
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                }
                MainWindowController.showInfo("Книга успешно добавлена", Alert.AlertType.INFORMATION);
                clearInput();
                fillTable();
            } else {
                MainWindowController.showInfo("Необходимо заполнить все поля", Alert.AlertType.WARNING);
            }
        });

        deleteBtn.setOnAction(actionEvent -> {
            Book selectedBook = booksTable.getSelectionModel().getSelectedItem();
            if (selectedBook != null) {
                int bookId = selectedBook.getId();
                String query = "DELETE FROM BOOKS WHERE ID=" + bookId;
                Statement stmt = null;
                try {
                    stmt = connection.createStatement();
                    stmt.executeQuery(query);
                    fillTable();
                    MainWindowController.showInfo("Книга успешно удалена", Alert.AlertType.INFORMATION);
                } catch (SQLIntegrityConstraintViolationException e) {
                    MainWindowController.showInfo("Упси, эту книгу нельзя удалить, " +
                            "так как в журнале есть записи о ней", Alert.AlertType.WARNING);
                } catch (SQLException e) {
                    e.printStackTrace();
                    MainWindowController.showInfo(e.getMessage(), Alert.AlertType.WARNING);
                } finally {
                    if (stmt != null) {
                        try {
                            stmt.close();
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                }
            } else {
                MainWindowController.showInfo("Ничего не выбрано", Alert.AlertType.WARNING);
            }
        });

        refreshBtn.setOnAction(actionEvent -> {
            fillTable();
        });
    }
}
