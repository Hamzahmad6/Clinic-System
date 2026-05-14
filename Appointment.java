public class Appointment {
    private static int nextId = 1;

    private int appointmentId;
    private int patientId;
    private String patientName;
    private Doctor doctor;
    private String dateTime;
    private String status;

    public Appointment(int patientId, String patientName, Doctor doctor, String dateTime) {
        this.appointmentId = nextId++;
        this.patientId     = patientId;
        this.patientName   = patientName;
        this.doctor        = doctor;
        this.dateTime      = dateTime;
        this.status        = "Scheduled";
    }

    public String getSummary() {
        return "[Appt#" + appointmentId + "] Patient: " + patientName
             + " | Dr. " + doctor.getName()
             + " | " + dateTime
             + " | Status: " + status;
    }

    public int    getAppointmentId() { return appointmentId; }
    public int    getPatientId()     { return patientId; }
    public String getPatientName()   { return patientName; }
    public Doctor getDoctor()        { return doctor; }
    public String getDateTime()      { return dateTime; }
    public String getStatus()        { return status; }

    public void setStatus(String status)     { this.status = status; }
    public void setDateTime(String dateTime) { this.dateTime = dateTime; }

    public void forceId(int id) {
        this.appointmentId = id;
        if (id >= nextId) nextId = id + 1;
    }
}
