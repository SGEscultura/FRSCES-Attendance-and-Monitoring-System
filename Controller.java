import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import javafx.animation.FadeTransition;
import javafx.util.Duration;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.sql.*;
import javafx.stage.Stage;

public class Controller 
{
    @FXML private StackPane contentArea;
    @FXML private Button btnRegister, btnView, btnUpdate, btnDelete, btnSummary, btnGenerate, btnBack;

    // Register.fxml
    @FXML private TextField        regFullName, regStudentID, regContactNumber;
    @FXML private ComboBox<String> comboGradeLevel, comboSection;

    // ViewStu.fxml
    @FXML private TableView<Student>          viewTable;
    @FXML private ComboBox<String>            filterGrade, filterSection, filterSession;
    @FXML private DatePicker                  filterDate;
    @FXML private TableColumn<Student,String> colViewName, colViewID, colViewContactNumber, colViewAttendance;

    // UpdateStu.fxml
    @FXML private TextField        searchID, updateStudentID, updateFullName, updateContactNumber;

    // Delete.fxml
    @FXML private TableView<Student>          deleteTable;
    @FXML private TableColumn<Student,String> colDeleteName, colDeleteID, colDeleteContactNumber;
    @FXML private ComboBox<String>            deleteFilterGrade, deleteFilterSection;
    @FXML private TextField                   deleteStudentID;

    // Summary.fxml
    @FXML private BarChart<String,Number>              attendanceChart;
    @FXML private ComboBox<String>                     summaryMonth, summaryGrade, summarySection, summaryYear;
    @FXML private TableView<AttendanceSummary>         summaryTable;
    @FXML private TableColumn<AttendanceSummary,String>  colSummaryName, colSummaryID;
    @FXML private TableColumn<AttendanceSummary,Integer> colSummaryPresent, colSummaryAbsent;

    // Generate.fxml
    @FXML private TableView<Student> genStudentTable;
    @FXML private ComboBox<String>   genGradeBox, genSectionBox;
    @FXML private ImageView          imgQrCode;

    //  shared across all instances
    private static final Map<String, List<String>> sectionData = new LinkedHashMap<>();
    static 
    {
        sectionData.put("Kinder",  Arrays.asList("FAITH (AM)", "FAITH (PM)", "HOPE (AM)", "HOPE (PM)"));
        sectionData.put("Grade 1", Arrays.asList("OKRA", "PATOLA", "PECHAY", "UPO"));
        sectionData.put("Grade 2", Arrays.asList("AVOCADO", "MANGO", "PAPAYA"));
        sectionData.put("Grade 3", Arrays.asList("BONIFACIO", "DEL PILAR", "MABINI", "RIZAL"));
        sectionData.put("Grade 4", Arrays.asList("DIAMOND", "PEARL", "ZIRCON"));
        sectionData.put("Grade 5", Arrays.asList("ACACIA", "MOLAVE", "YAKAL"));
        sectionData.put("Grade 6", Arrays.asList("DUTERTE", "MARCOS", "QUEZON"));
        sectionData.put("SPED",    Arrays.asList("SPED A", "SPED B"));
    }

