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

    public void addVisit(Appointment a) {
        visitHistory.add(a);
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public int getAge() { return age; }
    public String getContactInfo() { return contactInfo; }

    public ArrayList<Appointment> getVisitHistory() {
        return visitHistory;
    }
}