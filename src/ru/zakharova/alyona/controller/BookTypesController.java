package ru.zakharova.alyona.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import ru.zakharova.alyona.Helper;
import ru.zakharova.alyona.dto.BookType;

import java.sql.*;

public class BookTypesController {

    @FXML
    private TableView<BookType> bookTypesTable;

    @FXML
    private TableColumn<BookType, Integer> idColumn;

    @FXML
    private TableColumn<BookType, String> typeColumn;

    @FXML
    private TableColumn<BookType, Integer> daysColumn;

    @FXML
    private TableColumn<BookType, Double> fineColumn;

    @FXML
    private TextField typeField;

    @FXML
    private TextField daysField;

    @FXML
    private TextField fineField;

    @FXML
    private Button addBtn;

    @FXML
    private Button updateBtn;

    @FXML
    private Button deleteBtn;

//    private final Connection connection;
    private final ObservableList<BookType> booksTypes = FXCollections.observableArrayList();
    private int selectedForUpdateTypeId = -1;

//    public BookTypesController() {
//        this.connection = LoginController.connection;
//    }

    private void loadBookTypes() {
        String query = "SELECT ID, NAME, DAY_COUNT, FINE FROM BOOK_TYPES";
        Statement stmt = null;
        ResultSet rs = null;
        try {
            stmt = LoginController.connection.createStatement();
            rs = stmt.executeQuery(query);
            while (rs.next()) {
                booksTypes.add(new BookType(
                        rs.getInt("ID"),
                        rs.getString("NAME"),
                        rs.getInt("DAY_COUNT"),
                        rs.getDouble("FINE")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Helper.showInfo(e.getMessage(), Alert.AlertType.WARNING);
        } finally {
            Helper.closeRsAndStmt(rs, stmt);
        }
    }

    private void fillTable() {
        booksTypes.clear();
        loadBookTypes();
        bookTypesTable.setItems(booksTypes);
    }

    private void clearInput() {
        typeField.setText("");
        daysField.setText("");
        fineField.setText("");
    }

    @FXML
    void initialize() {
        idColumn.setCellValueFactory(new PropertyValueFactory<BookType, Integer>("id"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<BookType, String>("name"));
        daysColumn.setCellValueFactory(new PropertyValueFactory<BookType, Integer>("days"));
        fineColumn.setCellValueFactory(new PropertyValueFactory<BookType, Double>("fine"));

        fillTable();

        addBtn.setOnAction(actionEvent -> {
            if (!typeField.getText().isEmpty()
                    & !daysField.getText().isEmpty()
                    & !fineField.getText().isEmpty()) {
                int days = -1;
                double fine = -1;
                try {
                    days = Integer.parseInt(daysField.getText());
                    fine = Double.parseDouble(fineField.getText());
                } catch (NumberFormatException e) {
                    Helper.showInfo("Проверьте ввод", Alert.AlertType.WARNING);
                    return;
                }
                String query = "INSERT INTO BOOK_TYPES (NAME, FINE, DAY_COUNT) VALUES (?, ?, ?)";
                PreparedStatement pstmt = null;
                try {
                    pstmt = LoginController.connection.prepareStatement(query);
                    pstmt.setString(1, typeField.getText());
                    pstmt.setInt(2, days);
                    pstmt.setDouble(3, fine);
                    pstmt.executeUpdate();
                    Helper.showInfo("Новый тип успешно добавлен", Alert.AlertType.INFORMATION);
                    fillTable();
                } catch (SQLException e) {
                    e.printStackTrace();
                    Helper.showInfo(e.getMessage(), Alert.AlertType.WARNING);
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
                Helper.showInfo("Необходимо заполнить все поля", Alert.AlertType.WARNING);
            }

        });

        bookTypesTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            BookType selectedType = bookTypesTable.getSelectionModel().getSelectedItem();
            if (selectedType == null) {
                clearInput();
                return;
            }
            selectedForUpdateTypeId = selectedType.getId();
            typeField.setText(selectedType.getName());
            daysField.setText(String.valueOf(selectedType.getDays()));
            fineField.setText(String.valueOf(selectedType.getFine()));
        });

        updateBtn.setOnAction(actionEvent -> {
            if (bookTypesTable.getSelectionModel().getSelectedItem() == null) {
                Helper.showInfo("Ничего не выбрано", Alert.AlertType.WARNING);
            }
            int days = -1;
            double fine = -1;
            try {
                days = Integer.parseInt(daysField.getText());
                fine = Double.parseDouble(fineField.getText());
            } catch (NumberFormatException e) {
                Helper.showInfo("Проверьте ввод", Alert.AlertType.WARNING);
                return;
            }
            String query = "UPDATE BOOK_TYPES SET NAME='" + typeField.getText() +
                    "', FINE=" + fine + ", DAY_COUNT=" + days +
                    " WHERE ID=" + selectedForUpdateTypeId;
            Statement stmt = null;
            try {
                stmt = LoginController.connection.createStatement();
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
            BookType selectedType = bookTypesTable.getSelectionModel().getSelectedItem();
            if (selectedType != null) {
                int typeId = selectedType.getId();
                String query = "DELETE FROM BOOK_TYPES WHERE ID=" + typeId;
                Statement stmt = null;
                try {
                    stmt = LoginController.connection.createStatement();
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
                    if (stmt != null) {
                        try {
                            stmt.close();
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                }
            } else {
                Helper.showInfo("Ничего не выбрано", Alert.AlertType.WARNING);
            }
        });
    }

}
