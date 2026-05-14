import java.util.ArrayList;

public class ClinicSystem {
    private ArrayList<Patient>     patients;
    private ArrayList<Doctor>      doctors;
    private ArrayList<Appointment> allAppointments;
    private int nextPatientId;

    public ClinicSystem() {
        doctors = new ArrayList<>();
        doctors.add(new Doctor("Dr. Ahmed Hassan", "Cardiology"));
        doctors.add(new Doctor("Dr. Sara Mostafa",  "Dermatology"));
        doctors.add(new Doctor("Dr. Khaled Nour",   "General Practice"));
        doctors.add(new Doctor("Dr. Mona El-Sayed", "Pediatrics"));
        doctors.add(new Doctor("Dr. Malek Ahmed",   "Neurology"));

        patients= FileManager.loadPatients();
        allAppointments=FileManager.loadAppointments(patients, doctors);

        nextPatientId = 1;
        for (Patient p : patients) {
            if (p.getId() >= nextPatientId) {
                nextPatientId = p.getId() + 1;
            }
        }
    }

    public String addPatient(String name, int age, String contact) {
        name=name.trim();
        contact=contact.trim();

        if (name.isEmpty()) {
            return "Name cannot be empty.";
        }

        if (!name.matches("[a-zA-Z\\s'\\-]+")) {
            return "Name must contain letters only.";
        }

        if (age <= 0) {
            return "Age must be more than 0.";
        }

        if (contact.isEmpty()) {
            return "Contact info cannot be empty.";
        }

        boolean isPhone = contact.matches("\\+?\\d{7,15}");
        boolean isEmail = contact.matches("^[\\w._%+\\-]+@[\\w.\\-]+\\.[a-zA-Z]{2,}$");
        if (!isPhone && !isEmail) {
            return "Enter a valid phone number (7–15 digits) or email address.";
        }

        Patient patient = new Patient(nextPatientId++, name, age, contact);
        patients.add(patient);
        FileManager.savePatients(patients);
        return null;
    }

    public String bookAppointment(int patientId, int doctorIndex, String dateTime) {
        Patient patient = findPatientById(patientId);
        if (patient == null) {
            return "Patient not found.";
        }

        if (doctorIndex < 0 || doctorIndex >= doctors.size()) {
            return "Invalid doctor selection.";
        }

        if (dateTime == null || dateTime.trim().isEmpty()) {
            return "Date/time cannot be empty.";
        }

        dateTime = dateTime.trim();

        if (!dateTime.matches(
                "(0[1-9]|[12][0-9]|3[01])/"
              + "(0[1-9]|1[0-2])/"
              + "\\d{4} "
              + "([01][0-9]|2[0-3]):"
              + "[0-5][0-9]")) {
            return "Use format: DD/MM/YYYY HH:MM";
        }

        Doctor doctor = doctors.get(doctorIndex);

        if (doctor.isBusy(dateTime)) {
            return "Doctor is already booked at that time.";
        }

        Appointment appointment = new Appointment(
            patient.getId(),
            patient.getName(),
            doctor,
            dateTime
        );

        allAppointments.add(appointment);
        doctor.addAppointment(appointment);
        patient.addVisit(appointment);
        FileManager.saveAppointments(allAppointments);
        return null;
    }

    public String cancelAppointment(int appointmentId) {
        Appointment appointment = findAppointmentById(appointmentId);
        if (appointment == null) {
            return "Appointment not found.";
        }
        if (appointment.getStatus().equals("Cancelled")) {
            return "Appointment is already cancelled.";
        }
        appointment.setStatus("Cancelled");
        FileManager.saveAppointments(allAppointments);
        return null;
    }

    public String rescheduleAppointment(int appointmentId, String newDateTime) {
        Appointment appointment = findAppointmentById(appointmentId);
        if (appointment == null) {
            return "Appointment not found.";
        }
        if (appointment.getStatus().equals("Cancelled")) {
            return "Cannot reschedule a cancelled appointment.";
        }
        if (newDateTime == null || newDateTime.trim().isEmpty()) {
            return "New date/time cannot be empty.";
        }

        newDateTime = newDateTime.trim();

        if (!newDateTime.matches(
                "(0[1-9]|[12][0-9]|3[01])/"
              + "(0[1-9]|1[0-2])/"
              + "\\d{4} "
              + "([01][0-9]|2[0-3]):"
              + "[0-5][0-9]")) {
            return "Use format: DD/MM/YYYY HH:MM";
        }

        Doctor doctor = appointment.getDoctor();
        for (Appointment a : doctor.getAppointments()) {
            if (a.getAppointmentId() != appointmentId
                    && a.getDateTime().equals(newDateTime)
                    && a.getStatus().equals("Scheduled")) {
                return "Doctor is busy at that time.";
            }
        }

        appointment.setDateTime(newDateTime);
        FileManager.saveAppointments(allAppointments);
        return null;
    }

    public ArrayList<Patient>     getPatients()             { return patients; }
    public ArrayList<Doctor>      getDoctors()              { return doctors; }
    public ArrayList<Appointment> getAllAppointments()       { return allAppointments; }

    public ArrayList<Appointment> getScheduledAppointments() {
        ArrayList<Appointment> result = new ArrayList<>();
        for (Appointment a : allAppointments)
            if (a.getStatus().equals("Scheduled"))
                result.add(a);
        return result;
    }

    public ArrayList<Appointment> getAppointmentsByDoctor(int doctorIndex) {
        if (doctorIndex < 0 || doctorIndex >= doctors.size())
            return new ArrayList<>();
        ArrayList<Appointment> result = new ArrayList<>();
        for (Appointment a : doctors.get(doctorIndex).getAppointments())
            if (a.getStatus().equals("Scheduled"))
                result.add(a);
        return result;
    }

    private Patient findPatientById(int id) {
        for (Patient p : patients)
            if (p.getId() == id) return p;
        return null;
    }

    private Appointment findAppointmentById(int id) {
        for (Appointment a : allAppointments)
            if (a.getAppointmentId() == id) return a;
        return null;
    }
}
