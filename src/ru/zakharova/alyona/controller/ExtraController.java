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
import javafx.stage.Stage;
import ru.zakharova.alyona.Helper;
import ru.zakharova.alyona.dto.Client;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Handler;

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

    private Connection connection;

    public ExtraController() {
        this.connection = LoginController.connection;
    }

    private void initClientsCB() {
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
            clientsCB.setItems(clients);
        } catch (SQLException e) {
            e.printStackTrace();
            Helper.showInfo("Кто-то чё-то плохо закодил, " +
                    "и combobox с клиентами не отработал нормально", Alert.AlertType.WARNING);
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
                String query = "select count(ID) as N from JOURNAL where CLIENT_ID="
                        + clientId + " and DATE_RET is null";
                Statement stmt = null;
                ResultSet rs = null;
                try {
                    stmt = connection.createStatement();
                    rs = stmt.executeQuery(query);
                    if (rs.next()) {
                        int books = rs.getInt("N");
                        Helper.showInfo("Количество книг на руках у выбранного клиента: " +
                                books, Alert.AlertType.INFORMATION);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    Helper.showInfo(e.getMessage(), Alert.AlertType.WARNING);
                } finally {
                    Helper.closeRsAndStmt(rs, stmt);
                }
            } else {
                Helper.showInfo("Ничего не выбрано", Alert.AlertType.WARNING);
            }
        });

        checkFineBtn.setOnAction(actionEvent -> {
            Client selectedClient = clientsCB.getSelectionModel().getSelectedItem();
            if (selectedClient != null) {
                int clientId = selectedClient.getId();

                String query = "select sum(DAYS*FINE) as TOTAL_FINE " +
                        "from (select (trunc(DATE_RET) - trunc(DATE_END)) as DAYS, FINE " +
                        "from JOURNAL J join BOOKS B on B.ID = J.BOOK_ID join BOOK_TYPES BT on BT.ID = B.TYPE_ID " +
                        "where DATE_RET is not null and DATE_RET > J.DATE_END and CLIENT_ID=" + clientId +")";
                Statement stmt = null;
                ResultSet rs = null;
                try {
                    stmt = connection.createStatement();
                    rs = stmt.executeQuery(query);
                    if (rs.next()) {
                        int fine = rs.getInt("TOTAL_FINE");
                        Helper.showInfo("Штраф выбранного клиента составляет: " +
                                 + fine + " рублей", Alert.AlertType.INFORMATION);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    Helper.showInfo(e.getMessage(), Alert.AlertType.WARNING);
                } finally {
                    Helper.closeRsAndStmt(rs, stmt);
                }
            } else {
                Helper.showInfo("Ничего не выбрано", Alert.AlertType.WARNING);
            }
        });

        biggestFineBtn.setOnAction(actionEvent -> {
            String query = "select max(TOTAL_FINE) as RES from (select ((trunc(DATE_RET) - trunc(DATE_END)) * FINE)" +
                    " as TOTAL_FINE from JOURNAL J join BOOKS B on B.ID = J.BOOK_ID" +
                    " join BOOK_TYPES BT on BT.ID = B.TYPE_ID" +
                    " where DATE_RET is not null and DATE_RET > J.DATE_END)";
            Statement stmt = null;
            ResultSet rs = null;
            try {
                stmt = connection.createStatement();
                rs = stmt.executeQuery(query);
                if (rs.next()) {
                    int maxFine = rs.getInt("RES");
                    Helper.showInfo("Максимальный штраф составляет: "
                            + maxFine + " рублей", Alert.AlertType.INFORMATION);
                }
            } catch (SQLException e) {
                e.printStackTrace();
                Helper.showInfo(e.getMessage(), Alert.AlertType.WARNING);
            } finally {
                Helper.closeRsAndStmt(rs, stmt);
            }
        });

        popularBooksBtn.setOnAction(actionEvent -> {
            String query = "select B.NAME from JOURNAL J join BOOKS B on B.ID = J.BOOK_ID" +
                    " group by B.NAME order by count(J.BOOK_ID) desc fetch first 3 rows only";
            Statement stmt = null;
            ResultSet rs = null;
            try {
                stmt = connection.createStatement();
                rs = stmt.executeQuery(query);
                StringBuilder stringBuilder = new StringBuilder("3 самые популярные книги:\n");
                while (rs.next()) {
                    stringBuilder.append(rs.getString("NAME"));
                    stringBuilder.append("\n");
                }
                Helper.showInfo(stringBuilder.toString(), Alert.AlertType.INFORMATION);
            } catch (SQLException e) {
                e.printStackTrace();
                Helper.showInfo(e.getMessage(), Alert.AlertType.WARNING);
            } finally {
                Helper.closeRsAndStmt(rs, stmt);
            }
        });

        exitBtn.setOnAction(actionEvent -> {
            exitBtn.getScene().getWindow().hide();
            try {
                Parent root = FXMLLoader.load(getClass().getResource("../resources/login.fxml"));
                Stage stage = new Stage();
                stage.setTitle("Вход");
                stage.setScene(new Scene(root));
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

}
