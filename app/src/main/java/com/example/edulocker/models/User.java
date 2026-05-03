package com.example.edulocker.models;

import com.google.firebase.Timestamp;

public class User {
    public static final String ROLE_GOVERNMENT = "government";
    public static final String ROLE_SCHOOL = "school";
    public static final String ROLE_TEACHER = "teacher";
    public static final String ROLE_PARENT = "parent";
    public static final String ROLE_STUDENT = "student";

    private String uid;
    private String name;
    private String email;
    private String phone;
    private String role;
    private String schoolId;   // set for school/teacher users so it's in the doc from the start
    private Timestamp createdAt;

    public User() {}

    public User(String uid, String name, String email, String phone, String role) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.role = role;
        this.createdAt = Timestamp.now();
    }

    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getSchoolId() { return schoolId; }
    public void setSchoolId(String schoolId) { this.schoolId = schoolId; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
