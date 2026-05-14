import javafx.application.Application;
import javafx.beans.property.*;
import javafx.collections.*;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.stage.*;

import java.util.ArrayList;

public class ClinicApp extends Application {

    private ClinicSystem clinic;

    // ─── Shared table models ───────────────────────────────────────────────
    private ObservableList<PatientRow>     patientRows     = FXCollections.observableArrayList();
    private ObservableList<AppointmentRow> appointmentRows = FXCollections.observableArrayList();

    // ─── Colour palette ───────────────────────────────────────────────────
    private static final String BG       = "#0f1923";
    private static final String CARD     = "#162130";
    private static final String ACCENT   = "#00bfa5";
    private static final String ACCENT2  = "#e53935";
    private static final String TEXT     = "#e8f0f7";
    private static final String SUBTEXT  = "#7a99b0";
    private static final String BORDER   = "#1e3045";

    private static final String BTN_STYLE =
        "-fx-background-color:" + ACCENT + ";" +
        "-fx-text-fill:#0f1923;" +
        "-fx-font-weight:bold;" +
        "-fx-font-size:13px;" +
        "-fx-padding:8 22;" +
        "-fx-background-radius:6;" +
        "-fx-cursor:hand;";
    private static final String BTN_DANGER =
        "-fx-background-color:" + ACCENT2 + ";" +
        "-fx-text-fill:white;" +
        "-fx-font-weight:bold;" +
        "-fx-font-size:13px;" +
        "-fx-padding:8 22;" +
        "-fx-background-radius:6;" +
        "-fx-cursor:hand;";
    private static final String BTN_GHOST =
        "-fx-background-color:transparent;" +
        "-fx-text-fill:" + ACCENT + ";" +
        "-fx-font-weight:bold;" +
        "-fx-font-size:13px;" +
        "-fx-padding:8 22;" +
        "-fx-background-radius:6;" +
        "-fx-border-color:" + ACCENT + ";" +
        "-fx-border-radius:6;" +
        "-fx-cursor:hand;";
    private static final String FIELD_STYLE =
        "-fx-background-color:#1e3045;" +
        "-fx-text-fill:" + TEXT + ";" +
        "-fx-prompt-text-fill:" + SUBTEXT + ";" +
        "-fx-border-color:" + BORDER + ";" +
        "-fx-border-radius:6;" +
        "-fx-background-radius:6;" +
        "-fx-padding:8;" +
        "-fx-font-size:13px;";
    private static final String COMBO_STYLE =
        "-fx-background-color:#1e3045;" +
        "-fx-text-fill:" + TEXT + ";" +
        "-fx-border-color:" + BORDER + ";" +
        "-fx-border-radius:6;" +
        "-fx-background-radius:6;" +
        "-fx-padding:4;" +
        "-fx-font-size:13px;";
    private static final String TABLE_STYLE =
        "-fx-background-color:#162130;" +
        "-fx-text-fill:" + TEXT + ";" +
        "-fx-table-cell-border-color:" + BORDER + ";";

    // ─── Dashboard stat labels (kept as fields so they can be refreshed) ──
    // BUG FIX #9: The original buildDashboard() used a Supplier<String> lambda
    // that was called only ONCE at build time and then discarded. After adding
    // a patient or booking an appointment the numbers on the dashboard never
    // changed. We now keep references to the Label nodes and call
    // refreshDashboard() every time data changes.
    private Label statPatients;
    private Label statScheduled;
    private Label statAllAppts;
    private Label statDoctors;

    // ──────────────────────────────────────────────────────────────────────
    @Override
    public void start(Stage stage) {
        clinic = new ClinicSystem();
        refreshPatientRows();
        refreshAppointmentRows();

        TabPane tabs = new TabPane();
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabs.setStyle(
            "-fx-tab-min-width:140;" +
            "-fx-background-color:" + BG + ";" +
            "-fx-focus-color:transparent;"
        );

        tabs.getTabs().addAll(
            makeTab("🏠  Dashboard",   buildDashboard()),
            makeTab("👤  Patients",     buildPatientsTab()),
            makeTab("📅  Book",         buildBookTab()),
            makeTab("📋  Appointments", buildAppointmentsTab()),
            makeTab("🔍  History",      buildHistoryTab())
        );

        Scene scene = new Scene(tabs, 1100, 720);
        scene.setFill(Color.web(BG));
        stage.setScene(scene);
        stage.setTitle("Clinic Management System");
        stage.show();
    }

