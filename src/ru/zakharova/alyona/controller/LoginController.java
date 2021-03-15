package ru.zakharova.alyona.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.*;
import oracle.jdbc.driver.OracleDriver;

public class LoginController {

    @FXML
    private TextField loginField;

    @FXML
    private PasswordField pwdField;

    @FXML
    private Button loginBtn;

    @FXML
    private Label loginInfoLabel;

    Connection connection;

    private boolean connect(String username, String password) {
        try {
            connection = DriverManager.getConnection(
                    "jdbc:oracle:thin:@localhost:1521:XE",
                    username, password);
        } catch (SQLException e) {
            return false;
        }
        return true;
    }

    @FXML
    void initialize() throws SQLException {
        DriverManager.registerDriver(new oracle.jdbc.OracleDriver());

        loginBtn.setOnAction(actionEvent -> {
            if (!loginField.getText().isEmpty() && !pwdField.getText().isEmpty()) {
                if (connect(loginField.getText(), pwdField.getText())) {
                    loginBtn.getScene().getWindow().hide();
                    try {
                        Parent root = FXMLLoader.load(getClass().getResource("../resources/main-window.fxml"));
                        Stage stage = new Stage();
                        stage.setTitle("Библиотека");
                        stage.setScene(new Scene(root));
                        stage.show();
                    } catch (IOException e) {
                        System.out.println("Ooops...");
                    }
                } else {
                    loginInfoLabel.setText("Invalid login or/and password");
                    loginField.setText("");
                    pwdField.setText("");
                }
            }
        });
    }

}
