import java.util.ArrayList;

public class ClinicSystem {
    private ArrayList<Patient> patients;
    private ArrayList<Doctor> doctors;
    private ArrayList<Appointment> allAppointments;
    private int nextPatientId;

    public ClinicSystem() {
        doctors = new ArrayList<>();
        doctors.add(new Doctor("Dr. Ahmed Hassan",  "Cardiology"));
        doctors.add(new Doctor("Dr. Sara Mostafa",  "Dermatology"));
        doctors.add(new Doctor("Dr. Khaled Nour",   "General Practice"));
        doctors.add(new Doctor("Dr. Mona El-Sayed", "Pediatrics"));

        patients = FileManager.loadPatients();
        allAppointments = FileManager.loadAppointments(patients, doctors);

        nextPatientId = 1;
        for (Patient p : patients)
            if (p.getId() >= nextPatientId)
                nextPatientId = p.getId() + 1;
    }
    /** Returns null on success, error message on failure. */
    public String addPatient(String name, int age, String contact) {
        if (name == null || name.trim().isEmpty()) return "Name cannot be empty.";
        if (age <= 0) return "Age must be more than 0.";
        if (contact == null || contact.trim().isEmpty()) return "Contact info cannot be empty.";
        Patient p = new Patient(nextPatientId++, name.trim(), age, contact.trim());
        patients.add(p);
        FileManager.savePatients(patients);
        return null;
    }

    /** Returns null on success, error message on failure. */
    public String bookAppointment(int patientId, int doctorIndex, String dateTime) {
        Patient patient = findPatientById(patientId);
        if (patient == null) return "Patient not found.";
        if (doctorIndex < 0 || doctorIndex >= doctors.size()) return "Invalid doctor selection.";
        if (dateTime == null || dateTime.trim().isEmpty()) return "Date/time cannot be empty.";
        Doctor doctor = doctors.get(doctorIndex);
        if (doctor.isBusy(dateTime.trim())) return "Doctor is already booked at that time.";
        Appointment appt = new Appointment(patient.getId(), patient.getName(), doctor, dateTime.trim());
        allAppointments.add(appt);
        doctor.addAppointment(appt);
        patient.addVisit(appt);
        FileManager.saveAppointments(allAppointments);
        return null;
    }

    /** Returns null on success, error message on failure. */
    public String cancelAppointment(int apptId) {
        Appointment appt = findAppointmentById(apptId);
        if (appt == null) return "Appointment not found.";
        if (appt.getStatus().equals("Cancelled")) return "Already cancelled.";
        appt.setStatus("Cancelled");
        FileManager.saveAppointments(allAppointments);
        return null;
    }

    /** Returns null on success, error message on failure. */
    public String rescheduleAppointment(int apptId, String newDateTime) {
        Appointment appt = findAppointmentById(apptId);
        if (appt == null) return "Appointment not found.";
        if (appt.getStatus().equals("Cancelled")) return "Cannot reschedule a cancelled appointment.";
        if (newDateTime == null || newDateTime.trim().isEmpty()) return "New date/time cannot be empty.";
        if (appt.getDoctor().isBusy(newDateTime.trim())) return "Doctor is busy at that time.";
        appt.setDateTime(newDateTime.trim());
        FileManager.saveAppointments(allAppointments);
        return null;
    }

    public ArrayList<Patient>     getPatients()        { return patients; }
    public ArrayList<Doctor>      getDoctors()         { return doctors; }
    public ArrayList<Appointment> getAllAppointments()  { return allAppointments; }

    public ArrayList<Appointment> getScheduledAppointments() {
        ArrayList<Appointment> list = new ArrayList<>();
        for (Appointment a : allAppointments)
            if (a.getStatus().equals("Scheduled")) list.add(a);
        return list;
    }

    public ArrayList<Appointment> getAppointmentsByDoctor(int doctorIndex) {
        if (doctorIndex < 0 || doctorIndex >= doctors.size()) return new ArrayList<>();
        ArrayList<Appointment> list = new ArrayList<>();
        for (Appointment a : doctors.get(doctorIndex).getAppointments())
            if (a.getStatus().equals("Scheduled")) list.add(a);
        return list;
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