public class Student 
{
    private final String name, studentId, contactNumber, grade, section, attendance;
    
    public Student(String name, String studentId, String contactNumber, String grade, String section, String attendance) {
        this.name = name; this.studentId = studentId;
        this.contactNumber = (contactNumber != null) ? contactNumber : "";
        this.grade = grade; this.section = section;
        this.attendance = (attendance != null) ? attendance : "";
    }
    public String getName()          { return name; }
    public String getStudentId()     { return studentId; }
    public String getContactNumber() { return contactNumber; }
    public String getGrade()         { return grade; }
    public String getSection()       { return section; }
    public String getAttendance()    { return attendance; }
}