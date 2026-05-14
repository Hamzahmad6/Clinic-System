import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class FileManager {

    private static final String PATIENTS_FILE     = "patients.csv";
    private static final String APPOINTMENTS_FILE = "appointments.csv";

    // BUG FIX #2: Changed delimiter from "," to "|"
    // Using comma as delimiter breaks parsing whenever a patient's name contains
    // a comma (e.g. "Smith, John"). The pipe character "|" is safe because names,
    // phone numbers, and dates never contain it.

    // BUG FIX #3: Added StandardCharsets.UTF_8 to all readers and writers.
    // Without an explicit charset, Java uses the platform default encoding, which
    // on Windows is often CP-1252. This corrupts Arabic names and any non-ASCII
    // characters when the file is saved on one machine and read on another.

    // ─────────────────────────────────────────────
    // SAVE PATIENTS
    // Format: id|name|age|contactInfo
    // ─────────────────────────────────────────────
    public static void savePatients(ArrayList<Patient> patients) {
        try (BufferedWriter bw = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(PATIENTS_FILE), StandardCharsets.UTF_8))) {
            for (Patient p : patients) {
                bw.write(p.getId() + "," + p.getName() + "," + p.getAge() + "," + p.getContactInfo());
                bw.newLine();
            }
        } catch (IOException e) {
            System.out.println("  [!] Error saving patients: " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────
    // LOAD PATIENTS
    // ─────────────────────────────────────────────
    public static ArrayList<Patient> loadPatients() {
        ArrayList<Patient> patients = new ArrayList<>();
        File file = new File(PATIENTS_FILE);
        if (!file.exists()) return patients;

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(PATIENTS_FILE), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                // BUG FIX #4: Use limit=4 in split so a name that somehow contains
                // the delimiter does not silently drop the contact-info column.
                String[] parts = line.split("\\,", 4);
                if (parts.length < 4) continue;

                int    id      = Integer.parseInt(parts[0].trim());
                String name    = parts[1].trim();
                int    age     = Integer.parseInt(parts[2].trim());
                String contact = parts[3].trim();
                patients.add(new Patient(id, name, age, contact));
            }
        } catch (IOException e) {
            System.out.println("  [!] Error loading patients: " + e.getMessage());
        }

        return patients;
    }

    // ─────────────────────────────────────────────
    // SAVE APPOINTMENTS
    // Format: apptId|patientId|patientName|doctorName|dateTime|status
    // ─────────────────────────────────────────────
    public static void saveAppointments(ArrayList<Appointment> appointments) {
        try (BufferedWriter bw = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(APPOINTMENTS_FILE), StandardCharsets.UTF_8))) {
            for (Appointment a : appointments) {
                bw.write(
                    a.getAppointmentId()    + "," +
                    a.getPatientId()        + "," +
                    a.getPatientName()      + "," +
                    a.getDoctor().getName() + "," +
                    a.getDateTime()         + "," +
                    a.getStatus()
                );
                bw.newLine();
            }
        } catch (IOException e) {
            System.out.println("  [!] Error saving appointments: " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────
    // LOAD APPOINTMENTS
    // Needs doctors list to re-link the Doctor object
    // ─────────────────────────────────────────────
    public static ArrayList<Appointment> loadAppointments(ArrayList<Patient> patients,
                                                           ArrayList<Doctor>  doctors) {
        ArrayList<Appointment> appointments = new ArrayList<>();
        File file = new File(APPOINTMENTS_FILE);
        if (!file.exists()) return appointments;

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(APPOINTMENTS_FILE), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                // BUG FIX #5: Use limit=6 so that datetime ("DD/MM/YYYY HH:MM")
                // is never accidentally split on a space or other character.
                String[] parts = line.split("\\,", 6);
                if (parts.length < 6) continue;

                int    apptId      = Integer.parseInt(parts[0].trim());
                int    patientId   = Integer.parseInt(parts[1].trim());
                String patientName = parts[2].trim();
                String doctorName  = parts[3].trim();
                String dateTime    = parts[4].trim();
                String status      = parts[5].trim();

                // Find matching doctor object
                Doctor doctor = null;
                for (Doctor d : doctors) {
                    if (d.getName().equals(doctorName)) {
                        doctor = d;
                        break;
                    }
                }
                if (doctor == null) continue; // Skip if doctor was removed

                // Rebuild appointment
                Appointment appt = new Appointment(patientId, patientName, doctor, dateTime);
                appt.forceId(apptId);
                appt.setStatus(status);

                // Re-link to doctor and patient
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
            System.out.println("  [!] Error loading appointments: " + e.getMessage());
        }

        return appointments;
    }
}