    private Tab makeTab(String title, javafx.scene.Node content) {
        Tab t = new Tab(title);
        t.setContent(content);
        return t;
    }

    // ═══════════════════════════════════════════════════════
    //  DASHBOARD
    // ═══════════════════════════════════════════════════════
    private javafx.scene.Node buildDashboard() {
        VBox root = styled(new VBox(24));
        root.setPadding(new Insets(36));

        Label title = new Label("Clinic Management System");
        title.setStyle("-fx-font-size:28px;-fx-font-weight:bold;-fx-text-fill:" + ACCENT + ";");

        Label sub = new Label("Your all-in-one patient & appointment manager");
        sub.setStyle("-fx-font-size:14px;-fx-text-fill:" + SUBTEXT + ";");

        // BUG FIX #9 continued: create the stat cards with mutable Labels
        HBox cards = new HBox(20);
        VBox cardPatients  = buildStatCard("👤", "Total Patients",     "#00bfa5");
        VBox cardScheduled = buildStatCard("📅", "Scheduled",          "#42a5f5");
        VBox cardAllAppts  = buildStatCard("📋", "Total Appointments",  "#ab47bc");
        VBox cardDoctors   = buildStatCard("🩺", "Doctors",             "#ff7043");

        // The first child of each card is the icon+value label
        statPatients  = (Label) cardPatients.getChildren().get(0);
        statScheduled = (Label) cardScheduled.getChildren().get(0);
        statAllAppts  = (Label) cardAllAppts.getChildren().get(0);
        statDoctors   = (Label) cardDoctors.getChildren().get(0);

        refreshDashboard();

        cards.getChildren().addAll(cardPatients, cardScheduled, cardAllAppts, cardDoctors);

        Label docTitle = new Label("Our Doctors");
        docTitle.setStyle("-fx-font-size:18px;-fx-font-weight:bold;-fx-text-fill:" + TEXT + ";");

        HBox docCards = new HBox(16);
        for (Doctor d : clinic.getDoctors()) {
            VBox card = new VBox(6);
            card.setPadding(new Insets(16));
            card.setStyle(
                "-fx-background-color:" + CARD + ";" +
                "-fx-background-radius:10;" +
                "-fx-border-color:" + BORDER + ";" +
                "-fx-border-radius:10;"
            );
            card.setPrefWidth(210);
            Label nm = new Label(d.getName());
            nm.setStyle("-fx-text-fill:" + TEXT + ";-fx-font-weight:bold;-fx-font-size:13px;");
            nm.setWrapText(true);
            Label sp = new Label(d.getSpecialty());
            sp.setStyle("-fx-text-fill:" + ACCENT + ";-fx-font-size:12px;");
            card.getChildren().addAll(nm, sp);
            docCards.getChildren().add(card);
        }

        root.getChildren().addAll(title, sub, new Separator(), cards, docTitle, docCards);
        return root;
    }

    /** Creates a stat card. The first child is the icon+value Label. */
    private VBox buildStatCard(String icon, String labelText, String color) {
        VBox card = new VBox(4);
        card.setPadding(new Insets(20));
        card.setPrefWidth(200);
        card.setStyle(
            "-fx-background-color:" + CARD + ";" +
            "-fx-background-radius:12;" +
            "-fx-border-color:" + color + ";" +
            "-fx-border-radius:12;" +
            "-fx-border-width:2;"
        );
        Label ico = new Label(icon + "  —");   // placeholder; refreshDashboard() fills the real value
        ico.setStyle("-fx-font-size:22px;-fx-text-fill:" + color + ";-fx-font-weight:bold;");
        Label lbl = new Label(labelText);
        lbl.setStyle("-fx-text-fill:" + SUBTEXT + ";-fx-font-size:12px;");
        card.getChildren().addAll(ico, lbl);
        return card;
    }

    /** Updates the mutable dashboard stat labels with current counts. */
    private void refreshDashboard() {
        if (statPatients  != null) statPatients.setText("👤  " + clinic.getPatients().size());
        if (statScheduled != null) statScheduled.setText("📅  " + clinic.getScheduledAppointments().size());
        if (statAllAppts  != null) statAllAppts.setText("📋  " + clinic.getAllAppointments().size());
        if (statDoctors   != null) statDoctors.setText("🩺  " + clinic.getDoctors().size());
    }

