import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import java.sql.*;
import javafx.stage.Stage;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamResolution;
import com.google.zxing.*;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;

import java.awt.image.BufferedImage;
import java.util.concurrent.atomic.AtomicBoolean;

public class GuardController 
{
    @FXML private TableView<ScanRecord> guardScanTable;
    @FXML private TextField             guardScanInput;
    @FXML private ImageView             cameraView;
    @FXML private Label                 scanStatusLabel;

    private Webcam webcam;
    private Thread cameraThread;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private String lastScannedId = "";
    private long lastScanTime = 0;

    private static final javafx.collections.ObservableList<ScanRecord> scanLog =
        javafx.collections.FXCollections.observableArrayList();

    @FXML
    public void initialize()
    {
        if (guardScanTable != null && guardScanTable.getColumns().size() >= 5)
        {
            guardScanTable.getColumns().get(0).setCellValueFactory(
                new javafx.scene.control.cell.PropertyValueFactory<>("time"));
            guardScanTable.getColumns().get(1).setCellValueFactory(
                new javafx.scene.control.cell.PropertyValueFactory<>("name"));
            guardScanTable.getColumns().get(2).setCellValueFactory(
                new javafx.scene.control.cell.PropertyValueFactory<>("contactNumber"));
            guardScanTable.getColumns().get(3).setCellValueFactory(
                new javafx.scene.control.cell.PropertyValueFactory<>("grade"));
            guardScanTable.getColumns().get(4).setCellValueFactory(
                new javafx.scene.control.cell.PropertyValueFactory<>("section"));
            guardScanTable.setItems(scanLog);
        }

        if (guardScanInput != null) 
        {
            javafx.application.Platform.runLater(() -> guardScanInput.requestFocus());

            guardScanInput.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
                if (!isNowFocused)
                    javafx.application.Platform.runLater(() -> guardScanInput.requestFocus());
            });
        }

        startCamera();
    }

    private void startCamera()
    {
        try {
            webcam = Webcam.getDefault();
            if (webcam == null) {
                updateStatus("No camera found!");
                return;
            }
            webcam.setViewSize(WebcamResolution.VGA.getSize());
            webcam.open();
            running.set(true);
            updateStatus("Camera active — scanning...");

            cameraThread = new Thread(() -> {
                while (running.get()) {
                    try {
                        BufferedImage image = webcam.getImage();
                        if (image != null) {
                            WritableImage fxImage = SwingFXUtils.toFXImage(image, null);
                            Platform.runLater(() -> {
                                if (cameraView != null) {
                                    cameraView.setImage(fxImage);
                                    cameraView.setScaleX(-1);
                                }
                            });

                            String result = decodeQR(image);
                            if (result != null && !result.isEmpty()) {
                                long now = System.currentTimeMillis();
                                if (!result.equals(lastScannedId) || (now - lastScanTime) > 3000) {
                                    lastScannedId = result;
                                    lastScanTime  = now;
                                    final String scannedId = result;
                                    Platform.runLater(() -> {
                                        updateStatus("Scanned: " + scannedId);
                                        processScan(scannedId);
                                    });
                                }
                            }
                        }
                        Thread.sleep(100);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });
            cameraThread.setDaemon(true);
            cameraThread.start();

        } catch (Exception ex) {
            updateStatus("Camera error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private String decodeQR(BufferedImage image)
    {
        try {
            LuminanceSource source = new BufferedImageLuminanceSource(image);
            BinaryBitmap bitmap   = new BinaryBitmap(new HybridBinarizer(source));
            Result result         = new MultiFormatReader().decode(bitmap);
            return result.getText();
        } catch (NotFoundException e) {
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    private void updateStatus(String message)
    {
        Platform.runLater(() -> {
            if (scanStatusLabel != null) scanStatusLabel.setText("Status: " + message);
        });
    }

    private void stopCamera()
    {
        running.set(false);
        if (webcam != null && webcam.isOpen()) {
            webcam.close();
        }
    }

    @FXML
    void handleGuardScan(ActionEvent event)
    {
        if (guardScanInput == null || guardScanInput.getText().trim().isEmpty()) return;
        processScan(guardScanInput.getText().trim());
        guardScanInput.clear();
    }

    @FXML
    void handleGuardResetLog(ActionEvent event)
    {
        scanLog.clear();
        lastScannedId = "";
        updateStatus("Log cleared. Scanning...");
    }

    private void processScan(String id)
    {
        if (id.isEmpty()) return;

        String name = "", contactNumber = "", grade = "", section = "";

        // 1. Fetch student info
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT * FROM students WHERE student_id = ?"))
        {
            ps.setString(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                name          = rs.getString("full_name");
                contactNumber = rs.getString("parent_number") != null ? rs.getString("parent_number") : "";
                grade         = rs.getString("grade_level");
                section       = rs.getString("section");
            } else {
                updateStatus("Not found: " + id);
                showAlert("Not Found", "Student ID \"" + id + "\" not found.", Alert.AlertType.WARNING);
                return;
            }
        } catch (SQLException ex) {
            showAlert("Database Error", ex.getMessage(), Alert.AlertType.ERROR);
            ex.printStackTrace();
            return;
        }

        // 2. Determine current session based on time
        java.time.LocalTime now          = java.time.LocalTime.now();
        java.time.LocalTime timeInStart  = java.time.LocalTime.of(6,  0);
        java.time.LocalTime timeInEnd    = java.time.LocalTime.of(12, 0);
        java.time.LocalTime timeOutStart = java.time.LocalTime.of(12, 0);
        java.time.LocalTime timeOutEnd   = java.time.LocalTime.of(18, 0);

        boolean isTimeIn  = !now.isBefore(timeInStart)  && now.isBefore(timeInEnd);
        boolean isTimeOut = !now.isBefore(timeOutStart) && now.isBefore(timeOutEnd);

        if (!isTimeIn && !isTimeOut)
        {
            showAlert("Outside Hours",
                "Scanning is only allowed between 6:00 AM – 6:00 PM.",
                Alert.AlertType.WARNING);
            return;
        }

        // 3. Check for existing scans today
        try (Connection conn = DatabaseConnection.getConnection())
        {
            // Check time-in record
            PreparedStatement psCheckIn = conn.prepareStatement(
                "SELECT COUNT(*) FROM attendance_logs " +
                "WHERE student_id = ? AND scan_date = CURRENT_DATE " +
                "AND scan_time >= '06:00:00'::time AND scan_time < '12:00:00'::time");
            psCheckIn.setString(1, id);
            ResultSet rsIn = psCheckIn.executeQuery();
            rsIn.next();
            boolean alreadyTimedIn = rsIn.getInt(1) > 0;

            // Check time-out record
            PreparedStatement psCheckOut = conn.prepareStatement(
                "SELECT COUNT(*) FROM attendance_logs " +
                "WHERE student_id = ? AND scan_date = CURRENT_DATE " +
                "AND scan_time >= '12:00:00'::time AND scan_time < '18:00:00'::time");
            psCheckOut.setString(1, id);
            ResultSet rsOut = psCheckOut.executeQuery();
            rsOut.next();
            boolean alreadyTimedOut = rsOut.getInt(1) > 0;

            if (isTimeIn && alreadyTimedIn)
            {
                updateStatus("Already timed in: " + name);
                showAlert("Already Scanned",
                    name + " has already timed in today.",
                    Alert.AlertType.WARNING);
                return;
            }

            if (isTimeOut && !alreadyTimedIn)
            {
                updateStatus("No time-in record: " + name);
                showAlert("No Time-In Record",
                    name + " has not timed in today.\nPlease time in first.",
                    Alert.AlertType.WARNING);
                return;
            }

            if (isTimeOut && alreadyTimedOut)
            {
                updateStatus("Already timed out: " + name);
                showAlert("Already Scanned",
                    name + " has already timed out today.",
                    Alert.AlertType.WARNING);
                return;
            }

        } catch (SQLException ex) {
            showAlert("Database Error", ex.getMessage(), Alert.AlertType.ERROR);
            ex.printStackTrace();
            return;
        }

        // 4. All checks passed — log attendance
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO attendance_logs (student_id, scan_date, scan_time) " +
                     "VALUES (?, CURRENT_DATE, CURRENT_TIME)"))
        {
            ps.setString(1, id);
            ps.executeUpdate();
        } catch (SQLException ex) {
            showAlert("Database Error", "Could not log attendance:\n" + ex.getMessage(), Alert.AlertType.ERROR);
            ex.printStackTrace();
            return;
        }

        // 5. Success
        String session = isTimeIn ? "Time-In" : "Time-Out";
        String timeNow = now.format(java.time.format.DateTimeFormatter.ofPattern("hh:mm:ss a"));

        final String finalName    = name;
        final String finalContact = contactNumber;
        final String finalGrade   = grade;
        final String finalSection = section;
        final String finalSession = session;

        Platform.runLater(() -> {
            scanLog.add(0, new ScanRecord(timeNow, finalName, finalContact, finalGrade, finalSection));
            updateStatus("✓ " + finalSession + " logged: " + finalName);
            showAlert("Successfully Scanned!",
                "✓ " + finalSession + "\n" +
                "Name    : " + finalName + "\n" +
                "Time    : " + timeNow,
                Alert.AlertType.INFORMATION);
        });
    }

    private void showAlert(String title, String content, Alert.AlertType type)
    {
        Platform.runLater(() -> {
            Alert a = new Alert(type);
            a.setTitle(title);
            a.setHeaderText(null);
            a.setContentText(content);
            a.showAndWait();
        });
    }

    @FXML
    void handleBackToMain(ActionEvent event)
    {
        stopCamera();
        try {
            Parent root = FXMLLoader.load(getClass().getResource("Startup.fxml"));
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.setScene(new javafx.scene.Scene(root));
            stage.setMaximized(false);
            stage.setMaximized(true);
            stage.show();
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }
}