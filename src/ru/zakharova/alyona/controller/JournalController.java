package ru.zakharova.alyona.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import oracle.jdbc.OracleCallableStatement;
import oracle.jdbc.OracleTypes;
import ru.zakharova.alyona.Helper;
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

//    private final Connection connection;
    private int selectedBookId;
    private int selectedClientId;
    private final SimpleDateFormat format = new SimpleDateFormat("dd-MM-yy");
    private final ObservableList<JournalRecord> records = FXCollections.observableArrayList();

//    public JournalController() {
//        this.connection = LoginController.connection;
//    }

    void initClientCB() {
        ObservableList<Client> clients = FXCollections.observableArrayList();
        String query = "SELECT ID, LAST_NAME, FIRST_NAME, FATHER_NAME FROM CLIENTS";
        Statement stmt = null;
        ResultSet rs = null;
        try {
            stmt = LoginController.connection.createStatement();
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
            Helper.showInfo(e.getMessage(), Alert.AlertType.WARNING);
        } finally {
            Helper.closeRsAndStmt(rs, stmt);
        }
    }

    void initBookCB() {
        ObservableList<Book> books = FXCollections.observableArrayList();
        String query = "SELECT ID, NAME FROM BOOKS";
        Statement stmt = null;
        ResultSet rs = null;
        try {
            stmt = LoginController.connection.createStatement();
            rs = stmt.executeQuery(query);
            while (rs.next()) {
                books.add(new Book(rs.getInt("ID"), rs.getString("NAME")));
            }
            bookCB.setItems(books);
        } catch (SQLException e) {
            e.printStackTrace();
            Helper.showInfo(e.getMessage(), Alert.AlertType.WARNING);
        } finally {
            Helper.closeRsAndStmt(rs, stmt);
        }
    }

    private void loadRecords() {
        String query = "SELECT J.ID, LAST_NAME, FIRST_NAME, NAME, DATE_BEG, " +
                "DATE_END, DATE_RET FROM JOURNAL J JOIN CLIENTS C ON J.CLIENT_ID=C.ID " +
                "JOIN BOOKS B ON J.BOOK_ID=B.ID";
        Statement stmt = null;
        ResultSet rs = null;
        try {
            stmt = LoginController.connection.createStatement();
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
            Helper.showInfo(e.getMessage(), Alert.AlertType.WARNING);
        } finally {
            Helper.closeRsAndStmt(rs, stmt);
        }
    }

    private void fillTable() {
        records.clear();
        loadRecords();
        journalTable.setItems(records);
    }

    private int getDaysCount(int bookId) {
        String query = "SELECT DAY_COUNT FROM BOOKS B JOIN BOOK_TYPES BT" +
                " ON B.TYPE_ID=BT.ID WHERE B.ID=" + bookId;
        Statement stmt = null;
        ResultSet rs = null;
        int days = -1;
        try {
            stmt = LoginController.connection.createStatement();
            rs = stmt.executeQuery(query);
            if (rs.next()) {
                days = rs.getInt("DAY_COUNT");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Helper.showInfo(e.getMessage(), Alert.AlertType.WARNING);
        } finally {
            Helper.closeRsAndStmt(rs, stmt);
        }
        return days;
    }


    @FXML
    void initialize() {
        idColumn.setCellValueFactory(new PropertyValueFactory<JournalRecord, Integer>("id"));
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
                Date today = new Date();
                java.sql.Date beginDate = new java.sql.Date(today.getTime());
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(today);
                calendar.add(Calendar.DAY_OF_MONTH, getDaysCount(selectedBookId));
//                calendar.add(Calendar.DAY_OF_MONTH, -10);
                java.sql.Date endDate = new java.sql.Date(calendar.getTimeInMillis());

                String query = "INSERT INTO JOURNAL (BOOK_ID, CLIENT_ID, DATE_BEG, DATE_END) VALUES (?, ?, ?, ?)";
                PreparedStatement pstmt = null;
                try {
                    pstmt = LoginController.connection.prepareStatement(query);
                    pstmt.setInt(1, selectedBookId);
                    pstmt.setInt(2, selectedClientId);
                    pstmt.setDate(3, beginDate);
                    pstmt.setDate(4, endDate);
//                    pstmt.setDate(3, endDate);
//                    pstmt.setDate(4, endDate);
                    pstmt.executeUpdate();
                    fillTable();
                } catch (SQLException e)
                {
                    // на руках больше 10 книг либо книги нет в наличии
                    Helper.showInfo(e.getMessage(), Alert.AlertType.WARNING);
                } finally {
                    Helper.closePstmt(pstmt);
                }
            } else {
                Helper.showInfo("Выберите клиента и книгу", Alert.AlertType.WARNING);
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
                        LoginController.connection.createStatement().executeQuery(query);
                        fillTable();
                        if (new java.sql.Date(new Date().getTime()).after(record.getDateEnd())) {
                            int fine = -1;
                            try {
                                CallableStatement cs = LoginController.connection.prepareCall("{call CALCULATE_FINE(?, ?)}");
                                cs.setInt(1, recordId);
                                cs.registerOutParameter(2, OracleTypes.NUMBER);
                                cs.executeQuery();
                                fine = cs.getInt(2);
                            } catch (SQLException e) {
                                Helper.showInfo(e.getMessage(), Alert.AlertType.WARNING);
                            }
                            Helper.showInfo("Сдача просрочена! " +
                                    "Необходимо оплатить штраф: " + fine + " руб.",
                                    Alert.AlertType.WARNING);
                        } else {
                            Helper.showInfo("Книга сдана вовремя", Alert.AlertType.INFORMATION);
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                        Helper.showInfo(e.getMessage(), Alert.AlertType.WARNING);
                    }
                } else {
                    Helper.showInfo("Книга уже приянта", Alert.AlertType.WARNING);
                }
            } else {
                Helper.showInfo("Ничего не выбрано", Alert.AlertType.WARNING);
            }
        });

        // Обновление содержимого комбобоксов при изменении данных на вкладке Клиенты или Книги
        refreshBtn.setOnAction(actionEvent -> {
            initClientCB();
            initBookCB();
        });
    }
}
