import java.util.ArrayList;

public class Doctor {

    private String name;
    private String specialty;
    private ArrayList<Appointment> appointments;

    public Doctor(String name, String specialty) {
        this.name = name;
        this.specialty = specialty;
        this.appointments = new ArrayList<>();
    }

    public void addAppointment(Appointment a) {
        appointments.add(a);
    }

    public boolean removeAppointment(Appointment a) {
        return appointments.remove(a);
    }

    public boolean isBusy(String time) {
        for (Appointment a : appointments) {
            if (a.getStatus().equals("Scheduled") &&
                a.getDateTime().equals(time)) {
                return true;
            }
        }
        return false;
    }

    public String getName() { return name; }
    public String getSpecialty() { return specialty; }

    public ArrayList<Appointment> getAppointments() {
        return appointments;
    }

    public String toString() {
        return name + " (" + specialty + ")";
    }
}
