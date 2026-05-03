package com.example.edulocker.models;

import com.google.firebase.Timestamp;

public class Vacancy {
    private String vacancyId;
    private String schoolId;
    private String classStream;   // e.g. "Class 11 Science", "B.Tech CSE"
    private int seats;
    private String contactPhone;
    private String contactEmail;
    private Timestamp createdAt;

    public Vacancy() {}

    public String getVacancyId() { return vacancyId; }
    public void setVacancyId(String vacancyId) { this.vacancyId = vacancyId; }
    public String getSchoolId() { return schoolId; }
    public void setSchoolId(String schoolId) { this.schoolId = schoolId; }
    public String getClassStream() { return classStream; }
    public void setClassStream(String classStream) { this.classStream = classStream; }
    public int getSeats() { return seats; }
    public void setSeats(int seats) { this.seats = seats; }
    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }
    public String getContactEmail() { return contactEmail; }
    public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