    //  INITIALIZE  — runs on EVERY Controller instance
    @FXML
    public void initialize() 
    {
    // Only the SHELL controller has contentArea
    if (contentArea != null) 
    {
        resetSidebarColors();
        showRegisterScreen();
        applySmoothDarken(btnRegister);
        return; // ← ADD THIS LINE
    }

    // --- Delete.fxml ---
    if (deleteTable != null) 
    {
        if (colDeleteName  != null) colDeleteName.setCellValueFactory(new PropertyValueFactory<>("name"));
        if (colDeleteID    != null) colDeleteID.setCellValueFactory(new PropertyValueFactory<>("studentId"));
        if (colDeleteContactNumber != null) colDeleteContactNumber.setCellValueFactory(new PropertyValueFactory<>("contactNumber"));

        if (deleteFilterGrade != null) 
        {
            deleteFilterGrade.getItems().setAll(sectionData.keySet());
            deleteFilterGrade.setOnAction(e -> {
                updateSections(deleteFilterGrade, deleteFilterSection);
                if (deleteFilterSection != null)
                    deleteFilterSection.getSelectionModel().clearSelection();
            });
        }
        return; // ← ADD THIS LINE
    }

    // --- ViewStu.fxml ---
    if (colViewAttendance != null) colViewAttendance.setCellValueFactory(new PropertyValueFactory<>("attendance"));

    if (viewTable != null) 
    {
        if (colViewName  != null) colViewName.setCellValueFactory(new PropertyValueFactory<>("name"));
        if (colViewID    != null) colViewID.setCellValueFactory(new PropertyValueFactory<>("studentId"));
        if (colViewContactNumber != null) colViewContactNumber.setCellValueFactory(new PropertyValueFactory<>("contactNumber"));

        if (filterGrade != null)
        {
            filterGrade.getItems().setAll(sectionData.keySet());
            filterGrade.setOnAction(e -> {
                updateSections(filterGrade, filterSection);
                if (filterSection != null)
                    filterSection.getSelectionModel().clearSelection();
            });
        }
        if (filterSession != null)
            filterSession.getItems().setAll(
                "Time-in (6:00 AM – 12:00 PM)",
                "Time-out (12:00 PM – 6:00 PM)");
        return; // ← ADD THIS
    }

    // --- Register.fxml ---
    if (comboGradeLevel != null && deleteTable == null && viewTable == null && summaryGrade == null && genGradeBox == null) 
    {
        comboGradeLevel.getItems().setAll(sectionData.keySet());
        comboGradeLevel.setOnAction(e -> updateSections(comboGradeLevel, comboSection));
    }

    // --- UpdateStu.fxml ---
    if (searchID != null || updateStudentID != null) 
    {
        if (comboGradeLevel != null) 
        {
            comboGradeLevel.getItems().setAll(sectionData.keySet());
            comboGradeLevel.setOnAction(e -> updateSections(comboGradeLevel, comboSection));
        }
    }

    // --- Summary.fxml ---
    // --- Summary.fxml ---
    if (summaryGrade != null) 
    {
        if (summaryMonth != null)
            summaryMonth.getItems().setAll(
                    "January","February","March","April","May","June",
                    "July","August","September","October","November","December");

        if (summaryYear != null) {
           int currentYear = java.time.LocalDate.now().getYear();
            for (int y = 2026; y <= currentYear + 3; y++)
                summaryYear.getItems().add(String.valueOf(y));
            summaryYear.setValue(String.valueOf(currentYear));
        }

        summaryGrade.getItems().setAll(sectionData.keySet());
        summaryGrade.setOnAction(e -> {
            updateSections(summaryGrade, summarySection);
            if (summarySection != null)
                summarySection.getSelectionModel().clearSelection();
        });

        if (colSummaryName    != null) colSummaryName.setCellValueFactory(new PropertyValueFactory<>("name"));
        if (colSummaryID      != null) colSummaryID.setCellValueFactory(new PropertyValueFactory<>("studentId"));
        if (colSummaryPresent != null) colSummaryPresent.setCellValueFactory(new PropertyValueFactory<>("presentDays"));
        if (colSummaryAbsent  != null) colSummaryAbsent.setCellValueFactory(new PropertyValueFactory<>("absentDays"));
    }

    // --- Generate.fxml ---
    if (genGradeBox != null) 
    {
        genGradeBox.getItems().setAll(sectionData.keySet());
        genGradeBox.setOnAction(e -> updateSections(genGradeBox, genSectionBox));
        if (genStudentTable != null && !genStudentTable.getColumns().isEmpty())
            genStudentTable.getColumns().get(0)
                    .setCellValueFactory(new PropertyValueFactory<>("name"));
}
    }

    //  SIDEBAR STYLE HELPERS
    private void applySmoothDarken(Button b) 
    {
        if (b == null) return;
        b.setStyle("-fx-background-color: #A0C4B1; -fx-text-fill: black; -fx-background-radius: 12;");
        FadeTransition ft = new FadeTransition(Duration.millis(300), b);
        ft.setFromValue(0.6); ft.setToValue(1.0); ft.play();
    }

    private void applySmoothRed(Button b) 
    {
        if (b == null) return;
        b.setStyle("-fx-background-color: #FF6961; -fx-text-fill: white; "
                 + "-fx-background-radius: 12; -fx-font-weight: bold;");
        FadeTransition ft = new FadeTransition(Duration.millis(300), b);
        ft.setFromValue(0.6); ft.setToValue(1.0); ft.play();
    }

    private void resetSidebarColors() 
    {
        for (Button b : Arrays.asList(btnRegister, btnView, btnUpdate,
                                      btnDelete, btnSummary, btnGenerate))
            if (b != null)
            b.setStyle("-fx-background-color: #E6EDE9; -fx-text-fill: black; " + "-fx-background-radius: 12;");
    }

    private void updateSections(ComboBox<String> gradeBox, ComboBox<String> sectionBox) 
    {
        if (gradeBox == null || sectionBox == null) return;
        String sel = gradeBox.getValue();
        sectionBox.getItems().clear();
        if (sel != null && sectionData.containsKey(sel))
            sectionBox.getItems().addAll(sectionData.get(sel));
    }

    private void showAlert(String title, String content, Alert.AlertType type) 
    {
        Alert a = new Alert(type);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(content);
        a.showAndWait();
    }

    // Screen cache — keeps screens alive so data is preserved
    private final Map<String, Parent> screenCache = new HashMap<>();

