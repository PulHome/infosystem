package informationsystem;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Zerg0s
 */
public class InformationSystem extends Application {
    @Override
    public void start(Stage stage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("./FXMLDocument.fxml"));
            Parent root = loader.load();
            FXMLDocumentController controller = loader.getController();
            loader.setRoot(this);
            loader.setClassLoader(getClass().getClassLoader());

            Scene scene = new Scene(root);
            stage.setTitle("Проверка задач в системе Redmine");
            stage.setScene(scene);
            stage.setOnHidden(e -> controller.shutdown());
            stage.getIcons().add(new Image(Objects.requireNonNull(InformationSystem.class.getResourceAsStream("robot.png"))));
            stage.show();
        } catch (IOException ex) {
            ex.printStackTrace();
            Logger.getLogger(InformationSystem.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void main(String[] args) {
        System.setProperty("java.net.useSystemProxies", "true");
        // открытие сцены
        launch(args);
    }

    private static Logger logger = Logger.getLogger(FXMLDocumentController.class.getSimpleName());
}
