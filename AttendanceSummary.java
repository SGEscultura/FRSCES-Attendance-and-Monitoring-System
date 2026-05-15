public class AttendanceSummary 
{
    private final String name, studentId;
    private final int presentDays, absentDays;

    public AttendanceSummary(String name, String studentId, int presentDays, int absentDays) 
    {
        this.name = name; this.studentId = studentId;
        this.presentDays = presentDays; this.absentDays = absentDays;
    }
    public String getName()       { return name; }
    public String getStudentId()  { return studentId; }
    public int getPresentDays()   { return presentDays; }
    public int getAbsentDays()    { return absentDays; }
}