package com.example.edulocker.models;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.Exclude;

import java.util.ArrayList;
import java.util.List;

public class Teacher {
    private String teacherId;
    private String userId;
    private String schoolId;
    private String schoolName;
    private String name;
    private String subject;
    private String assignedClass;   // legacy — first class (kept for compat)
    private String division;        // legacy — first division
    private List<ClassAssignment> classAssignments = new ArrayList<>();
    private String phone;
    private String loginEmail;
    private String loginPassword;
    private Timestamp createdAt;

    public Teacher() {}

    public String getTeacherId() { return teacherId; }
    public void setTeacherId(String teacherId) { this.teacherId = teacherId; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getSchoolId() { return schoolId; }
    public void setSchoolId(String schoolId) { this.schoolId = schoolId; }
    public String getSchoolName() { return schoolName; }
    public void setSchoolName(String schoolName) { this.schoolName = schoolName; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public String getAssignedClass() { return assignedClass; }
    public void setAssignedClass(String assignedClass) { this.assignedClass = assignedClass; }
    public String getDivision() { return division; }
    public void setDivision(String division) { this.division = division; }
    public List<ClassAssignment> getClassAssignments() { return classAssignments; }
    public void setClassAssignments(List<ClassAssignment> classAssignments) {
        this.classAssignments = classAssignments != null ? classAssignments : new ArrayList<>();
    }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getLoginEmail() { return loginEmail; }
    public void setLoginEmail(String loginEmail) { this.loginEmail = loginEmail; }
    @Exclude
    public String getLoginPassword() { return loginPassword; }
    public void setLoginPassword(String loginPassword) { this.loginPassword = loginPassword; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
