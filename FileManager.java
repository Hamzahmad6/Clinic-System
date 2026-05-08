import java.io.*;
import java.util.ArrayList;

public class FileManager {

    private static final String PATIENTS_FILE = "patients.csv";
    private static final String APPOINTMENTS_FILE = "appointments.csv";

    // ───────── SAVE PATIENTS ─────────
    public static void savePatients(ArrayList<Patient> patients) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(PATIENTS_FILE))) {
            for (Patient p : patients) {
                bw.write(p.getId() + "," + p.getName() + "," + p.getAge() + "," + p.getContactInfo());
                bw.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error saving patients: " + e.getMessage());
        }
    }

    // ───────── LOAD PATIENTS ─────────
    public static ArrayList<Patient> loadPatients() {
        ArrayList<Patient> patients = new ArrayList<>();
        File file = new File(PATIENTS_FILE);
        if (!file.exists()) return patients;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length < 4) continue;

                int id = Integer.parseInt(parts[0]);
                String name = parts[1];
                int age = Integer.parseInt(parts[2]);
                String contact = parts[3];

                patients.add(new Patient(id, name, age, contact));
            }
        } catch (IOException e) {
            System.out.println("Error loading patients: " + e.getMessage());
        }

        return patients;
    }

    // ───────── SAVE APPOINTMENTS ─────────
    public static void saveAppointments(ArrayList<Appointment> appointments) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(APPOINTMENTS_FILE))) {
            for (Appointment a : appointments) {
                bw.write(
                    a.getAppointmentId() + "," +
                    a.getPatientId() + "," +
                    a.getPatientName() + "," +
                    a.getDoctor().getName() + "," +
                    a.getDateTime() + "," +
                    a.getStatus()
                );
                bw.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error saving appointments: " + e.getMessage());
        }
    }

    // ───────── LOAD APPOINTMENTS ─────────
    public static ArrayList<Appointment> loadAppointments(ArrayList<Patient> patients,
                                                           ArrayList<Doctor> doctors) {

        ArrayList<Appointment> appointments = new ArrayList<>();
        File file = new File(APPOINTMENTS_FILE);
        if (!file.exists()) return appointments;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;

            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length < 6) continue;

                int apptId = Integer.parseInt(parts[0]);
                int patientId = Integer.parseInt(parts[1]);
                String patientName = parts[2];
                String doctorName = parts[3];
                String dateTime = parts[4];
                String status = parts[5];

                Doctor doctor = null;
                for (Doctor d : doctors) {
                    if (d.getName().equals(doctorName)) {
                        doctor = d;
                        break;
                    }
                }
                if (doctor == null) continue;

                Appointment appt = new Appointment(patientId, patientName, doctor, dateTime);
                appt.forceId(apptId);
                appt.setStatus(status);

                doctor.addAppointment(appt);

                for (Patient p : patients) {
                    if (p.getId() == patientId) {
                        p.addVisit(appt);
                        break;
                    }
                }

                appointments.add(appt);
            }

        } catch (IOException e) {
            System.out.println("Error loading appointments: " + e.getMessage());
        }

        return appointments;
    }
}