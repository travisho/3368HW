package sample;

import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.text.Font;

import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;
import java.util.UUID;

public class Controller implements Initializable {
    @FXML
    TableView<Student> studentList;
    @FXML
    Button filterButton;
    @FXML
    Button createDatabase;
    @FXML
    Button loadDatabase;
    @FXML
    ComboBox filterBox = new ComboBox();
    @FXML
    TextField nameField;
    @FXML
    TextField ageField;
    @FXML
    TextField majorField;
    @FXML
    TextField gpaField;
    @FXML
    Button submitButton;


    final String hostname = "db1exam.cyxopo5okovi.us-east-1.rds.amazonaws.com";
    final String dbName = "Exam1DB";
    final String port = "3306";
    final String userName = "travisho";
    final String password = "travisho";
    final String AWS_URL = "jdbc:mysql://" + hostname + ":" + port + "/" + dbName + "?user=" + userName + "&password=" + password;

    ObservableList<Student> studentView = FXCollections.observableArrayList();


    public void initialize(URL url, ResourceBundle resourceBundle) {
        studentList.setItems(studentView);
        populateTable();

        TableColumn<Student, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(f -> new ReadOnlyStringWrapper(f.getValue().name));

        TableColumn<Student, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(f -> new ReadOnlyStringWrapper(String.valueOf(f.getValue().id)));

        TableColumn<Student, String> ageCol = new TableColumn<>("Age");
        ageCol.setCellValueFactory(f -> new ReadOnlyStringWrapper(String.valueOf(f.getValue().age)));

        TableColumn<Student, String> majorCol = new TableColumn<>("Major");
        majorCol.setCellValueFactory(f -> new ReadOnlyStringWrapper(f.getValue().major));

        TableColumn<Student, String> gpaCol = new TableColumn<>("GPA");
        gpaCol.setCellValueFactory(f -> new ReadOnlyStringWrapper(String.valueOf(f.getValue().gpa)));

        studentList.getColumns().add(nameCol);
        studentList.getColumns().add(ageCol);
        studentList.getColumns().add(majorCol);
        studentList.getColumns().add(gpaCol);
        studentList.getColumns().add(idCol);

        createDatabase.setOnAction(e -> {
            try {
                Connection conn = DriverManager.getConnection(AWS_URL);
                Statement stmt = conn.createStatement();
                DatabaseMetaData meta = conn.getMetaData();
                ResultSet res = meta.getTables(null, null, "StudentsHW2", null);
                if(res.next()){
                } else {
                    String sql = "CREATE TABLE StudentsHW2(" +
                            "name VARCHAR(128)," +
                            "id VARCHAR(255)," +
                            "age INT(64)," +
                            "major VARCHAR(255)," +
                            "gpa DOUBLE(3,2))";
                    stmt.execute(sql);
                }

                } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });

        loadDatabase.setOnAction(e-> {
            clearDatabase();
            createStudent("Ana", 18, "Biology", 4.0);
            createStudent("John", 20, "Physics", 2.4);
            createStudent("Henry", 23, "Computer Science", 1.1);
            createStudent("Trenton", 31, "Physics", 3.2);
            createStudent("Trevor", 28, "Health", 3.8);
            createStudent("Helena", 60, "Religious Studies", 3.1);
            createStudent("Wu", 30, "Chinese Studies", 4.0);
            createStudent("Joan", 18, "French", 1.6);
            createStudent("Alfred", 31, "Teaching and Learning", 4.0);
            createStudent("Diana", 21, "Theater", 3.5);
            populateTable();
        });

        ObservableList<String> filters = FXCollections.observableArrayList("None", "Below 2.0 GPA", "Above 3.0 GPA", "Physics Majors", "Older than 30");
        filterBox.getItems().addAll(filters);
        filterButton.setOnAction(e -> {
            populateTable();
        });

        submitButton.setOnAction(e -> {
            String name = nameField.getText();
            int age = Integer.parseInt(ageField.getText());
            String major = majorField.getText();
            Double gpa = Double.parseDouble(gpaField.getText());
            createStudent(name, age, major, gpa);

            nameField.clear();
            ageField.clear();
            majorField.clear();
            gpaField.clear();
            populateTable();
        });
    }

    public void clearDatabase(){
        try {
            Connection conn = DriverManager.getConnection(AWS_URL);
            Statement stmt = conn.createStatement();
            String sql = "DELETE FROM StudentsHW2";
            stmt.execute(sql);

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public void createStudent(String name, int age, String major, double gpa){
        try {
            UUID uuid = UUID.randomUUID();
            String id = String.valueOf(uuid);
            Connection conn = DriverManager.getConnection(AWS_URL);

            PreparedStatement ps = conn.prepareStatement("INSERT INTO StudentsHW2 (studentname, id, age, major, gpa) VALUES (?,?,?,?,?)");
            ps.setString(1, name);
            ps.setString(2, id);
            ps.setInt(3, age);
            ps.setString(4, major);
            ps.setDouble(5, gpa);
            ps.execute();

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public void populateTable(){
        studentView.clear();
        try {
            Connection startConn = DriverManager.getConnection(AWS_URL);
            Statement startStmt = startConn.createStatement();
            String startSQL = "Select * From StudentsHW2";
            ResultSet startResult = startStmt.executeQuery(startSQL);

            while (startResult.next()) {
                Student student = new Student();
                student.name = startResult.getString("studentname");
                student.age = startResult.getInt("age");
                student.major = startResult.getString("major");
                student.gpa = startResult.getDouble("gpa");

                String wid = startResult.getString("id");
                UUID uuid = UUID.fromString(wid);
                student.id = uuid;

            if (filterBox.getValue() == null) {
                    studentView.add(student);
                } else if (filterStudents(student)) {
                    studentView.add(student);
                }
            }
            } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean filterStudents(Student student) {
        boolean go = false;
        switch (String.valueOf(filterBox.getValue())) {
            case "None":
                go = true;
                break;
            case "Below 2.0 GPA":
                if (student.gpa < 2.0) {
                    go = true;
                }
                break;

            case "Above 3.0 GPA":
                if (student.gpa > 3.0) {
                    go = true;
                }
                break;

            case "Physics Majors":
                if (student.major.equals("Physics")) {
                    go = true;
                }
                break;

            case "Older than 30":
                if (student.age > 30) {
                    go = true;
                }
        }
        return go;
    }

}