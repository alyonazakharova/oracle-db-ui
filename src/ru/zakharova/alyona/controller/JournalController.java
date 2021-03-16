package ru.zakharova.alyona.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
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

    @FXML
    private Button refreshBtn;

    private final Connection connection;
    private int selectedBookId;
    private int selectedClientId;
    private final SimpleDateFormat format = new SimpleDateFormat("dd-MM-yy");
    private final ObservableList<JournalRecord> records = FXCollections.observableArrayList();

    public JournalController() {
        this.connection = MainWindowController.connection;
    }

    void initClientCB() {
        ObservableList<Client> clients = FXCollections.observableArrayList();
        String query = "SELECT ID, LAST_NAME, FIRST_NAME, FATHER_NAME FROM CLIENTS";
        Statement stmt = null;
        ResultSet rs = null;
        try {
            stmt = connection.createStatement();
            rs = stmt.executeQuery(query);
            while (rs.next()) {
                clients.add(new Client(rs.getInt("ID"),
                        rs.getString("LAST_NAME"),
                        rs.getString("FIRST_NAME"),
                        rs.getString("FATHER_NAME")));
            }
            clientCB.setItems(clients);
        } catch (SQLException e) {
            e.printStackTrace();
            MainWindowController.showInfo("Кто-то чё-то плохо закодил, " +
                    "и combobox с клиентами не отработал нормально", Alert.AlertType.WARNING);
        } finally {
            MainWindowController.closeRsAndStmt(rs, stmt);
        }
    }

    void initBookCB() {
        ObservableList<Book> books = FXCollections.observableArrayList();
        String query = "SELECT ID, NAME FROM BOOKS";
        Statement stmt = null;
        ResultSet rs = null;
        try {
            stmt = connection.createStatement();
            rs = stmt.executeQuery(query);
            while (rs.next()) {
                books.add(new Book(rs.getInt("ID"), rs.getString("NAME")));
            }
            bookCB.setItems(books);
        } catch (SQLException e) {
            e.printStackTrace();
            MainWindowController.showInfo("Кто-то чё-то плохо закодил, " +
                    "и combobox с книгами не отработал нормально", Alert.AlertType.WARNING);
        } finally {
            MainWindowController.closeRsAndStmt(rs, stmt);
        }
    }

    private void loadRecords() {
        String query = "SELECT J.ID, LAST_NAME, FIRST_NAME, NAME, DATE_BEG, " +
                "DATE_END, DATE_RET FROM JOURNAL J JOIN CLIENTS C ON J.CLIENT_ID=C.ID " +
                "JOIN BOOKS B ON J.BOOK_ID=B.ID";
        Statement stmt = null;
        ResultSet rs = null;
        try {
            stmt = connection.createStatement();
            rs = stmt.executeQuery(query);
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
        } catch (SQLException e) {
            e.printStackTrace();
            MainWindowController.showInfo("Кто-то чё-то плохо закодил, " +
                    "и записи журнала не загрузились нормально", Alert.AlertType.WARNING);
        } finally {
            MainWindowController.closeRsAndStmt(rs, stmt);
        }
    }

    private void fillTable() {
        records.clear();
        loadRecords();
        journalTable.setItems(records);
    }

    private int getBookId(int recordId) {
        String query = "SELECT BOOK_ID FROM JOURNAL WHERE ID=" + recordId;
        Statement stmt = null;
        ResultSet rs = null;
        int bookId = -1;
        try {
            rs = connection.createStatement().executeQuery(query);
            if (rs.next()) {
                bookId = rs.getInt("BOOK_ID");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            MainWindowController.showInfo(e.getMessage(), Alert.AlertType.WARNING);
        } finally {
            MainWindowController.closeRsAndStmt(rs, stmt);
        }
        return bookId;
    }

    private int getDaysCount(int bookId) {
        String query = "SELECT DAY_COUNT FROM BOOKS B JOIN BOOK_TYPES BT" +
                " ON B.TYPE_ID=BT.ID WHERE B.ID=" + bookId;
        Statement stmt = null;
        ResultSet rs = null;
        int days = -1;
        try {
            stmt = connection.createStatement();
            rs = stmt.executeQuery(query);
            if (rs.next()) {
                days = rs.getInt("DAY_COUNT");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            MainWindowController.showInfo(e.getMessage(), Alert.AlertType.WARNING);
        } finally {
            MainWindowController.closeRsAndStmt(rs, stmt);
        }
        return days;
    }

    private int getBookCount(int bookId) {
        String query = "SELECT CNT FROM BOOKS WHERE ID=" + bookId;
        Statement stmt = null;
        ResultSet rs = null;
        int cnt = -1;
        try {
            stmt = connection.createStatement();
            rs = stmt.executeQuery(query);
            if (rs.next()) {
                cnt = rs.getInt("CNT");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            MainWindowController.showInfo(e.getMessage(), Alert.AlertType.WARNING);
        } finally {
            MainWindowController.closeRsAndStmt(rs, stmt);
        }
        return cnt;
    }

    private double calculateFine(int recordId, int bookId) {
        double fine = -1.0;
        String query = "SELECT FINE FROM BOOKS B JOIN BOOK_TYPES BT " +
                "ON BT.ID = B.TYPE_ID WHERE B.ID=" + bookId;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            stmt = connection.createStatement();
            rs = stmt.executeQuery(query);
            if (rs.next()) {
                double oneDayFine = rs.getDouble("FINE");
                query = "SELECT TRUNC(DATE_RET)-TRUNC(DATE_END) AS DAYS FROM JOURNAL WHERE ID=" + recordId;
                rs = connection.createStatement().executeQuery(query);
                if (rs.next()) {
                    int days = rs.getInt("DAYS");
                    fine = oneDayFine * days;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            MainWindowController.showInfo(e.getMessage(), Alert.AlertType.WARNING);
        } finally {
            MainWindowController.closeRsAndStmt(rs, stmt);
        }
        return fine;
    }

    private void updateBookCount(int bookId, boolean increment) {
        String query;
        if (increment) {
            query = "UPDATE BOOKS SET CNT=CNT+1 WHERE ID=" + bookId;
        } else {
            query = "UPDATE BOOKS SET CNT=CNT-1 WHERE ID=" + bookId;
        }
        Statement stmt = null;
        try {
            stmt = connection.createStatement();
            stmt.executeQuery(query);
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
    }

    @FXML
    void initialize() {
        idColumn.setCellValueFactory(new PropertyValueFactory<JournalRecord, Integer>("id"));
//        idColumn.setVisible(false);
        lastName.setCellValueFactory(new PropertyValueFactory<JournalRecord, String>("lastName"));
        firstName.setCellValueFactory(new PropertyValueFactory<JournalRecord, String>("firstName"));
        bookName.setCellValueFactory(new PropertyValueFactory<JournalRecord, String>("bookName"));
        dateBeg.setCellValueFactory(new PropertyValueFactory<JournalRecord, java.sql.Date>("dateBegin"));
        dateEnd.setCellValueFactory(new PropertyValueFactory<JournalRecord, java.sql.Date>("dateEnd"));
        dateRet.setCellValueFactory(new PropertyValueFactory<JournalRecord, java.sql.Date>("dateReturn"));

        initClientCB();
        initBookCB();
        fillTable();

        clientCB.setOnAction(actionEvent -> {
            selectedClientId = clientCB.getValue().getId();
        });

        bookCB.setOnAction(actionEvent -> {
            selectedBookId = bookCB.getValue().getId();
        });

        okBtn.setOnAction(actionEvent -> {
            if (clientCB.getValue() != null & bookCB.getValue() != null) {
                if (getBookCount(selectedBookId) <= 0) {
                    MainWindowController.showInfo("Ошибка. Книги нет в наличии", Alert.AlertType.WARNING);
                    return;
                }
                Date today = new Date();
                java.sql.Date beginDate = new java.sql.Date(today.getTime());
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(today);
                calendar.add(Calendar.DAY_OF_MONTH, getDaysCount(selectedBookId));
                java.sql.Date endDate = new java.sql.Date(calendar.getTimeInMillis());

                String query = "INSERT INTO JOURNAL (BOOK_ID, CLIENT_ID, DATE_BEG, DATE_END) VALUES (?, ?, ?, ?)";
                PreparedStatement pstmt = null;
                try {
                    pstmt = connection.prepareStatement(query);
                    pstmt.setInt(1, selectedBookId);
                    pstmt.setInt(2, selectedClientId);
                    pstmt.setDate(3, beginDate);
                    pstmt.setDate(4, endDate);
                    pstmt.executeUpdate();

                    updateBookCount(selectedBookId, false);
                    fillTable();
                } catch (SQLException e) {
                    e.printStackTrace();
                    MainWindowController.showInfo(e.getMessage(), Alert.AlertType.WARNING);
                } finally {
                    MainWindowController.closePstmt(pstmt);
                }
            } else {
                MainWindowController.showInfo("Выберите клиента и книгу", Alert.AlertType.WARNING);
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
                        connection.createStatement().executeQuery(query);
                        fillTable();
                        int bookId = getBookId(recordId);
                        updateBookCount(bookId, true);
                        MainWindowController.showInfo("ОК. Книга сдана вовремя", Alert.AlertType.INFORMATION);
                        if (new java.sql.Date(new Date().getTime()).after(record.getDateEnd())) {
                            double fine = calculateFine(recordId, bookId);
                            MainWindowController.showInfo("Сдача просрочена! " +
                                    "Необходимо оплатить штраф: " + fine + " руб.",
                                    Alert.AlertType.WARNING);
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                        MainWindowController.showInfo(e.getMessage(), Alert.AlertType.WARNING);
                    }
                } else {
                    MainWindowController.showInfo("Книга уже приянта", Alert.AlertType.WARNING);
                }
            } else {
                MainWindowController.showInfo("Ничего не выбрано", Alert.AlertType.WARNING);
            }
        });

        // Refresh combobox items after modification
        // of clients or books tables on other tabs
        refreshBtn.setOnAction(actionEvent -> {
            initClientCB();
            initBookCB();
        });
    }
}