    // ═══════════════════════════════════════════════════════
    //  PATIENTS TAB
    // ═══════════════════════════════════════════════════════
    private javafx.scene.Node buildPatientsTab() {
        VBox root = styled(new VBox(20));
        root.setPadding(new Insets(28));

        Label title = sectionTitle("Add New Patient");

        // Form
        GridPane form = new GridPane();
        form.setHgap(16); form.setVgap(12);
        form.setStyle("-fx-background-color:" + CARD + ";-fx-background-radius:12;-fx-padding:24;");

        TextField nameField    = field("Full name");
        TextField ageField     = field("Age");
        TextField contactField = field("Phone number or email address");

        form.add(label("Name"),         0, 0); form.add(nameField,    1, 0);
        form.add(label("Age"),          0, 1); form.add(ageField,     1, 1);
        form.add(label("Contact Info"), 0, 2); form.add(contactField, 1, 2);

        ColumnConstraints c0 = new ColumnConstraints(120);
        ColumnConstraints c1 = new ColumnConstraints();
        c1.setHgrow(Priority.ALWAYS);
        form.getColumnConstraints().addAll(c0, c1);

        Button addBtn  = new Button("Add Patient");
        addBtn.setStyle(BTN_STYLE);
        Label feedback = feedbackLabel();

        addBtn.setOnAction(e -> {
            String name    = nameField.getText().trim();
            String ageStr  = ageField.getText().trim();
            String contact = contactField.getText().trim();
            try {
                int age = Integer.parseInt(ageStr);
                String err = clinic.addPatient(name, age, contact);
                if (err != null) {
                    setError(feedback, err);
                } else {
                    setSuccess(feedback, "Patient added successfully!");
                    nameField.clear(); ageField.clear(); contactField.clear();
                    refreshPatientRows();
                    refreshDashboard(); // keep dashboard in sync
                }
            } catch (NumberFormatException ex) {
                setError(feedback, "Age must be a valid number.");
            }
        });

        HBox btnRow = new HBox(12, addBtn, feedback);
        btnRow.setAlignment(Pos.CENTER_LEFT);

        Label tTitle = sectionTitle("All Patients");
        TableView<PatientRow> table = patientTable();
        VBox.setVgrow(table, Priority.ALWAYS);

        root.getChildren().addAll(title, form, btnRow, tTitle, table);
        return root;
    }

    private TableView<PatientRow> patientTable() {
        TableView<PatientRow> tv = new TableView<>(patientRows);
        tv.setStyle(TABLE_STYLE);
        tv.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tv.getColumns().addAll(
            col("ID",      "id",      80),
            col("Name",    "name",    200),
            col("Age",     "age",     80),
            col("Contact", "contact", 300)
        );
        styleTable(tv);
        return tv;
    }

    // ═══════════════════════════════════════════════════════
    //  BOOK TAB
    // ═══════════════════════════════════════════════════════
    private javafx.scene.Node buildBookTab() {
        VBox root = styled(new VBox(20));
        root.setPadding(new Insets(28));

        Label title = sectionTitle("Book Appointment");

        GridPane form = new GridPane();
        form.setHgap(16); form.setVgap(14);
        form.setStyle("-fx-background-color:" + CARD + ";-fx-background-radius:12;-fx-padding:24;");

        // Patient combo — refreshed each time the dropdown opens
        ComboBox<String> patientCombo = styledCombo();
        patientCombo.setMaxWidth(Double.MAX_VALUE);
        patientCombo.setOnShowing(e -> {
            patientCombo.getItems().clear();
            for (Patient p : clinic.getPatients())
                patientCombo.getItems().add("[" + p.getId() + "] " + p.getName());
        });

        // Doctor combo — static list
        ComboBox<String> doctorCombo = styledCombo();
        for (Doctor d : clinic.getDoctors()) doctorCombo.getItems().add(d.toString());
        doctorCombo.setMaxWidth(Double.MAX_VALUE);

        TextField dtField = field("DD/MM/YYYY HH:MM");

        form.add(label("Patient"),     0, 0); form.add(patientCombo, 1, 0);
        form.add(label("Doctor"),      0, 1); form.add(doctorCombo,  1, 1);
        form.add(label("Date & Time"), 0, 2); form.add(dtField,      1, 2);

        ColumnConstraints c0 = new ColumnConstraints(130);
        ColumnConstraints c1 = new ColumnConstraints();
        c1.setHgrow(Priority.ALWAYS);
        form.getColumnConstraints().addAll(c0, c1);

        Button bookBtn = new Button("Book Appointment");
        bookBtn.setStyle(BTN_STYLE);
        Label feedback = feedbackLabel();

        bookBtn.setOnAction(e -> {
            if (patientCombo.getValue() == null) { setError(feedback, "Please select a patient."); return; }
            if (doctorCombo.getValue()  == null) { setError(feedback, "Please select a doctor.");  return; }
            String ptStr     = patientCombo.getValue();
            int    patientId = Integer.parseInt(ptStr.substring(1, ptStr.indexOf(']')));
            int    doctorIdx = doctorCombo.getSelectionModel().getSelectedIndex();
            String dt        = dtField.getText().trim();
            String err = clinic.bookAppointment(patientId, doctorIdx, dt);
            if (err != null) {
                setError(feedback, err);
            } else {
                setSuccess(feedback, "Appointment booked successfully!");
                patientCombo.setValue(null);
                doctorCombo.setValue(null);
                dtField.clear();
                refreshAppointmentRows();
                refreshDashboard(); // keep dashboard in sync
            }
        });

        HBox btnRow = new HBox(12, bookBtn, feedback);
        btnRow.setAlignment(Pos.CENTER_LEFT);

        root.getChildren().addAll(title, form, btnRow);
        return root;
    }

