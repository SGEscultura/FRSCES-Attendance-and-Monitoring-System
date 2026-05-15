public class ScanRecord 
{
    private final String time, name, email, grade, section;
    public ScanRecord(String time, String name, String email, String grade, String section) 
    {
        this.time = time; this.name = name; this.email = email;
        this.grade = grade; this.section = section;
    }
    public String getTime()    { return time; }
    public String getName()    { return name; }
    public String getEmail()   { return email; }
    public String getGrade()   { return grade; }
    public String getSection() { return section; }
}