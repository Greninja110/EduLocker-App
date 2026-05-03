package com.example.edulocker.models;

import com.google.firebase.Timestamp;
import java.util.List;

public class Scholarship {
    private String scholarshipId;
    private String title;
    private String description;
    private String deadline;          // DD/MM/YYYY
    private int minMarksPercent;      // 0 = no requirement
    private long maxFamilyIncome;     // 0 = no requirement
    private String eligibleCategory;  // "All","SC","ST","OBC","EWS","General","SC/ST"
    private String eligibleClass;     // "All","Class 10","Class 12", etc.
    private String amount;            // e.g. "₹10,000"
    private String issuedBy;          // school name or "Govt of Odisha"
    private String schoolId;          // null = gov scholarship (all schools), set = school-specific
    private String postedByUserId;
    private String postedByRole;      // "government" or "school"
    private List<RequiredDoc> requiredDocs; // docs students must have
    private Timestamp createdAt;

    public Scholarship() {}

    public String getScholarshipId() { return scholarshipId; }
    public void setScholarshipId(String scholarshipId) { this.scholarshipId = scholarshipId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getDeadline() { return deadline; }
    public void setDeadline(String deadline) { this.deadline = deadline; }
    public int getMinMarksPercent() { return minMarksPercent; }
    public void setMinMarksPercent(int minMarksPercent) { this.minMarksPercent = minMarksPercent; }
    public long getMaxFamilyIncome() { return maxFamilyIncome; }
    public void setMaxFamilyIncome(long maxFamilyIncome) { this.maxFamilyIncome = maxFamilyIncome; }
    public String getEligibleCategory() { return eligibleCategory; }
    public void setEligibleCategory(String eligibleCategory) { this.eligibleCategory = eligibleCategory; }
    public String getEligibleClass() { return eligibleClass; }
    public void setEligibleClass(String eligibleClass) { this.eligibleClass = eligibleClass; }
    public String getAmount() { return amount; }
    public void setAmount(String amount) { this.amount = amount; }
    public String getIssuedBy() { return issuedBy; }
    public void setIssuedBy(String issuedBy) { this.issuedBy = issuedBy; }
    public String getSchoolId() { return schoolId; }
    public void setSchoolId(String schoolId) { this.schoolId = schoolId; }
    public String getPostedByUserId() { return postedByUserId; }
    public void setPostedByUserId(String postedByUserId) { this.postedByUserId = postedByUserId; }
    public String getPostedByRole() { return postedByRole; }
    public void setPostedByRole(String postedByRole) { this.postedByRole = postedByRole; }
    public List<RequiredDoc> getRequiredDocs() { return requiredDocs; }
    public void setRequiredDocs(List<RequiredDoc> requiredDocs) { this.requiredDocs = requiredDocs; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
