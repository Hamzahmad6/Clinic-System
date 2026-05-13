import java.util.ArrayList;
public class ClinicSystem {
    private ArrayList<Patient> patients;
    private ArrayList<Doctor> doctors;
    private ArrayList<Appointment> allAppointments;
    private int nextPatientId;

    public ClinicSystem() {
        // doctors
        doctors = new ArrayList<>();
        doctors.add(new Doctor("Dr. Ahmed Hassan", "Cardiology"));
        doctors.add(new Doctor("Dr. Sara Mostafa", "Dermatology"));
        doctors.add(new Doctor("Dr. Khaled Nour", "General Practice"));
        doctors.add(new Doctor("Dr. Mona El-Sayed", "Pediatrics"));
        doctors.add(new Doctor("Dr. Malek Ahmed", "Neurology"));

        // Load patients from CSV file
        patients = FileManager.loadPatients();
        allAppointments = FileManager.loadAppointments(patients, doctors);

        nextPatientId = 1;
        for (Patient patient : patients) {

            // Check if patient ID is greater than or equal to next ID
            if (patient.getId() >= nextPatientId) {
                // Set next ID to current patient ID + 1
                nextPatientId = patient.getId() + 1;
            }
        }
    }

    // Method to add a new patient
    // Returns null if successful
    // Returns error message if validation fails
    public String addPatient(String name, int age, String contact) {
        name = name.trim();
        contact = contact.trim();
        if (name.isEmpty()) {
            return "Name cannot be empty.";
        }

        // Check if name contains only letters and spaces
        if (!name.matches("[a-zA-Z ]+")) {
            return "Name must contain letters only.";
        }

        if (age <= 0) {
            return "Age must be greater than 0.";
        }

        if (contact.isEmpty()) {
            return "Contact info cannot be empty.";
        }

        // Check if phone number contains exactly 11 digits
        if (!contact.matches("\\d{11}")) {
            return "Phone number must contain exactly 11 digits.";
        }

        // Validate Egyptian phone number prefixes
        if (!(contact.startsWith("010") || contact.startsWith("011") 
            || contact.startsWith("012") || contact.startsWith("015"))) {
                return "Invalid Egyptian phone number.";
        }

        Patient patient = new Patient(nextPatientId++,name,age,contact);

        patients.add(patient); // Add patient to patients list
        FileManager.savePatients(patients); // Save patients to CSV file
        return null; // Return null to indicate success

    }

    public String bookAppointment(int patientId, int doctorIndex, String dateTime) {

        Patient patient = findPatientById(patientId);
        if (patient == null) {
            return "Patient not found.";
        }

        // Check if doctor index is invalid
        if (doctorIndex < 0 || doctorIndex >= doctors.size()) {
            return "Invalid doctor selection.";
        }

        // Check if date/time is empty
        if (dateTime == null || dateTime.trim().isEmpty()) {

            // Return error message
            return "Date/time cannot be empty.";
        }

        // Remove extra spaces from date/time
        dateTime = dateTime.trim();

        // Validate date/time format
        if (!dateTime.matches(
                "(0[1-9]|[12][0-9]|3[01])/"
              + "(0[1-9]|1[0-2])/"
              + "\\d{4} "
              + "([01][0-9]|2[0-3]):"
              + "[0-5][0-9]"
        )) {

            // Return error message
            return "Use format: DD/MM/YYYY HH:MM";
        }

        // Get selected doctor object
        Doctor doctor = doctors.get(doctorIndex);

        // Check if doctor already has appointment at same time
        if (doctor.isBusy(dateTime)) {

            // Return error message
            return "Doctor is already booked at that time.";
        }

        // Create new appointment object
        Appointment appointment = new Appointment(

                // Store patient ID
                patient.getId(),

                // Store patient name
                patient.getName(),

                // Store doctor object
                doctor,

                // Store appointment date/time
                dateTime
        );

        // Add appointment to all appointments list
        allAppointments.add(appointment);

        // Add appointment to doctor's schedule
        doctor.addAppointment(appointment);

        // Add appointment to patient's visit history
        patient.addVisit(appointment);

        // Save appointments to CSV file
        FileManager.saveAppointments(allAppointments);

        // Return null to indicate success
        return null;
    }

