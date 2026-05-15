import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import java.io.IOException;
import javafx.scene.control.PasswordField;



public class LoginController 
{
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;

    @FXML
    void handleLogin(ActionEvent event) 
    {
        String username = usernameField.getText();
        String password = passwordField.getText();

        // Simple hardcoded check — change as needed
        if (username.equals("admin") && password.equals("admin123")) 
        {
            try {
                Parent root = FXMLLoader.load(getClass().getResource("SceneB.fxml"));
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setMaximized(false);
                stage.setMaximized(true);
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } 
            else if (username.equals("guard") && password.equals("guard123")) 
            {
                try {
                    Parent root = FXMLLoader.load(getClass().getResource("GuardScan.fxml"));
                    Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                    stage.setScene(new Scene(root));
                    stage.setMaximized(false);
                    stage.setMaximized(true);
                    stage.show();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } 
                else 
                {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Login Failed");
                    alert.setHeaderText(null);
                    alert.setContentText("Invalid username or password.");
                    alert.showAndWait();
                }
    }

    @FXML
    void backToAdmin(ActionEvent event) 
    {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("Startup.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setMaximized(false);
            stage.setMaximized(true);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}