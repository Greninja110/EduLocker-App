package com.example.edulocker.models;

import com.google.firebase.Timestamp;

public class EduDocument {
    public static final String TYPE_MARKSHEET = "Marksheet";
    public static final String TYPE_CASTE_CERT = "Caste Certificate";
    public static final String TYPE_INCOME_CERT = "Income Certificate";
    public static final String TYPE_ACHIEVEMENT = "Achievement Certificate";
    public static final String TYPE_OTHER = "Other";

    private String docId;
    private String passportId;
    private String type;          // canonical name from gov doc list, or "Other"
    private String otherTitle;    // only used when type = "Other"
    private String title;
    private String fileUrl;
    private String uploadedByUserId;
    private String uploadedByRole;
    private boolean verified;
    private Timestamp timestamp;
    private String academicYear;  // e.g. "2024-25"
    private String visibility;    // "private" (parent+student) | "shared" (all users)

    public EduDocument() {}

    public String getDocId() { return docId; }
    public void setDocId(String docId) { this.docId = docId; }
    public String getPassportId() { return passportId; }
    public void setPassportId(String passportId) { this.passportId = passportId; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }
    public String getUploadedByUserId() { return uploadedByUserId; }
    public void setUploadedByUserId(String uploadedByUserId) { this.uploadedByUserId = uploadedByUserId; }
    public String getUploadedByRole() { return uploadedByRole; }
    public void setUploadedByRole(String uploadedByRole) { this.uploadedByRole = uploadedByRole; }
    public boolean isVerified() { return verified; }
    public void setVerified(boolean verified) { this.verified = verified; }
    public Timestamp getTimestamp() { return timestamp; }
    public void setTimestamp(Timestamp timestamp) { this.timestamp = timestamp; }
    public String getAcademicYear() { return academicYear; }
    public void setAcademicYear(String academicYear) { this.academicYear = academicYear; }
    public String getOtherTitle() { return otherTitle; }
    public void setOtherTitle(String otherTitle) { this.otherTitle = otherTitle; }
    public String getVisibility() { return visibility; }
    public void setVisibility(String visibility) { this.visibility = visibility; }

    public String getDisplayTitle() {
        if ("Other".equals(type) && otherTitle != null && !otherTitle.isEmpty()) return otherTitle;
        return title != null ? title : type;
    }
}
