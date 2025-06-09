package uk.ac.ed.inf.ui;

import java.sql.SQLException;
import java.util.List;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.VBox;
import uk.ac.ed.inf.Commands;
import uk.ac.ed.inf.database.PlayerStatsDAO;
import uk.ac.ed.inf.model.PlayerStats;
import uk.ac.ed.inf.service.Service;

public class PrimaryController {

    @FXML
    private ToggleButton dataCollectionToggle;
    @FXML
    private VBox dataContainer;
    @FXML
    private TextField commandInput;

    @FXML
    private void initialize() {
        // You can set the initial state of the toggle button here if needed
    }

    @FXML
    private void toggleDataCollection() {
        if (dataCollectionToggle.isSelected()) {
            dataCollectionToggle.setText("Data Collection: ON");
            Service.startAutoProcessing();
        } else {
            dataCollectionToggle.setText("Data Collection: OFF");
            Service.stopAutoProcessing();
        }
    }

    @FXML
    private void submitCommand() {
        String command = commandInput.getText();
        if (command != null && !command.trim().isEmpty()) {
            Commands.call(command);
            commandInput.clear();
        }
    }

    @FXML
    private void showData() {
        dataContainer.getChildren().clear(); // Clear previous data
        try {
            List<PlayerStats> allStats = PlayerStatsDAO.getAllPlayerStats();
            if (allStats.isEmpty()) {
                dataContainer.getChildren().add(new Label("No data available."));
            } else {
                for (PlayerStats stats : allStats) {
                    Label playerLabel = new Label(stats.toString()); // Assuming PlayerStats has a useful toString() method
                    dataContainer.getChildren().add(playerLabel);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            dataContainer.getChildren().add(new Label("Error loading data from the database."));
        }
    }

    public void shutdown() {
        Service.stopAutoProcessing();
    }
} 