    private void loadScreen(String fxmlFile) 
    {
        try 
        {
            // If already on this screen, do nothing
            if (screenCache.containsKey(fxmlFile) && 
                contentArea.getChildren().contains(screenCache.get(fxmlFile))) 
            {
                return; // already showing this screen, don't reload
            }

            // If cached, reuse it instead of reloading
            if (screenCache.containsKey(fxmlFile)) 
            {
                contentArea.getChildren().setAll(screenCache.get(fxmlFile));
                return;
            }

            // First time loading — create and cache it
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = loader.load();
            screenCache.put(fxmlFile, root);
            contentArea.getChildren().setAll(root);

        } catch (IOException e) {
            System.err.println("ERROR loading " + fxmlFile + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    // =========================================================
    // REGISTER  →  Register.fxml
    @FXML 
    void handleRegister(ActionEvent event)
    {
        resetSidebarColors(); applySmoothDarken(btnRegister);
        showRegisterScreen();
    }

    private void showRegisterScreen() { loadScreen("Register.fxml"); }

    @FXML
    void handleRegisterSubmit(ActionEvent event) 
    {
        String id      = getText(regStudentID);
        String name    = getText(regFullName);
        String contactNumber = getText(regContactNumber);
        String grade   = (comboGradeLevel != null) ? comboGradeLevel.getValue() : null;
        String section = (comboSection    != null) ? comboSection.getValue()    : null;

        if (id.isEmpty())              { showAlert("Missing", "Student ID is required.",      Alert.AlertType.WARNING); return; }
        if (name.isEmpty())            { showAlert("Missing", "Full Name is required.",       Alert.AlertType.WARNING); return; }
        if (grade   == null || grade.isEmpty())   { showAlert("Missing", "Select a Grade Level.", Alert.AlertType.WARNING); return; }
        if (section == null || section.isEmpty()) { showAlert("Missing", "Select a Section.",     Alert.AlertType.WARNING); return; }

        String sql = "INSERT INTO students (student_id, full_name, parent_number, grade_level, section) "
                   + "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql))
        {

            ps.setString(1, id);
            ps.setString(2, name);
            ps.setString(3, contactNumber.isEmpty() ? null : contactNumber);
            ps.setString(4, grade);
            ps.setString(5, section);
            ps.executeUpdate();

            showAlert("Registered Successfully!",
                    "\"" + name + "\" (ID: " + id + ") has been registered.",
                    Alert.AlertType.INFORMATION);
            clearRegisterFields();

        } catch (SQLException ex) {
            if ("23505".equals(ex.getSQLState())) {
                showAlert("Duplicate Student ID",
                        "ID \"" + id + "\" already exists. Use a different ID.",
                        Alert.AlertType.ERROR);
            } else {
                showAlert("Database Error", ex.getMessage(), Alert.AlertType.ERROR);
                ex.printStackTrace();
            }
        }
    }

    private void clearRegisterFields() 
    {
        if (regFullName     != null) regFullName.clear();
        if (regStudentID    != null) regStudentID.clear();
        if (regContactNumber != null) regContactNumber.clear();
        if (comboGradeLevel != null) comboGradeLevel.getSelectionModel().clearSelection();
        if (comboSection    != null) { comboSection.getItems().clear(); comboSection.getSelectionModel().clearSelection(); }
        if (regFullName     != null) regFullName.requestFocus();
    }

    // =========================================================
    // VIEW  →  ViewStu.fxml
    @FXML 
    void handleView(ActionEvent event) 
    {
        resetSidebarColors(); applySmoothDarken(btnView);
        loadScreen("ViewStu.fxml");
    }

    @FXML
    void handleLoadRecords(ActionEvent event) 
    {
        applySmoothDarken((Button) event.getSource());

        if (viewTable != null) 
        {
            loadViewRecords();
        } else if (deleteTable != null) {
            loadDeleteRecords();
        }
    }

    private void loadViewRecords() 
    {
        String grade   = (filterGrade   != null) ? filterGrade.getValue()   : null;
        String section = (filterSection != null) ? filterSection.getValue() : null;
        String session = (filterSession != null) ? filterSession.getValue() : null;
        LocalDate date = (filterDate    != null) ? filterDate.getValue()    : null;

        if (grade   == null || grade.isEmpty())   { showAlert("Missing", "Select a Grade Level.", Alert.AlertType.WARNING); return; }
        if (section == null || section.isEmpty()) { showAlert("Missing", "Select a Section.",     Alert.AlertType.WARNING); return; }

        ObservableList<Student> list = FXCollections.observableArrayList();

        try (Connection conn = DatabaseConnection.getConnection()) {
            PreparedStatement ps;

            if (date != null && session != null && !session.isEmpty()) 
            {
                String timeStart = session.startsWith("Time-in") ? "06:00:00" : "12:00:00";
                String timeEnd   = session.startsWith("Time-in") ? "11:59:59" : "18:00:00";
                ps = conn.prepareStatement(
                    "SELECT DISTINCT s.student_id, s.full_name, s.parent_number, s.grade_level, s.section, " +
                    "       TO_CHAR(a.scan_time, 'HH:MI:SS AM') AS scan_time " +
                    "FROM students s INNER JOIN attendance_logs a ON s.student_id = a.student_id " +
                    "WHERE s.grade_level=? AND s.section=? AND a.scan_date=? " +
                    "  AND a.scan_time>=?::time AND a.scan_time<=?::time ORDER BY s.full_name");
                ps.setString(1, grade); ps.setString(2, section);
                ps.setDate(3, java.sql.Date.valueOf(date));
                ps.setString(4, timeStart); ps.setString(5, timeEnd);

            } else if (date != null) 
            {
                ps = conn.prepareStatement(
                    "SELECT DISTINCT s.student_id, s.full_name, s.parent_number, s.grade_level, s.section, " +
                    "       TO_CHAR(a.scan_time, 'HH:MI:SS AM') AS scan_time " +
                    "FROM students s INNER JOIN attendance_logs a ON s.student_id = a.student_id " +
                    "WHERE s.grade_level=? AND s.section=? AND a.scan_date=? ORDER BY s.full_name");
                ps.setString(1, grade); ps.setString(2, section);
                ps.setDate(3, java.sql.Date.valueOf(date));

            } else {
                // No date — show all enrolled students, attendance = "Not recorded"
                ps = conn.prepareStatement(
                    "SELECT student_id, full_name, parent_number, grade_level, section " +
                    "FROM students WHERE grade_level=? AND section=? ORDER BY full_name");
                ps.setString(1, grade); ps.setString(2, section);
            }

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String attendance = "";
                try { attendance = rs.getString("scan_time"); } catch (Exception e) { attendance = "No record"; }
                list.add(new Student(
                    rs.getString("full_name"),
                    rs.getString("student_id"),
                    rs.getString("parent_number"),
                    rs.getString("grade_level"),
                    rs.getString("section"),
                    attendance));
            }
            ps.close();

        } catch (SQLException ex) {
            showAlert("Database Error", ex.getMessage(), Alert.AlertType.ERROR); ex.printStackTrace();
        }

        viewTable.setItems(list); viewTable.refresh();
        if (list.isEmpty())
            showAlert("No Records", "No students found for the selected filters.", Alert.AlertType.INFORMATION);
    }
        private void loadDeleteRecords() 
        {
            String grade   = (deleteFilterGrade   != null) ? deleteFilterGrade.getValue()   : null;
            String section = (deleteFilterSection != null) ? deleteFilterSection.getValue() : null;

            if (grade   == null || grade.isEmpty())   { showAlert("Missing", "Select a Grade Level.", Alert.AlertType.WARNING); return; }
            if (section == null || section.isEmpty()) { showAlert("Missing", "Select a Section.",     Alert.AlertType.WARNING); return; }

            ObservableList<Student> list = fetchStudents(grade, section);

            deleteTable.setItems(list);
            deleteTable.refresh();

            if (list.isEmpty())
                showAlert("No Records",
                        "No students in " + grade + " – " + section + ".", Alert.AlertType.INFORMATION);
        }

    @FXML
    void handleClearAttendance(ActionEvent event)
    {
        String grade   = (filterGrade   != null) ? filterGrade.getValue()   : null;
        String section = (filterSection != null) ? filterSection.getValue() : null;
        LocalDate date = (filterDate    != null) ? filterDate.getValue()    : null;

        if (grade == null || grade.isEmpty())   { showAlert("Missing", "Select a Grade Level.", Alert.AlertType.WARNING); return; }
        if (section == null || section.isEmpty()) { showAlert("Missing", "Select a Section.",   Alert.AlertType.WARNING); return; }
        if (date == null) { showAlert("Missing", "Select a Date to clear attendance for.", Alert.AlertType.WARNING); return; }

        Alert c = new Alert(Alert.AlertType.CONFIRMATION,
                "Delete ALL attendance records for\n" + grade + " – " + section + "\non " + date + "?\n\nThis CANNOT be undone.",
                ButtonType.YES, ButtonType.NO);
        c.setTitle("Confirm Clear Attendance");
        c.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                try (Connection conn = DatabaseConnection.getConnection();
                    PreparedStatement ps = conn.prepareStatement(
                        "DELETE FROM attendance_logs WHERE scan_date=? " +
                        "AND student_id IN (SELECT student_id FROM students WHERE grade_level=? AND section=?)")) {
                    ps.setDate(1, java.sql.Date.valueOf(date));
                    ps.setString(2, grade);
                    ps.setString(3, section);
                    int rows = ps.executeUpdate();
                    if (viewTable != null) viewTable.getItems().clear();
                    showAlert("Done", rows + " attendance record(s) deleted.", Alert.AlertType.INFORMATION);
                } catch (SQLException ex) {
                    showAlert("Database Error", ex.getMessage(), Alert.AlertType.ERROR);
                    ex.printStackTrace();
                }
            }
        });
    }

    // =========================================================
    // UPDATE  →  UpdateStu.fxml
    @FXML 
    void handleUpdate(ActionEvent event) 
    {
        resetSidebarColors(); applySmoothDarken(btnUpdate);
        loadScreen("UpdateStu.fxml");
    }

    @FXML
    void handleFindStudent(ActionEvent event) {
        applySmoothDarken((Button) event.getSource());
        String id = getText(searchID);
        if (id.isEmpty()) { showAlert("Input Required", "Enter a Student ID to search.", Alert.AlertType.WARNING); return; }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM students WHERE student_id=?")) {
            ps.setString(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) 
            {
                if (updateStudentID   != null) updateStudentID.setText(rs.getString("student_id"));
                if (updateFullName    != null) updateFullName.setText(rs.getString("full_name"));
               if (updateContactNumber != null) updateContactNumber.setText(
                    rs.getString("parent_number") != null ? rs.getString("parent_number") : "");
                if (comboGradeLevel != null) {
                    comboGradeLevel.setValue(rs.getString("grade_level"));
                    updateSections(comboGradeLevel, comboSection);
                }
                if (comboSection != null) comboSection.setValue(rs.getString("section"));
            } else {
                showAlert("Not Found", "No student with ID: " + id, Alert.AlertType.WARNING);
            }
        } catch (SQLException ex) {
            showAlert("Database Error", ex.getMessage(), Alert.AlertType.ERROR); ex.printStackTrace();
        }
    }

    @FXML
    void handleSaveChanges(ActionEvent event) 
    {
        applySmoothDarken((Button) event.getSource());
        String id = (updateStudentID != null) ? getText(updateStudentID) : "";
        if (id.isEmpty()) { showAlert("No Student", "Use FIND to load a student first.", Alert.AlertType.WARNING); return; }

        String name    = (updateFullName    != null) ? updateFullName.getText().trim()    : "";
        String contactNumber = (updateContactNumber != null) ? updateContactNumber.getText().trim() : "";
        String grade   = (comboGradeLevel   != null) ? comboGradeLevel.getValue()         : null;
        String section = (comboSection      != null) ? comboSection.getValue()            : null;

        if (name.isEmpty() || grade == null || section == null) 
        {
        showAlert("Missing Fields", "Name, Grade and Section cannot be empty.", Alert.AlertType.WARNING); return;
        }

        try (Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(
                 "UPDATE students SET full_name=?, parent_number=?, grade_level=?, section=? WHERE student_id=?")) {
            ps.setString(1, name);
            ps.setString(2, contactNumber.isEmpty() ? null : contactNumber);
            ps.setString(3, grade);
            ps.setString(4, section);
            ps.setString(5, id);

            if (ps.executeUpdate() > 0) 
            {
                showAlert("Updated!", "Student record updated successfully.", Alert.AlertType.INFORMATION);
                clearUpdateFields(); // ← clear AFTER success only
            } else {
                showAlert("Not Found", "No record updated. Check the Student ID.", Alert.AlertType.WARNING);
            }
            } catch (SQLException ex) {
                showAlert("Database Error", ex.getMessage(), Alert.AlertType.ERROR); ex.printStackTrace();
            }
    }

    private void clearUpdateFields() 
    {
        if (searchID          != null) searchID.clear();
        if (updateStudentID   != null) updateStudentID.clear();
        if (updateFullName    != null) updateFullName.clear();
        if (updateContactNumber != null) updateContactNumber.clear();
        if (comboGradeLevel   != null) comboGradeLevel.getSelectionModel().clearSelection();
        if (comboSection      != null) { comboSection.getItems().clear(); comboSection.getSelectionModel().clearSelection(); }
        if (searchID          != null) searchID.requestFocus();
    }

    // =========================================================
    // DELETE  →  Delete.fxml
    @FXML 
    void handleDelete(ActionEvent event) 
    {
        resetSidebarColors(); applySmoothDarken(btnDelete);
        loadScreen("Delete.fxml");
    }

    @FXML
    void handleDeleteIndividual(ActionEvent event) {
        String targetId = getText(deleteStudentID);
        if (targetId.isEmpty()) { showAlert("Input Required", "Type a Student ID to delete.", Alert.AlertType.WARNING); return; }

        boolean found = deleteTable != null &&
                deleteTable.getItems().stream().anyMatch(s -> s.getStudentId().equals(targetId));
        if (!found) {
            showAlert("Not in List",
                    "ID \"" + targetId + "\" is not in the loaded list.\nClick Show Students first.",
                    Alert.AlertType.WARNING);
            return;
        }

        Alert c = new Alert(Alert.AlertType.CONFIRMATION,
                "Permanently delete student ID: " + targetId + "?", ButtonType.YES, ButtonType.NO);
        c.setTitle("Confirm Delete");
        c.showAndWait().ifPresent(btn -> { if (btn == ButtonType.YES) deleteById(targetId); });
    }

    @FXML
    void handleDeleteAllInSection(ActionEvent event) 
    {
        applySmoothRed((Button) event.getSource());

        if (deleteTable == null || deleteTable.getItems().isEmpty()) 
        {
            showAlert("Nothing to Delete", "Click Show Students to load a section first.", Alert.AlertType.WARNING);
            return;
        }
        String grade   = (deleteFilterGrade   != null) ? deleteFilterGrade.getValue()   : null;
        String section = (deleteFilterSection != null) ? deleteFilterSection.getValue() : null;
        if (grade == null || section == null) 
        {
            showAlert("No Section", "Select Grade and Section, then click Show Students.", Alert.AlertType.WARNING);
            return;
        }

        int count = deleteTable.getItems().size();
        Alert c = new Alert(Alert.AlertType.CONFIRMATION,
                "Delete ALL " + count + " student(s) in " + grade + " – " + section + "?\nThis CANNOT be undone.",
                ButtonType.YES, ButtonType.NO);
        c.setTitle("Confirm Bulk Delete");
        c.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) 
            {
                try (Connection conn = DatabaseConnection.getConnection();
                     PreparedStatement ps = conn.prepareStatement(
                             "DELETE FROM students WHERE grade_level=? AND section=?")) {
                    ps.setString(1, grade); ps.setString(2, section);
                    int rows = ps.executeUpdate();
                    deleteTable.getItems().clear();
                    showAlert("Done", rows + " student(s) deleted.", Alert.AlertType.INFORMATION);
                } catch (SQLException ex) {
                    showAlert("Database Error", ex.getMessage(), Alert.AlertType.ERROR); ex.printStackTrace();
                }
            }
        });
    }

    private void deleteById(String studentId) 
    {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM students WHERE student_id=?")) {
            ps.setString(1, studentId);
            if (ps.executeUpdate() > 0) 
            {
                if (deleteStudentID != null) deleteStudentID.clear();
                if (deleteTable     != null) {
                    deleteTable.getItems().removeIf(s -> s.getStudentId().equals(studentId));
                    deleteTable.refresh();
                }
                showAlert("Deleted", "Student " + studentId + " removed.", Alert.AlertType.INFORMATION);
            } else {
                showAlert("Not Found", "No student with ID: " + studentId, Alert.AlertType.WARNING);
            }
        } catch (SQLException ex) {
            showAlert("Database Error", ex.getMessage(), Alert.AlertType.ERROR); ex.printStackTrace();
        }
    }

    // =========================================================
    // SUMMARY  →  Summary.fxml
    @FXML 
    void handleSummary(ActionEvent event)
    {
        resetSidebarColors(); applySmoothDarken(btnSummary);
        loadScreen("Summary.fxml");
    }

    @FXML
    void handleGenerateReport(ActionEvent event) 
    {
        applySmoothDarken((Button) event.getSource());
        if (attendanceChart == null) return;

        String monthName = (summaryMonth   != null) ? summaryMonth.getValue()   : null;
        String yearStr   = (summaryYear    != null) ? summaryYear.getValue()    : null;
        String grade     = (summaryGrade   != null) ? summaryGrade.getValue()   : null;
        String section   = (summarySection != null) ? summarySection.getValue() : null;

        if (monthName == null) { showAlert("Missing", "Select a Month.",       Alert.AlertType.WARNING); return; }
        if (yearStr   == null) { showAlert("Missing", "Select a Year.",        Alert.AlertType.WARNING); return; }
        if (grade     == null) { showAlert("Missing", "Select a Grade Level.", Alert.AlertType.WARNING); return; }
        if (section   == null) { showAlert("Missing", "Select a Section.",     Alert.AlertType.WARNING); return; }

        int monthNum = Arrays.asList("January","February","March","April","May","June",
                "July","August","September","October","November","December").indexOf(monthName) + 1;
        int year = Integer.parseInt(yearStr);

        // Count total weekdays (Mon-Fri) in selected month/year
        int totalWeekdays = countWeekdays(year, monthNum);

        // ── BAR CHART ──────────────────────────────────────────
        String sql =
            "SELECT EXTRACT(DAY FROM a.scan_date)::int AS day_num, " +
            "       COUNT(DISTINCT a.student_id) AS present " +
            "FROM attendance_logs a " +
            "INNER JOIN students s ON a.student_id = s.student_id " +
            "WHERE s.grade_level=? AND s.section=? " +
            "  AND EXTRACT(MONTH FROM a.scan_date)=? " +
            "  AND EXTRACT(YEAR  FROM a.scan_date)=? " +
            "GROUP BY EXTRACT(DAY FROM a.scan_date) " +
            "ORDER BY EXTRACT(DAY FROM a.scan_date)";

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName(monthName + " " + year + " — " + grade + " " + section);

        try (Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, grade); ps.setString(2, section);
            ps.setInt(3, monthNum); ps.setInt(4, year);
            ResultSet rs = ps.executeQuery();
            while (rs.next())
                series.getData().add(new XYChart.Data<>("Day " + rs.getInt("day_num"), rs.getInt("present")));
            attendanceChart.getData().setAll(series);
        } catch (SQLException ex) {
            showAlert("Database Error", ex.getMessage(), Alert.AlertType.ERROR); ex.printStackTrace();
        }

        // ── STUDENT SUMMARY TABLE ───────────────────────────────
        ObservableList<AttendanceSummary> summaryList = FXCollections.observableArrayList();

        String sqlStudents =
            "SELECT s.student_id, s.full_name, " +
            "       COUNT(DISTINCT a.scan_date) AS present_days " +
            "FROM students s " +
            "LEFT JOIN attendance_logs a ON s.student_id = a.student_id " +
            "  AND EXTRACT(MONTH FROM a.scan_date)=? " +
            "  AND EXTRACT(YEAR  FROM a.scan_date)=? " +
            "WHERE s.grade_level=? AND s.section=? " +
            "GROUP BY s.student_id, s.full_name " +
            "ORDER BY s.full_name";

        try (Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sqlStudents)) {
            ps.setInt(1, monthNum); ps.setInt(2, year);
            ps.setString(3, grade); ps.setString(4, section);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int present = rs.getInt("present_days");
                int absent  = totalWeekdays - present;
                summaryList.add(new AttendanceSummary(
                    rs.getString("full_name"),
                    rs.getString("student_id"),
                    present,
                    absent < 0 ? 0 : absent));
            }
        } catch (SQLException ex) {
            showAlert("Database Error", ex.getMessage(), Alert.AlertType.ERROR); ex.printStackTrace();
        }

        if (summaryTable != null) { summaryTable.setItems(summaryList); summaryTable.refresh(); }

        if (summaryList.isEmpty())
            showAlert("No Data", "No records for " + monthName + " " + year +
                    " in " + grade + " – " + section + ".", Alert.AlertType.INFORMATION);
    }

    private int countWeekdays(int year, int month) 
    {
        java.time.YearMonth ym = java.time.YearMonth.of(year, month);
        int count = 0;
        for (int d = 1; d <= ym.lengthOfMonth(); d++) {
            java.time.DayOfWeek dow = java.time.LocalDate.of(year, month, d).getDayOfWeek();
            if (dow != java.time.DayOfWeek.SATURDAY && dow != java.time.DayOfWeek.SUNDAY)
                count++;
        }
        return count;
    }

    // =========================================================
    // GENERATE QR  →  Generate.fxml
    @FXML 
    void handleGenerate(ActionEvent event) 
    {
        resetSidebarColors(); applySmoothDarken(btnGenerate);
        loadScreen("Generate.fxml");
    }

    // Tracks which student is currently selected for QR preview
    private Student selectedStudentForQR = null;

    @FXML
    void handleFilterStudents(ActionEvent event) {
        String grade   = (genGradeBox   != null) ? genGradeBox.getValue()   : null;
        String section = (genSectionBox != null) ? genSectionBox.getValue() : null;

        if (grade == null || grade.isEmpty())   { showAlert("Missing", "Select a Grade Level.", Alert.AlertType.WARNING); return; }
        if (section == null || section.isEmpty()) { showAlert("Missing", "Select a Section.",   Alert.AlertType.WARNING); return; }

        ObservableList<Student> list = fetchStudents(grade, section);

        if (genStudentTable != null) 
        {
            genStudentTable.setItems(list);

            // NAME column
            TableColumn<Student, String> nameCol =
                (TableColumn<Student, String>)(Object) genStudentTable.getColumns().get(0);
                nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

            // ACTION column — "Create QR Code" button per row
            TableColumn<Student, Void> actionCol =
                (TableColumn<Student, Void>)(Object) genStudentTable.getColumns().get(1);
                 actionCol.setCellFactory(col -> new TableCell<>() 
            {
            private final Button btn = new Button("Create QR Code");
            {
                btn.setStyle("-fx-background-color: #2d5a4c; -fx-text-fill: white; "
                           + "-fx-background-radius: 8; -fx-cursor: hand;");
                btn.setOnAction(e -> {
                    Student student = getTableView().getItems().get(getIndex());
                    selectedStudentForQR = student;
                    generateQRPreview(student);
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });

        genStudentTable.refresh();
    }

    if (list.isEmpty())
        showAlert("No Students", "No students found in " + grade + " – " + section + ".", Alert.AlertType.INFORMATION);
    }

    private void generateQRPreview(Student student) 
    {
        try 
        {
            String qrContent = student.getStudentId();

            // Generate QR using ZXing
            com.google.zxing.qrcode.QRCodeWriter writer = new com.google.zxing.qrcode.QRCodeWriter();
            com.google.zxing.common.BitMatrix bitMatrix =
                    writer.encode(qrContent, com.google.zxing.BarcodeFormat.QR_CODE, 300, 300);

            // Convert BitMatrix to JavaFX Image
            javafx.scene.image.WritableImage writableImage = new javafx.scene.image.WritableImage(300, 300);
            javafx.scene.image.PixelWriter pixelWriter = writableImage.getPixelWriter();
            for (int x = 0; x < 300; x++)
                for (int y = 0; y < 300; y++)
                    pixelWriter.setColor(x, y, bitMatrix.get(x, y)
                            ? javafx.scene.paint.Color.BLACK
                            : javafx.scene.paint.Color.WHITE);

            if (imgQrCode != null) imgQrCode.setImage(writableImage);

        } catch (Exception ex) {
            showAlert("QR Error", "Could not generate QR code:\n" + ex.getMessage(), Alert.AlertType.ERROR);
            ex.printStackTrace();
        }
    }   

    @FXML
    void handleDownloadQR(ActionEvent event) 
    {
        if (selectedStudentForQR == null) {
            showAlert("No QR Generated", "Click 'Create QR Code' on a student first.", Alert.AlertType.WARNING);
            return;
        }
        if (imgQrCode == null || imgQrCode.getImage() == null) {
            showAlert("No QR Generated", "Click 'Create QR Code' on a student first.", Alert.AlertType.WARNING);
            return;
        }

        // File chooser to save the image
        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle("Save QR Code");
        fileChooser.setInitialFileName(selectedStudentForQR.getStudentId() + "_QR.png");
        fileChooser.getExtensionFilters().add(
            new javafx.stage.FileChooser.ExtensionFilter("PNG Image", "*.png"));

        java.io.File file = fileChooser.showSaveDialog(imgQrCode.getScene().getWindow());
        if (file != null) 
        {
            try 
            {
                java.awt.image.BufferedImage bufferedImage =
                javafx.embed.swing.SwingFXUtils.fromFXImage(imgQrCode.getImage(), null);
                javax.imageio.ImageIO.write(bufferedImage, "PNG", file);

            showAlert("Downloaded!", "QR Code saved to:\n" + file.getAbsolutePath(), Alert.AlertType.INFORMATION);
            } catch (Exception ex) {
            showAlert("Save Error", "Could not save image:\n" + ex.getMessage(), Alert.AlertType.ERROR);
            ex.printStackTrace();
            }
        }
    }

    @FXML
    void handleCancel(ActionEvent event) 
    {
        if (genGradeBox     != null) genGradeBox.getSelectionModel().clearSelection();
        if (genSectionBox   != null) { genSectionBox.getItems().clear(); genSectionBox.getSelectionModel().clearSelection(); }
        if (genStudentTable != null) genStudentTable.getItems().clear();
        if (imgQrCode       != null) imgQrCode.setImage(null);
        selectedStudentForQR = null;
    }

    //BACK TO MAIN BUTTON
    @FXML
    void handleBackToMain(ActionEvent event)
    {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("Startup.fxml"));
            Stage stage = (Stage) btnBack.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setMaximized(false);
            stage.setMaximized(true);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // =========================================================
    //  UTILITIES
    private String getText(TextField tf) 
    {
        return (tf != null && tf.getText() != null) ? tf.getText().trim() : "";
    }

    private ObservableList<Student> fetchStudents(String grade, String section) 
    {
        ObservableList<Student> list = FXCollections.observableArrayList();
        try (Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT student_id, full_name, parent_number, grade_level, section " +
                    "FROM students WHERE grade_level=? AND section=? ORDER BY full_name")) {
            ps.setString(1, grade); 
            ps.setString(2, section);
            ResultSet rs = ps.executeQuery();
            while (rs.next())
                list.add(new Student(rs.getString("full_name"), rs.getString("student_id"),
                rs.getString("parent_number"), rs.getString("grade_level"), rs.getString("section"), ""));
        } catch (SQLException ex) {
            showAlert("Database Error", "Fetch failed:\n" + ex.getMessage(), Alert.AlertType.ERROR);
            ex.printStackTrace();
        }
        return list;
    }
}