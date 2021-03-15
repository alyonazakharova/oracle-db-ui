package ru.zakharova.alyona.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
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

    private Connection connection;

    public ClientsController() {
        this.connection = MainWindowController.connection;
    }

    private final ObservableList<Client> clients = FXCollections.observableArrayList();

    private boolean isPassportOk(String seria, String num) {
        return seria.matches("\\d{4}") & num.matches("\\d{6}");
    }

    private void loadClients() throws SQLException {
        String query = "SELECT ID, LAST_NAME, FIRST_NAME, FATHER_NAME," +
                " PASSPORT_SERIA, PASSPORT_NUM FROM CLIENTS";
        Statement statement = connection.createStatement();
        ResultSet rs = statement.executeQuery(query);
        while (rs.next()) {
            clients.add(new Client(
                    rs.getInt("ID"),
                    rs.getString("LAST_NAME"),
                    rs.getString("FIRST_NAME"),
                    rs.getString("FATHER_NAME"),
                    rs.getString("PASSPORT_SERIA"),
                    rs.getString("PASSPORT_NUM")
            ));
        }
        rs.close();
        statement.close();
    }

    private void fillTable() throws SQLException {
        clients.clear();
        loadClients();
        clientsTable.setItems(clients);
    }

    private void initColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<Client, Integer>("id"));
        lastNameColumn.setCellValueFactory(new PropertyValueFactory<Client, String>("lastName"));
        firstNameColumn.setCellValueFactory(new PropertyValueFactory<Client, String>("firstName"));
        fatherNameColumn.setCellValueFactory(new PropertyValueFactory<Client, String>("fatherName"));
        passSeriaColumn.setCellValueFactory(new PropertyValueFactory<Client, String>("passSeria"));
        passNumColumn.setCellValueFactory(new PropertyValueFactory<Client, String>("passNum"));
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
//        try {
//            DriverManager.registerDriver(new oracle.jdbc.OracleDriver());
//            connection = DriverManager.getConnection(
//                    "jdbc:oracle:thin:@localhost:1521:XE",
//                    "c##myuser", "mypass");
//        } catch (SQLException e) {
//            e.printStackTrace();
//            return;
//        }

        initColumns();

        try {
            fillTable();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        addBtn.setOnAction(actionEvent -> {
            if (!lastNameField.getText().isEmpty()
                    & !firstNameField.getText().isEmpty()
                    & !fatherNameField.getText().isEmpty()
                    & !passSeriaField.getText().isEmpty()
                    & !passNumField.getText().isEmpty()) {
                String seria = passSeriaField.getText();
                String num = passNumField.getText();
                if (isPassportOk(seria, num)) {
                    try {
                        Statement stmt = connection.createStatement();
                        String query = "INSERT INTO CLIENTS (FIRST_NAME, LAST_NAME, FATHER_NAME, PASSPORT_SERIA, PASSPORT_NUM) VALUES (?, ?, ?, ?, ?)";
                        PreparedStatement pstmt = connection.prepareStatement(query);
                        pstmt.setString(1, firstNameField.getText());
                        pstmt.setString(2, lastNameField.getText());
                        pstmt.setString(3, fatherNameField.getText());
                        pstmt.setString(4, seria);
                        pstmt.setString(5, num);
                        pstmt.executeUpdate();
                        System.out.println("Новый клиент успешно добавлен");
                        clearInput();
                        fillTable();
//                        JournalController.initClientCB();
                    } catch (SQLException throwables) {
                        throwables.printStackTrace();
                    }
                } else {
                    System.out.println("Проверьте ввод");
                }
            }
        });
    }
}
