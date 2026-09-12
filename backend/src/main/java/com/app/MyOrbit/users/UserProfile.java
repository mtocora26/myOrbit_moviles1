package com.app.MyOrbit.users;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "user_profiles")
public class UserProfile {
    @Id
    private String userId;
    private String program;
    private String semester;
    private String studentCode;
    private String gradeTarget;

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getProgram() { return program; }
    public void setProgram(String program) { this.program = program; }
    public String getSemester() { return semester; }
    public void setSemester(String semester) { this.semester = semester; }
    public String getStudentCode() { return studentCode; }
    public void setStudentCode(String studentCode) { this.studentCode = studentCode; }
    public String getGradeTarget() { return gradeTarget; }
    public void setGradeTarget(String gradeTarget) { this.gradeTarget = gradeTarget; }
}