import java.util.ArrayList;

public class Patient {
    private int id;
    private String name;
    private int age;
    private String contactInfo;
    private ArrayList<Appointment> visitHistory;

    public Patient(int id, String name, int age, String contactInfo) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.contactInfo = contactInfo;
        this.visitHistory = new ArrayList<>();
    }

    public void addVisit(Appointment appointment) {
        visitHistory.add(appointment);
    }

    public void printVisitHistory() {
        if (visitHistory.isEmpty()) {
            System.out.println("  No visits recorded.");
            return;
        }
        for (Appointment a : visitHistory) {
            System.out.println("  - " + a.getSummary());
        }
    }

    public int getId()              { return id; }
    public String getName()         { return name; }
    public int getAge()             { return age; }
    public String getContactInfo()  { return contactInfo; }
    public ArrayList<Appointment> getVisitHistory() { return visitHistory; }

    public void setName(String name)           { this.name = name; }
    public void setAge(int age)                { this.age = age; }
    public void setContactInfo(String c)       { this.contactInfo = c; }

    @Override
    public String toString() {
        return "[ID: " + id + "] " + name + " | Age: " + age + " | Contact: " + contactInfo;
    }
}