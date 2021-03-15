package ru.zakharova.alyona.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import ru.zakharova.alyona.dto.Book;
import ru.zakharova.alyona.dto.Client;
import ru.zakharova.alyona.dto.JournalRecord;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class JournalController {

    @FXML
    private TableView<JournalRecord> journalTable;

    @FXML
    private TableColumn<JournalRecord, Integer> idColumn;

    @FXML
    private TableColumn<JournalRecord, String> lastName;

    @FXML
    private TableColumn<JournalRecord, String> firstName;

    @FXML
    private TableColumn<JournalRecord, String> bookName;

    @FXML
    private TableColumn<JournalRecord, java.sql.Date> dateBeg;

    @FXML
    private TableColumn<JournalRecord, java.sql.Date> dateEnd;

    @FXML
    private TableColumn<JournalRecord, java.sql.Date> dateRet;

    @FXML
    private ComboBox<Client> clientCB;

    @FXML
    private ComboBox<Book> bookCB;

    @FXML
    private Button okBtn;

    @FXML
    private Button returnBtn;

//    public static Connection connection;

    private int selectedBookId;

    private int selectedClientId;

    private final SimpleDateFormat format = new SimpleDateFormat("dd-MM-yy");

    private final ObservableList<JournalRecord> records = FXCollections.observableArrayList();

    void initClientCB() throws SQLException {
        ObservableList<Client> clients = FXCollections.observableArrayList();
        ResultSet rs = MainWindowController.connection.createStatement().executeQuery(
                "SELECT ID, LAST_NAME, FIRST_NAME, FATHER_NAME FROM CLIENTS");
        while (rs.next()) {
            clients.add(new Client(rs.getInt("ID"),
                    rs.getString("LAST_NAME"),
                    rs.getString("FIRST_NAME"),
                    rs.getString("FATHER_NAME")));
        }
        clientCB.setItems(clients);
    }

    void initBookCB() throws SQLException {
        ObservableList<Book> books = FXCollections.observableArrayList();
        ResultSet rs = MainWindowController.connection.createStatement().executeQuery(
                "SELECT ID, NAME FROM BOOKS");
        while (rs.next()) {
            books.add(new Book(rs.getInt("ID"), rs.getString("NAME")));
        }
        bookCB.setItems(books);
    }

    private int getDaysCount(int bookId) {
        try {
            ResultSet rs = MainWindowController.connection.createStatement().executeQuery(
                    "SELECT DAY_COUNT FROM BOOKS B JOIN BOOK_TYPES BT" +
                            " ON B.TYPE_ID=BT.ID WHERE B.ID=" + bookId);
            if (rs.next()) {
                return rs.getInt("DAY_COUNT");
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return -1;
    }

    private void loadRecords() throws SQLException {
        String query = "SELECT J.ID, LAST_NAME, FIRST_NAME, NAME, DATE_BEG, " +
                "DATE_END, DATE_RET FROM JOURNAL J JOIN CLIENTS C ON J.CLIENT_ID=C.ID " +
                "JOIN BOOKS B ON J.BOOK_ID=B.ID";

        Statement statement = MainWindowController.connection.createStatement();
        ResultSet rs = statement.executeQuery(query);
        while (rs.next()) {
            JournalRecord newRecord = new JournalRecord(
                    rs.getInt("ID"),
                    rs.getString("LAST_NAME"),
                    rs.getString("FIRST_NAME"),
                    rs.getString("NAME"),
                    rs.getDate("DATE_BEG"),
                    rs.getDate("DATE_END"),
                    rs.getDate("DATE_RET")
            );
            records.add(newRecord);
        }
        rs.close();
        statement.close();
    }

    private void initColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<JournalRecord, Integer>("id"));
//        idColumn.setVisible(false);
        lastName.setCellValueFactory(new PropertyValueFactory<JournalRecord, String>("lastName"));
        firstName.setCellValueFactory(new PropertyValueFactory<JournalRecord, String>("firstName"));
        bookName.setCellValueFactory(new PropertyValueFactory<JournalRecord, String>("bookName"));
        dateBeg.setCellValueFactory(new PropertyValueFactory<JournalRecord, java.sql.Date>("dateBegin"));
        dateEnd.setCellValueFactory(new PropertyValueFactory<JournalRecord, java.sql.Date>("dateEnd"));
        dateRet.setCellValueFactory(new PropertyValueFactory<JournalRecord, java.sql.Date>("dateReturn"));
    }

    private void fillTable() throws SQLException {
        records.clear();
        loadRecords();
        journalTable.setItems(records);
    }

    private int getBookId(int recordId) throws SQLException {
        String query = "SELECT BOOK_ID FROM JOURNAL WHERE ID=" + recordId;
        Statement statement = MainWindowController.connection.createStatement();
        ResultSet rs = statement.executeQuery(query);
        int bookId = -1;
        if (rs.next()) {
            bookId = rs.getInt("BOOK_ID");
        }
        rs.close();
        statement.close();
        return bookId;
    }

    private double calculateFine(int recordId, int bookId) throws SQLException {
        double fine = -1.0;
        String query = "SELECT FINE FROM BOOKS B JOIN BOOK_TYPES BT ON BT.ID = B.TYPE_ID WHERE B.ID=" + bookId;
        Statement statement = MainWindowController.connection.createStatement();
        ResultSet rs = statement.executeQuery(query);
        if (rs.next()) {
            double oneDayFine = rs.getDouble("FINE");
            query = "SELECT TRUNC(DATE_RET)-TRUNC(DATE_END) AS DAYS FROM JOURNAL WHERE ID=" + recordId;
            statement = MainWindowController.connection.createStatement();
            rs = statement.executeQuery(query);
            if (rs.next()) {
                int days = rs.getInt("DAYS");
                System.out.println(oneDayFine + " - for " + days);
                fine = oneDayFine * days;
            }
        }
        rs.close();
        statement.close();
        return fine;
    }

    private void incBookCount(int bookId) throws SQLException {
        String query = "UPDATE BOOKS SET CNT=CNT+1 WHERE ID=" + bookId;
        MainWindowController.connection.createStatement().executeQuery(query);
    }

    private void decBookCount(int bookId) throws SQLException {
        String query = "UPDATE BOOKS SET CNT=CNT-1 WHERE ID=" + bookId;
        MainWindowController.connection.createStatement().executeQuery(query);
    }

    @FXML
    void initialize() {
        try {
            initClientCB();
            initBookCB();
            initColumns();
            fillTable();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        clientCB.setOnAction(actionEvent -> {
            selectedClientId = clientCB.getValue().getId();
        });

        bookCB.setOnAction(actionEvent -> {
            selectedBookId = bookCB.getValue().getId();
        });

        okBtn.setOnAction(actionEvent -> {
            if (clientCB.getValue() != null & bookCB.getValue() != null) {
                int daysCount = getDaysCount(selectedBookId);
                Date currentDate = new Date();
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(currentDate);
                calendar.add(Calendar.DAY_OF_MONTH, daysCount);
                Date dateEnd = calendar.getTime();
                try {
                    Statement stmt = MainWindowController.connection.createStatement();
                    String query = "INSERT INTO JOURNAL (BOOK_ID, CLIENT_ID, DATE_BEG, DATE_END) VALUES (?, ?, ?, ?)";
                    PreparedStatement pstmt = MainWindowController.connection.prepareStatement(query);
                    pstmt.setInt(1, selectedBookId);
                    pstmt.setInt(2, selectedClientId);
                    pstmt.setDate(3, new java.sql.Date(currentDate.getTime()));
                    pstmt.setDate(4, new java.sql.Date(dateEnd.getTime()));
                    pstmt.executeUpdate();
                    System.out.println(selectedBookId + " - " + selectedClientId);
                    decBookCount(selectedBookId);
                    fillTable();
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }

                System.out.println(getDaysCount(selectedBookId));
            } else {
                System.out.println("Выберите клиента и книгу");
            }
        });

        returnBtn.setOnAction(actionEvent -> {
            JournalRecord record = journalTable.getSelectionModel().getSelectedItem();
            if (record != null) {
                if (record.getDateReturn() == null) {
                    String date = format.format(new Date());
                    int recordId = record.getId();
                    String query = "UPDATE JOURNAL SET DATE_RET='" + date +
                            "' WHERE ID=" + recordId;
                    try {
                        MainWindowController.connection.createStatement().executeQuery(query);
                        fillTable();
                        int bookId = getBookId(recordId);
                        incBookCount(bookId);

                        if (new java.sql.Date(new Date().getTime()).after(record.getDateEnd())) {
                            System.out.println("FINE = " + calculateFine(recordId, bookId));
                        }

                    } catch (SQLException throwables) {
                        throwables.printStackTrace();
                    }
                } else {
                    System.out.println("КНИГА УЖЕ ПРИНЯТА");
                }
            } else {
                System.out.println("НИЧЕГО НЕ ВЫБРАНО");
            }
        });
    }
}
