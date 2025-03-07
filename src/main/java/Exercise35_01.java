package com.example.dbcdemo;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.sql.*;

// Run the following SQL query to create the db table
// create table Temp (num1 double, num2 double, num3 double);
public class Exercise35_01 extends Application {

    private TextArea resultArea;
    private Connection connection;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Exercise35_01");

        GridPane grid = new GridPane();
        grid.setAlignment(javafx.geometry.Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));

        Button batchUpdateButton = new Button("Batch Update");
        Button nonBatchUpdateButton = new Button("Non-Batch Update");
        Button connectButton = new Button("Connect to Database");

        resultArea = new TextArea();
        resultArea.setEditable(false);
        resultArea.setPrefRowCount(10);
        resultArea.setPrefColumnCount(40);

        HBox buttonBox = new HBox(10);
        buttonBox.getChildren().addAll(batchUpdateButton, nonBatchUpdateButton);

        grid.add(connectButton, 0, 0);
        grid.add(buttonBox, 0, 1);
        grid.add(resultArea, 0, 2);

        connectButton.setOnAction(e -> showDBConnectionDialog(primaryStage));
        batchUpdateButton.setOnAction(e -> performBatchUpdate());
        nonBatchUpdateButton.setOnAction(e -> performNonBatchUpdate());

        Scene scene = new Scene(grid, 500, 400);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void showDBConnectionDialog(Stage ownerStage) {
        Stage dialogStage = new Stage();
        dialogStage.setTitle("Connect to DB");
        dialogStage.initModality(Modality.WINDOW_MODAL);
        dialogStage.initOwner(ownerStage);

        GridPane dialogGrid = new GridPane();
        dialogGrid.setAlignment(javafx.geometry.Pos.CENTER);
        dialogGrid.setHgap(10);
        dialogGrid.setVgap(10);
        dialogGrid.setPadding(new Insets(25, 25, 25, 25));

        // Change to your enviro
        Label jdbcDriverLabel = new Label("JDBC Driver");
        TextField jdbcDriverField = new TextField("com.mysql.cj.jdbc.Driver");
        Label databaseUrlLabel = new Label("Database URL");
        TextField databaseUrlField = new TextField("jdbc:mysql://localhost/javabook");
        Label usernameLabel = new Label("Username");
        TextField usernameField = new TextField("scott");
        Label passwordLabel = new Label("Password");
        PasswordField passwordField = new PasswordField();

        Button connectDialogButton = new Button("Connect to DB");
        Button closeDialogButton = new Button("Close Dialog");

        dialogGrid.add(jdbcDriverLabel, 0, 0);
        dialogGrid.add(jdbcDriverField, 1, 0);
        dialogGrid.add(databaseUrlLabel, 0, 1);
        dialogGrid.add(databaseUrlField, 1, 1);
        dialogGrid.add(usernameLabel, 0, 2);
        dialogGrid.add(usernameField, 1, 2);
        dialogGrid.add(passwordLabel, 0, 3);
        dialogGrid.add(passwordField, 1, 3);
        dialogGrid.add(connectDialogButton, 0, 4);
        dialogGrid.add(closeDialogButton, 1, 4);

        connectDialogButton.setOnAction(e -> {
            try {
                Class.forName(jdbcDriverField.getText());
                connection = DriverManager.getConnection(databaseUrlField.getText(), usernameField.getText(), passwordField.getText());
                resultArea.appendText("Connected to " + databaseUrlField.getText() + "\n");
                dialogStage.close();
            } catch (ClassNotFoundException | SQLException ex) {
                resultArea.appendText("Connection failed: " + ex.getMessage() + "\n");
            }
        });

        closeDialogButton.setOnAction(e -> dialogStage.close());

        Scene dialogScene = new Scene(dialogGrid, 400, 250);
        dialogStage.setScene(dialogScene);
        dialogStage.showAndWait();
    }

    private void performBatchUpdate() {
        if (connection == null) {
            resultArea.appendText("Please connect to the database first.\n");
            return;
        }

        long startTime = System.currentTimeMillis();
        try {
            connection.setAutoCommit(false);
            String sql = "INSERT INTO Temp (num1, num2, num3) VALUES (?, ?, ?)";
            PreparedStatement statement = connection.prepareStatement(sql);

            for (int i = 0; i < 1000; i++) {
                statement.setDouble(1, Math.random());
                statement.setDouble(2, Math.random());
                statement.setDouble(3, Math.random());
                statement.addBatch();
            }

            statement.executeBatch();
            connection.commit();
            connection.setAutoCommit(true);

            long endTime = System.currentTimeMillis();
            resultArea.appendText("Batch update succeeded\n");
            resultArea.appendText("Batch update completed\n");
            resultArea.appendText("The elapsed time is " + (endTime - startTime) + " ms\n");
        } catch (SQLException ex) {
            resultArea.appendText("Batch update failed: " + ex.getMessage() + "\n");
            try {
                connection.rollback();
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                resultArea.appendText("Rollback failed: " + e.getMessage() + "\n");
            }
        }
    }

    private void performNonBatchUpdate() {
        if (connection == null) {
            resultArea.appendText("Please connect to the database first.\n");
            return;
        }

        long startTime = System.currentTimeMillis();
        try {
            String sql = "INSERT INTO Temp (num1, num2, num3) VALUES (?, ?, ?)";
            PreparedStatement statement = connection.prepareStatement(sql);

            for (int i = 0; i < 1000; i++) {
                statement.setDouble(1, Math.random());
                statement.setDouble(2, Math.random());
                statement.setDouble(3, Math.random());
                statement.executeUpdate();
            }

            long endTime = System.currentTimeMillis();
            resultArea.appendText("Non-Batch update completed\n");
            resultArea.appendText("The elapsed time is " + (endTime - startTime) + " ms\n");
        } catch (SQLException ex) {
            resultArea.appendText("Non-Batch update failed: " + ex.getMessage() + "\n");
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}

