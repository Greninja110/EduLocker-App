package com.example.edulocker.models;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.Exclude;

public class Student {
    private String passportId;
    private String userId;
    private String schoolId;
    private String schoolName;
    private String state;     // e.g. "Maharashtra"
    private String stateCode; // e.g. "MH"
    private String name;
    private String dob;        // DD/MM/YYYY
    private String bloodGroup;
    private String fatherName;
    private String motherName;
    private String address;
    private String pinCode;
    private String studentClass; // "Class 12"
    private String section;
    private String contactNumber;
    private String aadhaarMasked; // e.g. "3431 56** ****"
    private String kycStatus;   // "Verified" | "Pending"
    private String parentUserId;
    private String parentPhone;
    private String parentLoginEmail;
    private String parentLoginPassword; // stored so org admin can look it up
    private String category;    // "General" | "OBC" | "SC" | "ST"
    private String loginEmail;
    private String loginPassword; // shown once after creation
    private int attendancePercentage;
    private Timestamp createdAt;

    public Student() {}

    public String getPassportId() { return passportId; }
    public void setPassportId(String passportId) { this.passportId = passportId; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getSchoolId() { return schoolId; }
    public void setSchoolId(String schoolId) { this.schoolId = schoolId; }
    public String getSchoolName() { return schoolName; }
    public void setSchoolName(String schoolName) { this.schoolName = schoolName; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDob() { return dob; }
    public void setDob(String dob) { this.dob = dob; }
    public String getBloodGroup() { return bloodGroup; }
    public void setBloodGroup(String bloodGroup) { this.bloodGroup = bloodGroup; }
    public String getFatherName() { return fatherName; }
    public void setFatherName(String fatherName) { this.fatherName = fatherName; }
    public String getMotherName() { return motherName; }
    public void setMotherName(String motherName) { this.motherName = motherName; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getPinCode() { return pinCode; }
    public void setPinCode(String pinCode) { this.pinCode = pinCode; }
    public String getStudentClass() { return studentClass; }
    public void setStudentClass(String studentClass) { this.studentClass = studentClass; }
    public String getSection() { return section; }
    public void setSection(String section) { this.section = section; }
    public String getContactNumber() { return contactNumber; }
    public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }
    public String getAadhaarMasked() { return aadhaarMasked; }
    public void setAadhaarMasked(String aadhaarMasked) { this.aadhaarMasked = aadhaarMasked; }
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    public String getStateCode() { return stateCode; }
    public void setStateCode(String stateCode) { this.stateCode = stateCode; }
    public String getKycStatus() { return kycStatus; }
    public void setKycStatus(String kycStatus) { this.kycStatus = kycStatus; }
    public String getParentUserId() { return parentUserId; }
    public void setParentUserId(String parentUserId) { this.parentUserId = parentUserId; }
    public String getParentPhone() { return parentPhone; }
    public void setParentPhone(String parentPhone) { this.parentPhone = parentPhone; }
    public String getParentLoginEmail() { return parentLoginEmail; }
    public void setParentLoginEmail(String parentLoginEmail) { this.parentLoginEmail = parentLoginEmail; }
    public String getParentLoginPassword() { return parentLoginPassword; }
    public void setParentLoginPassword(String parentLoginPassword) { this.parentLoginPassword = parentLoginPassword; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getLoginEmail() { return loginEmail; }
    public void setLoginEmail(String loginEmail) { this.loginEmail = loginEmail; }
    @Exclude
    public String getLoginPassword() { return loginPassword; }
    public void setLoginPassword(String loginPassword) { this.loginPassword = loginPassword; }
    public int getAttendancePercentage() { return attendancePercentage; }
    public void setAttendancePercentage(int attendancePercentage) { this.attendancePercentage = attendancePercentage; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
