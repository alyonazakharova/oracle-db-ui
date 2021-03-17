package ru.zakharova.alyona.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import ru.zakharova.alyona.Helper;
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

    @FXML
    private Button updateBtn;

    @FXML
    private Button deleteBtn;

    private final Connection connection;
    private final ObservableList<Client> clients = FXCollections.observableArrayList();
    private int selectedForUpdateClientId = -1;

    public ClientsController() {
        this.connection = LoginController.connection;
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
            Helper.showInfo("Кто-то чё-то плохо закодил, " +
                    "и клиенты не смогли загрузиться нормально", Alert.AlertType.WARNING);
        } finally {
            Helper.closeRsAndStmt(rs, stmt);
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

                        Helper.showInfo("Клиент успешно добавлен", Alert.AlertType.INFORMATION);
                        clearInput();
                        fillTable();
                    } catch (SQLException e) {
//                        error with duplicated passport
                        Helper.showInfo(e.getMessage(), Alert.AlertType.WARNING);
                    } finally {
                        Helper.closePstmt(pstmt);
                    }
                } else {
                    Helper.showInfo("Проверьте ввод", Alert.AlertType.WARNING);
                }
            }
        });

        clientsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            Client selectedClient = clientsTable.getSelectionModel().getSelectedItem();
            if (selectedClient == null) {
                clearInput();
                return;
            }
            selectedForUpdateClientId = selectedClient.getId();
            lastNameField.setText(selectedClient.getLastName());
            firstNameField.setText(selectedClient.getFirstName());
            fatherNameField.setText(selectedClient.getFatherName());
            passSeriaField.setText(selectedClient.getPassSeria());
            passNumField.setText(selectedClient.getPassNum());
        });

        updateBtn.setOnAction(actionEvent -> {
            if (clientsTable.getSelectionModel().getSelectedItem() == null) {
                Helper.showInfo("Ничего не выбрано", Alert.AlertType.WARNING);
            }
            String query = "UPDATE CLIENTS SET LAST_NAME='" + lastNameField.getText() +
                    "', FIRST_NAME='" + firstNameField.getText() +
                    "', FATHER_NAME='" + fatherNameField.getText() +
                    "', PASSPORT_SERIA='" + passSeriaField.getText() +
                    "', PASSPORT_NUM='" + passNumField.getText() +
                    "' WHERE ID=" + selectedForUpdateClientId;
            Statement stmt = null;
            try {
                stmt = connection.createStatement();
                stmt.executeQuery(query);
                fillTable();
                Helper.showInfo("Данные обновлены", Alert.AlertType.INFORMATION);
                fillTable();
            } catch (SQLException e) {
                e.printStackTrace();
                Helper.showInfo(e.getMessage(), Alert.AlertType.WARNING);
            } finally {
                if (stmt != null) {
                    try {
                        stmt.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        deleteBtn.setOnAction(actionEvent -> {
            Client selectedClient = clientsTable.getSelectionModel().getSelectedItem();
            if (selectedClient != null) {
                int clientId = selectedClient.getId();
                String query = "DELETE FROM CLIENTS WHERE ID=" + clientId;
                Statement stmt = null;
                try {
                    stmt = connection.createStatement();
                    stmt.executeQuery(query);
                    fillTable();
                    Helper.showInfo("Выбранный тип удален", Alert.AlertType.INFORMATION);
                    fillTable();
                } catch (SQLIntegrityConstraintViolationException e) {
                    Helper.showInfo("Невозможно удалить запись, " +
                                    "потому что на нее ссылаются записи из другой таблицы",
                            Alert.AlertType.WARNING);
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
    }
}
