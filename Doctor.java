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

    public void addAppointment(Appointment appointment) {
        appointments.add(appointment);
    }

    public boolean removeAppointment(Appointment appointment) {
        return appointments.remove(appointment);
    }

    public void printUpcomingAppointments() {
        boolean found = false;
        for (Appointment a : appointments) {
            if (a.getStatus().equals("Scheduled")) {
                System.out.println("  " + a.getSummary());
                found = true;
            }
        }
        if (!found) System.out.println("  No upcoming appointments.");
    }

    public boolean isBusy(String dateTime) {
        for (Appointment a : appointments) {
            if (a.getDateTime().equals(dateTime) && a.getStatus().equals("Scheduled")) {
                return true;
            }
        }
        return false;
    }

    public String getName()       { return name; }
    public String getSpecialty()  { return specialty; }
    public ArrayList<Appointment> getAppointments() { return appointments; }

    @Override
    public String toString() {
        return name + " (" + specialty + ")";
    }
}