package com.example.edulocker.models;

import com.google.firebase.Timestamp;
import java.util.List;

public class ScholarshipApplication {
    public static final String STATUS_SUBMITTED    = "Submitted";
    public static final String STATUS_UNDER_REVIEW = "Under Review";
    public static final String STATUS_APPROVED     = "Approved";
    public static final String STATUS_REJECTED     = "Rejected";

    private String applicationId;
    private String passportId;
    private String studentName;
    private String studentClass;
    private String studentCategory;
    private String schoolId;
    private String scholarshipId;
    private String scholarshipTitle;
    private String status;
    private String reviewNote;          // rejection/approval note from reviewer
    private String reviewedByUserId;
    private List<String> attachedDocIds;
    private Timestamp submittedAt;
    private Timestamp reviewedAt;

    public ScholarshipApplication() {}

    public String getApplicationId() { return applicationId; }
    public void setApplicationId(String applicationId) { this.applicationId = applicationId; }
    public String getPassportId() { return passportId; }
    public void setPassportId(String passportId) { this.passportId = passportId; }
    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
    public String getStudentClass() { return studentClass; }
    public void setStudentClass(String studentClass) { this.studentClass = studentClass; }
    public String getStudentCategory() { return studentCategory; }
    public void setStudentCategory(String studentCategory) { this.studentCategory = studentCategory; }
    public String getSchoolId() { return schoolId; }
    public void setSchoolId(String schoolId) { this.schoolId = schoolId; }
    public String getScholarshipId() { return scholarshipId; }
    public void setScholarshipId(String scholarshipId) { this.scholarshipId = scholarshipId; }
    public String getScholarshipTitle() { return scholarshipTitle; }
    public void setScholarshipTitle(String scholarshipTitle) { this.scholarshipTitle = scholarshipTitle; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getReviewNote() { return reviewNote; }
    public void setReviewNote(String reviewNote) { this.reviewNote = reviewNote; }
    public String getReviewedByUserId() { return reviewedByUserId; }
    public void setReviewedByUserId(String reviewedByUserId) { this.reviewedByUserId = reviewedByUserId; }
    public List<String> getAttachedDocIds() { return attachedDocIds; }
    public void setAttachedDocIds(List<String> attachedDocIds) { this.attachedDocIds = attachedDocIds; }
    public Timestamp getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(Timestamp submittedAt) { this.submittedAt = submittedAt; }
    public Timestamp getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(Timestamp reviewedAt) { this.reviewedAt = reviewedAt; }
}
