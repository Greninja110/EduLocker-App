package com.example.edulocker.models;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.Exclude;

public class School {
    private String schoolId;
    private String name;
    private String type; // "higher_secondary", "college", "primary"
    private String district;
    private String districtCode; // e.g. "KMD"
    private String state;
    private String stateCode; // ISO code e.g. "MH", "DL"
    private String schoolCode; // e.g. "PKS" — 3-letter code
    private String loginEmail;
    private String loginPassword; // stored temporarily for display after creation
    private String registeredByGovtId;
    private int studentCount;
    private int teacherCount;
    private Timestamp createdAt;
    private String principalName;
    private String address;
    private String phone;

    public School() {}

    public String getSchoolId() { return schoolId; }
    public void setSchoolId(String schoolId) { this.schoolId = schoolId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }
    public String getDistrictCode() { return districtCode; }
    public void setDistrictCode(String districtCode) { this.districtCode = districtCode; }
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    public String getStateCode() { return stateCode; }
    public void setStateCode(String stateCode) { this.stateCode = stateCode; }
    public String getSchoolCode() { return schoolCode; }
    public void setSchoolCode(String schoolCode) { this.schoolCode = schoolCode; }
    public String getLoginEmail() { return loginEmail; }
    public void setLoginEmail(String loginEmail) { this.loginEmail = loginEmail; }
    @Exclude
    public String getLoginPassword() { return loginPassword; }
    public void setLoginPassword(String loginPassword) { this.loginPassword = loginPassword; }
    public String getRegisteredByGovtId() { return registeredByGovtId; }
    public void setRegisteredByGovtId(String registeredByGovtId) { this.registeredByGovtId = registeredByGovtId; }
    public int getStudentCount() { return studentCount; }
    public void setStudentCount(int studentCount) { this.studentCount = studentCount; }
    public int getTeacherCount() { return teacherCount; }
    public void setTeacherCount(int teacherCount) { this.teacherCount = teacherCount; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
    public String getPrincipalName() { return principalName; }
    public void setPrincipalName(String principalName) { this.principalName = principalName; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
}
