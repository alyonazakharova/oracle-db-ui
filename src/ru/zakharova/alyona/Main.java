package ru.zakharova.alyona;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import oracle.jdbc.OracleType;

import java.sql.*;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("resources/login.fxml"));
        primaryStage.setTitle("Вход");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }

//public class Main {
    public static void main(String[] args) {
        launch(args);

//        Connection connection;
//        try {
//            DriverManager.registerDriver(new oracle.jdbc.OracleDriver());
//            connection = DriverManager.getConnection(
//                    "jdbc:oracle:thin:@localhost:1521:XE",
//                    "c##myuser", "mypass");
//        } catch (SQLException e) {
//            System.out.println("БАЛИН, ЧТО-ТО НЕ ТАК");
//            return;
//        }
//        try {
//            int bookId = 13;
//            int time = -1;
//            int clientId = -1;
//            CallableStatement cs = connection.prepareCall("{ call MAX_TIME(?, ?, ?)}");
//            cs.setInt(1, bookId);
//            cs.registerOutParameter(2, OracleType.NUMBER);
//            cs.registerOutParameter(3, OracleType.NUMBER);
//            cs.executeQuery();
//            time = cs.getInt(2);
//            clientId = cs.getInt(3);
//            System.out.println(time + " * " + clientId);
//
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//
//        String query = "INSERT INTO USERS (LOGIN, PASSWORD) VALUES (?, ?)";
//        PreparedStatement pstmt = null;
//        String pwd = "pwd";
//        String hashed = BCrypt.hashpw(pwd, BCrypt.gensalt());
//        try {
//            pstmt = connection.prepareStatement(query);
//            pstmt.setString(1, "user");
//            pstmt.setString(2, hashed);
//            pstmt.executeUpdate();
//        } catch (SQLException e) {
//            e.printStackTrace();
//            MainWindowController.showInfo(e.getMessage(), Alert.AlertType.WARNING);
//        } finally {
//            if (pstmt != null) {
//                try {
//                    pstmt.close();
//                } catch (SQLException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
    }
}