    // ═══════════════════════════════════════════════════════
    //  APPOINTMENTS TAB
    // ═══════════════════════════════════════════════════════
    private javafx.scene.Node buildAppointmentsTab() {
        VBox root = styled(new VBox(20));
        root.setPadding(new Insets(28));

        Label title = sectionTitle("All Appointments");

        // Filter bar
        HBox filterRow = new HBox(12);
        filterRow.setAlignment(Pos.CENTER_LEFT);
        ComboBox<String> filterCombo = styledCombo();
        filterCombo.getItems().addAll("All", "Scheduled", "Cancelled");
        filterCombo.setValue("All");
        filterRow.getChildren().addAll(label("Show:"), filterCombo);

        TableView<AppointmentRow> table = appointmentTable();
        VBox.setVgrow(table, Priority.ALWAYS);

        filterCombo.setOnAction(e -> {
            String f = filterCombo.getValue();
            if ("All".equals(f)) {
                table.setItems(appointmentRows);
            } else {
                ObservableList<AppointmentRow> filtered = FXCollections.observableArrayList();
                for (AppointmentRow r : appointmentRows)
                    if (r.getStatus().equals(f)) filtered.add(r);
                table.setItems(filtered);
            }
        });

        // ── Action panel ──────────────────────────────────────
        VBox actionBox = new VBox(14);
        actionBox.setStyle("-fx-background-color:" + CARD + ";-fx-background-radius:12;-fx-padding:20;");

        Label actTitle = new Label("Actions");
        actTitle.setStyle("-fx-font-size:15px;-fx-font-weight:bold;-fx-text-fill:" + TEXT + ";");

        // Cancel row
        HBox cancelRow = new HBox(12);
        cancelRow.setAlignment(Pos.CENTER_LEFT);
        TextField cancelIdField = field("Appointment ID");
        cancelIdField.setPrefWidth(140);
        Button cancelBtn = new Button("Cancel Appointment");
        cancelBtn.setStyle(BTN_DANGER);
        cancelRow.getChildren().addAll(label("ID:"), cancelIdField, cancelBtn);

        // Reschedule row
        HBox reschedRow = new HBox(12);
        reschedRow.setAlignment(Pos.CENTER_LEFT);
        TextField reschedIdField = field("Appointment ID");
        reschedIdField.setPrefWidth(140);
        TextField newDtField = field("New DD/MM/YYYY HH:MM");
        newDtField.setPrefWidth(200);
        Button reschedBtn = new Button("Reschedule");
        reschedBtn.setStyle(BTN_GHOST);
        reschedRow.getChildren().addAll(label("ID:"), reschedIdField, label("New Time:"), newDtField, reschedBtn);

        Label feedback = feedbackLabel();

        cancelBtn.setOnAction(e -> {
            try {
                int id  = Integer.parseInt(cancelIdField.getText().trim());
                String err = clinic.cancelAppointment(id);
                if (err != null) {
                    setError(feedback, err);
                } else {
                    setSuccess(feedback, "Appointment #" + id + " cancelled.");
                    cancelIdField.clear();
                    refreshAppointmentRows();
                    refreshDashboard();
                    table.refresh();
                }
            } catch (NumberFormatException ex) { setError(feedback, "Invalid ID."); }
        });

        reschedBtn.setOnAction(e -> {
            try {
                int    id  = Integer.parseInt(reschedIdField.getText().trim());
                String dt  = newDtField.getText().trim();
                String err = clinic.rescheduleAppointment(id, dt);
                if (err != null) {
                    setError(feedback, err);
                } else {
                    setSuccess(feedback, "Rescheduled to: " + dt);
                    reschedIdField.clear();
                    newDtField.clear();
                    refreshAppointmentRows();
                    table.refresh();
                }
            } catch (NumberFormatException ex) { setError(feedback, "Invalid ID."); }
        });

        actionBox.getChildren().addAll(actTitle, cancelRow, reschedRow, feedback);
        root.getChildren().addAll(title, filterRow, table, actionBox);
        return root;
    }