    // Method to cancel appointment
    public String cancelAppointment(int appointmentId) {

        // Find appointment using ID
        Appointment appointment = findAppointmentById(appointmentId);

        // Check if appointment exists
        if (appointment == null) {

            // Return error message
            return "Appointment not found.";
        }

        // Check if appointment is already cancelled
        if (appointment.getStatus().equals("Cancelled")) {

            // Return error message
            return "Appointment already cancelled.";
        }

        // Change appointment status to cancelled
        appointment.setStatus("Cancelled");

        // Save updated appointments to file
        FileManager.saveAppointments(allAppointments);

        // Return null to indicate success
        return null;
    }

    // Method to reschedule appointment
    public String rescheduleAppointment(int appointmentId, String newDateTime) {

        // Find appointment using ID
        Appointment appointment = findAppointmentById(appointmentId);

        // Check if appointment exists
        if (appointment == null) {

            // Return error message
            return "Appointment not found.";
        }

        // Check if appointment is cancelled
        if (appointment.getStatus().equals("Cancelled")) {

            // Return error message
            return "Cannot reschedule a cancelled appointment.";
        }

        // Check if new date/time is empty
        if (newDateTime == null || newDateTime.trim().isEmpty()) {

            // Return error message
            return "New date/time cannot be empty.";
        }

        // Remove extra spaces from date/time
        newDateTime = newDateTime.trim();

        // Validate date/time format
        if (!newDateTime.matches(
                "(0[1-9]|[12][0-9]|3[01])/"
              + "(0[1-9]|1[0-2])/"
              + "\\d{4} "
              + "([01][0-9]|2[0-3]):"
              + "[0-5][0-9]"
        )) {

            // Return error message
            return "Use format: DD/MM/YYYY HH:MM";
        }

        // Check if doctor is busy at new time
        if (appointment.getDoctor().isBusy(newDateTime)) {

            // Return error message
            return "Doctor is busy at that time.";
        }

        // Update appointment date/time
        appointment.setDateTime(newDateTime);

        // Save updated appointments to file
        FileManager.saveAppointments(allAppointments);

        // Return null to indicate success
        return null;
    }

    // Method to return all patients
    public ArrayList<Patient> getPatients() {

        // Return patients list
        return patients;
    }

    // Method to return all doctors
    public ArrayList<Doctor> getDoctors() {

        // Return doctors list
        return doctors;
    }

    // Method to return all appointments
    public ArrayList<Appointment> getAllAppointments() {

        // Return appointments list
        return allAppointments;
    }

    // Method to return only scheduled appointments
    public ArrayList<Appointment> getScheduledAppointments() {

        // Create new list for scheduled appointments
        ArrayList<Appointment> scheduledAppointments = new ArrayList<>();

        // Loop through all appointments
        for (Appointment appointment : allAppointments) {

            // Check if appointment is scheduled
            if (appointment.getStatus().equals("Scheduled")) {

                // Add appointment to filtered list
                scheduledAppointments.add(appointment);
            }
        }

        // Return filtered list
        return scheduledAppointments;
    }

    // Method to get appointments for specific doctor
    public ArrayList<Appointment> getAppointmentsByDoctor(int doctorIndex) {

        // Validate doctor index
        if (doctorIndex < 0 || doctorIndex >= doctors.size()) {

            // Return empty list
            return new ArrayList<>();
        }

        // Create filtered appointments list
        ArrayList<Appointment> doctorAppointments = new ArrayList<>();

        // Loop through doctor's appointments
        for (Appointment appointment : doctors.get(doctorIndex).getAppointments()) {

            // Check if appointment is scheduled
            if (appointment.getStatus().equals("Scheduled")) {

                // Add appointment to filtered list
                doctorAppointments.add(appointment);
            }
        }

        // Return filtered list
        return doctorAppointments;
    }

    // Helper method to find patient using ID
    private Patient findPatientById(int id) {

        // Loop through all patients
        for (Patient patient : patients) {

            // Compare patient IDs
            if (patient.getId() == id) {

                // Return matching patient
                return patient;
            }
        }

        // Return null if not found
        return null;
    }

    // Helper method to find appointment using ID
    private Appointment findAppointmentById(int id) {

        // Loop through all appointments
        for (Appointment appointment : allAppointments) {

            // Compare appointment IDs
            if (appointment.getAppointmentId() == id) {

                // Return matching appointment
                return appointment;
            }
        }

        // Return null if appointment not found
        return null;
    }
}
