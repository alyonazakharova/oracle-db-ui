package ru.zakharova.alyona.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.springframework.security.crypto.bcrypt.BCrypt;
import ru.zakharova.alyona.Helper;

import java.io.IOException;
import java.sql.*;

public class LoginController {

    @FXML
    private TextField loginField;

    @FXML
    private PasswordField pwdField;

    @FXML
    private Button loginBtn;

    public static Connection connection;

    public LoginController() {
        try {
            DriverManager.registerDriver(new oracle.jdbc.OracleDriver());
            connection = DriverManager.getConnection(
                    "jdbc:oracle:thin:@localhost:1521:XE",
                    "c##myuser", "mypass");
        } catch (SQLException e) {
            System.out.println("БАЛИН, ЧТО-ТО НЕ ТАК");
        }
    }

    private boolean auth(String login, String password) {
        String query = "SELECT PASSWORD FROM USERS WHERE LOGIN='" + login + "'";
        Statement stmt = null;
        ResultSet rs = null;
        try {
            stmt = connection.createStatement();
            rs = stmt.executeQuery(query);
            if (rs.next()) {
                String hashedPassword = rs.getString("PASSWORD");
                if (BCrypt.checkpw(password, hashedPassword)) {
                    return true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Helper.showInfo(e.getMessage(), Alert.AlertType.WARNING);
        } finally {
            Helper.closeRsAndStmt(rs, stmt);
        }
        return false;
    }

    @FXML
    void initialize() throws SQLException {
        DriverManager.registerDriver(new oracle.jdbc.OracleDriver());

        loginBtn.setOnAction(actionEvent -> {
            String login = loginField.getText();
            String password = pwdField.getText();
            if (!login.isEmpty() && !password.isEmpty()) {
                if (auth(login, password)) {
                    loginBtn.getScene().getWindow().hide();
                    try {
                        Parent root = FXMLLoader.load(getClass().getResource("../resources/main-window.fxml"));
                        Stage stage = new Stage();
                        stage.setTitle("Библиотека им. А. А. Захаровой))00))0)");
                        stage.setScene(new Scene(root));
                        stage.show();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    Helper.showInfo("Неверный логин или пароль", Alert.AlertType.WARNING);
                    loginField.setText("");
                    pwdField.setText("");
                }
            }
        });
    }
}
