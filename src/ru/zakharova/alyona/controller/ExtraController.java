package ru.zakharova.alyona.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.stage.Stage;
import oracle.jdbc.OracleCallableStatement;
import oracle.jdbc.OracleType;
import oracle.jdbc.OracleTypes;
import ru.zakharova.alyona.Helper;
import ru.zakharova.alyona.dto.Client;

import java.io.IOException;
import java.sql.*;
import java.time.ZoneId;

public class ExtraController {

    @FXML
    private ComboBox<Client> clientsCB;

    @FXML
    private Button checkBooksBtn;

    @FXML
    private Button checkFineBtn;

    @FXML
    private Button biggestFineBtn;

    @FXML
    private Button popularBooksBtn;

    @FXML
    private Button exitBtn;

    @FXML
    private DatePicker dp1;

    @FXML
    private DatePicker dp2;

    @FXML
    private Button periodFineBtn;

//    private final Connection connection;
//
//    public ExtraController() {
//        this.connection = LoginController.connection;
//    }

    private void initClientsCB() {
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
            clientsCB.setItems(clients);
        } catch (SQLException e) {
            e.printStackTrace();
            Helper.showInfo(e.getMessage(), Alert.AlertType.WARNING);
        } finally {
            Helper.closeRsAndStmt(rs, stmt);
        }
    }

    @FXML
    void initialize() {
        initClientsCB();

        checkBooksBtn.setOnAction(actionEvent -> {
            Client selectedClient = clientsCB.getSelectionModel().getSelectedItem();
            if (selectedClient != null) {
                int clientId = selectedClient.getId();
                try {
                    CallableStatement cs = LoginController.connection.prepareCall("{ call BOOKS_NOT_RETURNED(?, ?)}");
                    cs.setInt(1, clientId);
                    cs.registerOutParameter(2, OracleType.NUMBER);
                    cs.executeQuery();
                    Helper.showInfo("Количество книг на руках у выбранного клиента: " +
                                cs.getInt(2), Alert.AlertType.INFORMATION);
                } catch (SQLException e) {
                    e.printStackTrace();
                    Helper.showInfo(e.getMessage(), Alert.AlertType.WARNING);
                }
            } else {
                Helper.showInfo("Ничего не выбрано", Alert.AlertType.WARNING);
            }
        });

        checkFineBtn.setOnAction(actionEvent -> {
            Client selectedClient = clientsCB.getSelectionModel().getSelectedItem();
            if (selectedClient != null) {
                int clientId = selectedClient.getId();
                try {
                    CallableStatement cs = LoginController.connection.prepareCall("{ call CLIENTS_FINE(?, ?)}");
                    cs.setInt(1, clientId);
                    cs.registerOutParameter(2, OracleType.NUMBER);
                    cs.executeQuery();
                    Helper.showInfo("Штраф выбранного клиента составляет: " +
                            + cs.getInt(2) + " рублей", Alert.AlertType.INFORMATION);
                } catch (SQLException e) {
                    e.printStackTrace();
                    Helper.showInfo(e.getMessage(), Alert.AlertType.WARNING);
                }
            } else {
                Helper.showInfo("Ничего не выбрано", Alert.AlertType.WARNING);
            }
        });

        biggestFineBtn.setOnAction(actionEvent -> {
            try {
                CallableStatement cs = LoginController.connection.prepareCall("{ call BIGGEST_FINE(?)}");
                cs.registerOutParameter(1, OracleType.NUMBER);
                cs.executeQuery();
                Helper.showInfo("Максимальный штраф составляет: "
                        + cs.getInt(1) + " рублей", Alert.AlertType.INFORMATION);
            } catch (SQLException e) {
                e.printStackTrace();
                Helper.showInfo(e.getMessage(), Alert.AlertType.WARNING);
            }
        });

        popularBooksBtn.setOnAction(actionEvent -> {
            try {
                CallableStatement cs = LoginController.connection.prepareCall("{call POPULAR_BOOKS(?)}");
                cs.registerOutParameter(1, OracleTypes.CURSOR);
                cs.execute();
                ResultSet rs = ((OracleCallableStatement)cs).getCursor(1);
                StringBuilder stringBuilder = new StringBuilder("3 самые популярные книги:\n");
                while (rs.next()) {
                    stringBuilder.append(rs.getString("NAME")).append("\n");
                }
                Helper.showInfo(stringBuilder.toString(), Alert.AlertType.INFORMATION);
            } catch (SQLException e) {
                e.printStackTrace();
                Helper.showInfo(e.getMessage(), Alert.AlertType.WARNING);
            }
        });

        periodFineBtn.setOnAction(actionEvent -> {
            if (dp1.getValue() != null & dp2.getValue() != null) {
                Date date1 = new Date(Date.from(dp1.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant()).getTime());
                Date date2 = new Date(Date.from(dp2.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant()).getTime());
            try {
                CallableStatement cs = LoginController.connection.prepareCall("{call FINE_SUM(?, ?, ?)}");
                cs.setDate(1, date1);
                cs.setDate(2, date2);
                cs.registerOutParameter(3, OracleTypes.NUMBER);
                cs.executeQuery();
                Helper.showInfo("Штрафы за заданный период времени составляют: " +
                        cs.getInt(3) + " рублей", Alert.AlertType.INFORMATION);
            } catch (SQLException e) {
                e.printStackTrace();
                Helper.showInfo(e.getMessage(), Alert.AlertType.WARNING);
            }
            } else {
                Helper.showInfo("Укажите даты", Alert.AlertType.WARNING);
            }
        });

        exitBtn.setOnAction(actionEvent -> {
            exitBtn.getScene().getWindow().hide();
            try {
                LoginController.connection.close();
                Parent root = FXMLLoader.load(getClass().getResource("../resources/login.fxml"));
                Stage stage = new Stage();
                stage.setTitle("Вход");
                stage.setScene(new Scene(root));
                stage.show();
            } catch (IOException | SQLException e) {
                e.printStackTrace();
            }
        });
    }

}
