import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.scene.image.Image;
import javafx.application.Platform;

public class Main extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {

        Parent root = FXMLLoader.load(getClass().getResource("Startup.fxml"));
        Scene scene = new Scene(root);
        
        stage.setScene(scene);
        stage.setTitle("FRSCES── Attendance Monitoring System");
        
        Image image = new Image(getClass().getResourceAsStream("/LOGO.jpg"));
        stage.getIcons().add(image);

        // Ensure the app closes all background processes on exit
        stage.setOnCloseRequest(event -> {
            Platform.exit(); 
            System.exit(0);
        });

        stage.setMaximized(true); 
        stage.show();
    }
}