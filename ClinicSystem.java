import java.util.ArrayList; // Import ArrayList to store dynamic lists of patients, doctors, and appointments

public class ClinicSystem { // Main system class that controls all clinic operations

    private ArrayList<Patient> patients; // List to store all patients in the system
    private ArrayList<Doctor> doctors; // List to store all doctors in the system
    private ArrayList<Appointment> allAppointments; // List to store all appointments in the system

    private int nextPatientId; // Counter to generate unique patient IDs

    public ClinicSystem() { // Constructor: runs when system starts

        doctors = new ArrayList<>(); // Initialize doctors list

        // Add predefined doctors to the system
        doctors.add(new Doctor("Dr. Ahmed Hassan", "Cardiology")); // Doctor 1
        doctors.add(new Doctor("Dr. Sara Mostafa", "Dermatology")); // Doctor 2
        doctors.add(new Doctor("Dr. Khaled Nour", "General")); // Doctor 3
        doctors.add(new Doctor("Dr. Mona El-Sayed", "Pediatrics")); // Doctor 4

        patients = FileManager.loadPatients(); // Load saved patients from file
        allAppointments = FileManager.loadAppointments(patients, doctors); // Load saved appointments and link them

        nextPatientId = 1; // Start patient ID counter from 1

        // Find highest existing ID to avoid duplicates
        for (Patient p : patients) { // Loop through all loaded patients

            if (p.getId() >= nextPatientId) { // If patient ID is bigger or equal
                nextPatientId = p.getId() + 1; // Set next ID to be safe
            }
        }
    }

    // ─────────────────────────────────────────────
    // ADD PATIENT
    // Returns null if success, error message otherwise
    // ─────────────────────────────────────────────
    public String addPatient(String name, int age, String contact) {

        if (name == null || name.trim().isEmpty()) { // Check if name is empty
            return "Name cannot be empty."; // Return error message
        }

        if (age <= 0) { // Check if age is valid
            return "Age must be more than 0."; // Return error
        }

        if (contact == null || contact.trim().isEmpty()) { // Check contact info
            return "Contact info cannot be empty."; // Return error
        }

        // Check for duplicate patient using contact info
        for (Patient p : patients) { // Loop through patients

            if (p.getContactInfo().equals(contact.trim())) { // If contact already exists
                return "Patient already exists with this contact."; // Stop duplication
            }
        }

        // Create new patient object
        Patient patient = new Patient(
                nextPatientId++, // Assign unique ID then increment counter
                name.trim(), // Remove extra spaces
                age, // Store age
                contact.trim() // Clean contact
        );

        patients.add(patient); // Add patient to list

        FileManager.savePatients(patients); // Save updated list to file

        return null; // Success (no error)
    }

    // ─────────────────────────────────────────────
    // BOOK APPOINTMENT
    // ─────────────────────────────────────────────
    public String bookAppointment(int patientId, int doctorIndex, String dateTime) {

        Patient patient = findPatientById(patientId); // Search for patient

        if (patient == null) { // If patient not found
            return "Patient not found."; // Error message
        }

        if (doctorIndex < 0 || doctorIndex >= doctors.size()) { // Validate doctor index
            return "Invalid doctor selection."; // Error message
        }

        if (dateTime == null || dateTime.trim().isEmpty()) { // Validate time
            return "Date/time cannot be empty."; // Error message
        }

        Doctor doctor = doctors.get(doctorIndex); // Get selected doctor

        if (doctor.isBusy(dateTime.trim())) { // Check if doctor is already booked
            return "Doctor is already booked at that time."; // Conflict error
        }

        // Create new appointment
        Appointment appointment = new Appointment(
                patient.getId(), // patient ID
                patient.getName(), // patient name
                doctor, // doctor object
                dateTime.trim() // appointment time
        );

        allAppointments.add(appointment); // Add to global list

        doctor.addAppointment(appointment); // Add to doctor schedule

        patient.addVisit(appointment); // Add to patient history

        FileManager.saveAppointments(allAppointments); // Save to file

        return null; // Success
    }

    // ─────────────────────────────────────────────
    // CANCEL APPOINTMENT
    // ─────────────────────────────────────────────
    public String cancelAppointment(int appointmentId) {

        Appointment appointment = findAppointmentById(appointmentId); // Find appointment

        if (appointment == null) { // If not found
            return "Appointment not found."; // Error
        }

        if (appointment.getStatus().equals("Cancelled")) { // Already cancelled check
            return "Already cancelled."; // Error
        }

        appointment.setStatus("Cancelled"); // Update status

        appointment.getDoctor().removeAppointment(appointment); // Remove from doctor schedule

        FileManager.saveAppointments(allAppointments); // Save changes

        return null; // Success
    }

    // ─────────────────────────────────────────────
    // RESCHEDULE APPOINTMENT
    // ─────────────────────────────────────────────
    public String rescheduleAppointment(int appointmentId, String newDateTime) {

        Appointment appointment = findAppointmentById(appointmentId); // Find appointment

        if (appointment == null) { // If not found
            return "Appointment not found."; // Error
        }

        if (appointment.getStatus().equals("Cancelled")) { // Cannot reschedule cancelled
            return "Cannot reschedule a cancelled appointment."; // Error
        }

        if (newDateTime == null || newDateTime.trim().isEmpty()) { // Validate new time
            return "New date/time cannot be empty."; // Error
        }

        // Check if doctor is already busy at new time (excluding same appointment)
        for (Appointment a : appointment.getDoctor().getAppointments()) {

            if (a.getStatus().equals("Scheduled") &&
                a.getDateTime().equals(newDateTime.trim()) &&
                a.getAppointmentId() != appointmentId) {

                return "Doctor is busy at that time."; // Conflict
            }
        }

        appointment.setDateTime(newDateTime.trim()); // Update time

        FileManager.saveAppointments(allAppointments); // Save changes

        return null; // Success
    }

    // ─────────────────────────────────────────────
    // GETTERS
    // ─────────────────────────────────────────────
    public ArrayList<Patient> getPatients() {
        return patients; // Return patient list
    }

    public ArrayList<Doctor> getDoctors() {
        return doctors; // Return doctor list
    }

    public ArrayList<Appointment> getAllAppointments() {
        return allAppointments; // Return all appointments
    }

    // ─────────────────────────────────────────────
    // FILTER: SCHEDULED ONLY
    // ─────────────────────────────────────────────
    public ArrayList<Appointment> getScheduledAppointments() {

        ArrayList<Appointment> scheduled = new ArrayList<>(); // Create list

        for (Appointment a : allAppointments) { // Loop all appointments

            if (a.getStatus().equals("Scheduled")) { // Filter only scheduled
                scheduled.add(a); // Add to result list
            }
        }

        return scheduled; // Return filtered list
    }

    // ─────────────────────────────────────────────
    // FIND PATIENT BY ID
    // ─────────────────────────────────────────────
    private Patient findPatientById(int id) {

        for (Patient p : patients) { // Loop patients

            if (p.getId() == id) { // Match ID
                return p; // Return patient
            }
        }

        return null; // Not found
    }

    // ─────────────────────────────────────────────
    // FIND APPOINTMENT BY ID
    // ─────────────────────────────────────────────
    private Appointment findAppointmentById(int id) {

        for (Appointment a : allAppointments) { // Loop appointments

            if (a.getAppointmentId() == id) { // Match ID
                return a; // Return appointment
            }
        }

        return null; // Not found
    }
}