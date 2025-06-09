package uk.ac.ed.inf.ui;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import uk.ac.ed.inf.database.PlayerStatsDAO;

/**
 * JavaFX App
 */
public class App extends Application {

    private static Scene scene;
    private PrimaryController controller;

    @Override
    public void start(Stage stage) throws IOException {
        // Initialise the database
        PlayerStatsDAO.initialise();

        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("primary.fxml"));
        Parent root = fxmlLoader.load();
        controller = fxmlLoader.getController();

        scene = new Scene(root, 640, 480);
        stage.setScene(scene);
        stage.setTitle("Finals Stat Tracker");
        stage.setMinWidth(400);
        stage.setMinHeight(300);

        stage.setOnCloseRequest(event -> controller.shutdown());

        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }

} 