    private TableView<AppointmentRow> appointmentTable() {
        TableView<AppointmentRow> tv = new TableView<>(appointmentRows);
        tv.setStyle(TABLE_STYLE);
        tv.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<AppointmentRow, String> statusCol = col("Status", "status", 100);
        statusCol.setCellFactory(c -> new TableCell<AppointmentRow, String>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                setText(item);
                setStyle("Scheduled".equals(item)
                    ? "-fx-text-fill:" + ACCENT  + ";-fx-font-weight:bold;"
                    : "-fx-text-fill:" + ACCENT2 + ";-fx-font-weight:bold;");
            }
        });

        tv.getColumns().addAll(
            col("Appt#",    "apptId",      60),
            col("Patient",  "patientName", 160),
            col("Doctor",   "doctorName",  200),
            col("DateTime", "dateTime",    170),
            statusCol
        );
        styleTable(tv);
        return tv;
    }

    // ═══════════════════════════════════════════════════════
    //  HISTORY TAB
    // ═══════════════════════════════════════════════════════
    private javafx.scene.Node buildHistoryTab() {
        VBox root = styled(new VBox(20));
        root.setPadding(new Insets(28));

        Label title = sectionTitle("Patient Visit History");

        HBox searchRow = new HBox(12);
        searchRow.setAlignment(Pos.CENTER_LEFT);
        ComboBox<String> patientCombo = styledCombo();
        patientCombo.setPrefWidth(280);
        Button viewBtn = new Button("View History");
        viewBtn.setStyle(BTN_STYLE);
        searchRow.getChildren().addAll(label("Patient:"), patientCombo, viewBtn);

        patientCombo.setOnShowing(e -> {
            patientCombo.getItems().clear();
            for (Patient p : clinic.getPatients())
                patientCombo.getItems().add("[" + p.getId() + "] " + p.getName());
        });

        ListView<String> historyList = new ListView<>();
        historyList.setStyle(
            "-fx-background-color:" + CARD + ";" +
            "-fx-border-color:" + BORDER + ";" +
            "-fx-border-radius:8;" +
            "-fx-background-radius:8;"
        );
        historyList.setCellFactory(lv -> new ListCell<String>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("-fx-background-color:" + CARD + ";");
                    return;
                }
                setText(item);
                setStyle(
                    "-fx-text-fill:" + TEXT + ";" +
                    "-fx-background-color:" + CARD + ";" +
                    "-fx-font-size:13px;" +
                    "-fx-padding:8;"
                );
            }
        });
        VBox.setVgrow(historyList, Priority.ALWAYS);

        viewBtn.setOnAction(e -> {
            historyList.getItems().clear();
            if (patientCombo.getValue() == null) return;
            String sel = patientCombo.getValue();
            int id = Integer.parseInt(sel.substring(1, sel.indexOf(']')));
            for (Patient p : clinic.getPatients()) {
                if (p.getId() == id) {
                    ArrayList<Appointment> visits = p.getVisitHistory();
                    if (visits.isEmpty()) {
                        historyList.getItems().add("No visits recorded.");
                    } else {
                        for (Appointment a : visits)
                            historyList.getItems().add(a.getSummary());
                    }
                    break;
                }
            }
        });

        root.getChildren().addAll(title, searchRow, historyList);
        return root;
    }

    // ═══════════════════════════════════════════════════════
    //  DATA ROW MODELS (for TableView)
    // ═══════════════════════════════════════════════════════
    public static class PatientRow {
        private final SimpleIntegerProperty id;
        private final SimpleStringProperty  name, age, contact;
        PatientRow(Patient p) {
            id      = new SimpleIntegerProperty(p.getId());
            name    = new SimpleStringProperty(p.getName());
            age     = new SimpleStringProperty(String.valueOf(p.getAge()));
            contact = new SimpleStringProperty(p.getContactInfo());
        }
        public int    getId()      { return id.get(); }
        public String getName()    { return name.get(); }
        public String getAge()     { return age.get(); }
        public String getContact() { return contact.get(); }
        public IntegerProperty idProperty()      { return id; }
        public StringProperty  nameProperty()    { return name; }
        public StringProperty  ageProperty()     { return age; }
        public StringProperty  contactProperty() { return contact; }
    }

    public static class AppointmentRow {
        private final SimpleIntegerProperty apptId;
        private final SimpleStringProperty  patientName, doctorName, dateTime, status;
        AppointmentRow(Appointment a) {
            apptId      = new SimpleIntegerProperty(a.getAppointmentId());
            patientName = new SimpleStringProperty(a.getPatientName());
            doctorName  = new SimpleStringProperty(a.getDoctor().getName());
            dateTime    = new SimpleStringProperty(a.getDateTime());
            status      = new SimpleStringProperty(a.getStatus());
        }
        public int    getApptId()      { return apptId.get(); }
        public String getPatientName() { return patientName.get(); }
        public String getDoctorName()  { return doctorName.get(); }
        public String getDateTime()    { return dateTime.get(); }
        public String getStatus()      { return status.get(); }
        public IntegerProperty apptIdProperty()      { return apptId; }
        public StringProperty  patientNameProperty() { return patientName; }
        public StringProperty  doctorNameProperty()  { return doctorName; }
        public StringProperty  dateTimeProperty()    { return dateTime; }
        public StringProperty  statusProperty()      { return status; }
    }

    // ═══════════════════════════════════════════════════════
    //  HELPERS
    // ═══════════════════════════════════════════════════════
    private void refreshPatientRows() {
        patientRows.clear();
        for (Patient p : clinic.getPatients()) patientRows.add(new PatientRow(p));
    }

    private void refreshAppointmentRows() {
        appointmentRows.clear();
        for (Appointment a : clinic.getAllAppointments()) appointmentRows.add(new AppointmentRow(a));
    }

    private VBox styled(VBox box) {
        box.setStyle("-fx-background-color:" + BG + ";");
        return box;
    }

    private Label sectionTitle(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-size:20px;-fx-font-weight:bold;-fx-text-fill:" + TEXT + ";");
        return l;
    }

    private Label label(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-text-fill:" + SUBTEXT + ";-fx-font-size:13px;");
        return l;
    }

    private TextField field(String prompt) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        tf.setStyle(FIELD_STYLE);
        return tf;
    }

    /** Creates a styled ComboBox with consistent dark-theme cell colours. */
    private ComboBox<String> styledCombo() {
        ComboBox<String> cb = new ComboBox<>();
        cb.setStyle(COMBO_STYLE);
        cb.setButtonCell(new ListCell<String>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item);
                setStyle("-fx-text-fill:" + TEXT + ";-fx-background-color:#1e3045;");
            }
        });
        cb.setCellFactory(list -> new ListCell<String>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item);
                setStyle("-fx-text-fill:" + TEXT + ";-fx-background-color:#1e3045;");
            }
        });
        return cb;
    }

    private Label feedbackLabel() {
        Label l = new Label();
        l.setStyle("-fx-font-size:13px;");
        return l;
    }

    private void setError(Label l, String msg) {
        l.setText("✖  " + msg);
        l.setStyle("-fx-text-fill:" + ACCENT2 + ";-fx-font-size:13px;");
    }

    private void setSuccess(Label l, String msg) {
        l.setText("✔  " + msg);
        l.setStyle("-fx-text-fill:" + ACCENT + ";-fx-font-size:13px;");
    }

    @SuppressWarnings("unchecked")
    private <T> TableColumn<T, String> col(String header, String property, int minW) {
        TableColumn<T, String> c = new TableColumn<>(header);
        c.setCellValueFactory(new PropertyValueFactory<>(property));
        c.setMinWidth(minW);
        c.setStyle("-fx-text-fill:" + TEXT + ";-fx-font-size:13px;");
        return c;
    }

    private <T> void styleTable(TableView<T> tv) {
        tv.setStyle(TABLE_STYLE +
            "-fx-selection-bar:" + BORDER + ";" +
            "-fx-selection-bar-non-focused:" + BORDER + ";");
        tv.setPlaceholder(new Label("No data") {{
            setStyle("-fx-text-fill:" + SUBTEXT + ";-fx-font-size:13px;");
        }});
    }

    public static void main(String[] args) { launch(args); }
}
