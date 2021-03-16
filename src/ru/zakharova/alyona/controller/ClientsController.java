package ru.zakharova.alyona.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import ru.zakharova.alyona.dto.Client;

import java.sql.*;

public class ClientsController {

    @FXML
    private TableView<Client> clientsTable;

    @FXML
    private TableColumn<Client, Integer> idColumn;

    @FXML
    private TableColumn<Client, String> lastNameColumn;

    @FXML
    private TableColumn<Client, String> firstNameColumn;

    @FXML
    private TableColumn<Client, String> fatherNameColumn;

    @FXML
    private TableColumn<Client, String> passSeriaColumn;

    @FXML
    private TableColumn<Client, String> passNumColumn;

    @FXML
    private TextField lastNameField;

    @FXML
    private TextField firstNameField;

    @FXML
    private TextField fatherNameField;

    @FXML
    private TextField passSeriaField;

    @FXML
    private TextField passNumField;

    @FXML
    private Button addBtn;

    private final Connection connection;
    private final ObservableList<Client> clients = FXCollections.observableArrayList();

    public ClientsController() {
        this.connection = MainWindowController.connection;
    }

    private boolean isPassportOk(String seria, String num) {
        return seria.matches("\\d{4}") & num.matches("\\d{6}");
    }

    private void loadClients() {
        String query = "SELECT ID, LAST_NAME, FIRST_NAME, FATHER_NAME," +
                " PASSPORT_SERIA, PASSPORT_NUM FROM CLIENTS";
        Statement stmt = null;
        ResultSet rs = null;
        try {
            stmt = connection.createStatement();
            rs = stmt.executeQuery(query);
            while (rs.next()) {
                clients.add(new Client(
                        rs.getInt("ID"),
                        rs.getString("LAST_NAME"),
                        rs.getString("FIRST_NAME"),
                        rs.getString("FATHER_NAME"),
                        rs.getString("PASSPORT_SERIA"),
                        rs.getString("PASSPORT_NUM")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            MainWindowController.showInfo("Кто-то чё-то плохо закодил, " +
                    "и клиенты не смогли загрузиться нормально", Alert.AlertType.WARNING);
        } finally {
            MainWindowController.closeRsAndStmt(rs, stmt);
        }
    }

    private void fillTable() {
        clients.clear();
        loadClients();
        clientsTable.setItems(clients);
    }

    private void clearInput() {
        lastNameField.setText("");
        firstNameField.setText("");
        fatherNameField.setText("");
        passSeriaField.setText("");
        passNumField.setText("");
    }


    @FXML
    void initialize() {
        idColumn.setCellValueFactory(new PropertyValueFactory<Client, Integer>("id"));
        lastNameColumn.setCellValueFactory(new PropertyValueFactory<Client, String>("lastName"));
        firstNameColumn.setCellValueFactory(new PropertyValueFactory<Client, String>("firstName"));
        fatherNameColumn.setCellValueFactory(new PropertyValueFactory<Client, String>("fatherName"));
        passSeriaColumn.setCellValueFactory(new PropertyValueFactory<Client, String>("passSeria"));
        passNumColumn.setCellValueFactory(new PropertyValueFactory<Client, String>("passNum"));

        fillTable();

        addBtn.setOnAction(actionEvent -> {
            if (!lastNameField.getText().isEmpty()
                    & !firstNameField.getText().isEmpty()
                    & !fatherNameField.getText().isEmpty()
                    & !passSeriaField.getText().isEmpty()
                    & !passNumField.getText().isEmpty()) {
                String seria = passSeriaField.getText();
                String num = passNumField.getText();
                if (isPassportOk(seria, num)) {
                    String query = "INSERT INTO CLIENTS " +
                            "(FIRST_NAME, LAST_NAME, FATHER_NAME, PASSPORT_SERIA, PASSPORT_NUM) " +
                            "VALUES (?, ?, ?, ?, ?)";
                    PreparedStatement pstmt = null;
                    try {
                        pstmt = connection.prepareStatement(query);
                        pstmt.setString(1, firstNameField.getText());
                        pstmt.setString(2, lastNameField.getText());
                        pstmt.setString(3, fatherNameField.getText());
                        pstmt.setString(4, seria);
                        pstmt.setString(5, num);
                        pstmt.executeUpdate();

                        MainWindowController.showInfo("Клиент успешно добавлен", Alert.AlertType.INFORMATION);
                        clearInput();
                        fillTable();
                    } catch (SQLException e) {
//                        error with duplicated passport
                        MainWindowController.showInfo(e.getMessage(), Alert.AlertType.WARNING);
                    } finally {
                        if (pstmt != null) {
                            try {
                                pstmt.close();
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                } else {
                    MainWindowController.showInfo("Проверьте ввод", Alert.AlertType.WARNING);
                }
            }
        });
    }
}
