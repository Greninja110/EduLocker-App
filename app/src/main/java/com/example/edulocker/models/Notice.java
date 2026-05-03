package com.example.edulocker.models;

import com.google.firebase.Timestamp;

public class Notice {
    public static final String TYPE_ORG   = "org";
    public static final String TYPE_STATE = "state";

    private String noticeId;
    private String noticeType; // "org" | "state"
    private String schoolId;   // set for org notices
    private String state;      // set for state notices (state name or "All" for national)
    private String title;
    private String content;
    private String postedByUserId;
    private String postedByName;
    private String postedByRole;
    private String targetClass; // e.g. "10-A" — null/empty means whole school
    private String audience;    // "all" | "students" | "faculty" (org notices only)
    private Timestamp timestamp;

    public Notice() {}

    public String getNoticeId() { return noticeId; }
    public void setNoticeId(String noticeId) { this.noticeId = noticeId; }
    public String getNoticeType() { return noticeType; }
    public void setNoticeType(String noticeType) { this.noticeType = noticeType; }
    public String getSchoolId() { return schoolId; }
    public void setSchoolId(String schoolId) { this.schoolId = schoolId; }
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getPostedByUserId() { return postedByUserId; }
    public void setPostedByUserId(String postedByUserId) { this.postedByUserId = postedByUserId; }
    public String getPostedByName() { return postedByName; }
    public void setPostedByName(String postedByName) { this.postedByName = postedByName; }
    public String getPostedByRole() { return postedByRole; }
    public void setPostedByRole(String postedByRole) { this.postedByRole = postedByRole; }
    public String getTargetClass() { return targetClass; }
    public void setTargetClass(String targetClass) { this.targetClass = targetClass; }
    public String getAudience() { return audience; }
    public void setAudience(String audience) { this.audience = audience; }
    public Timestamp getTimestamp() { return timestamp; }
    public void setTimestamp(Timestamp timestamp) { this.timestamp = timestamp; }
}
