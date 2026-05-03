package com.example.edulocker.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private final SharedPreferences prefs;
    private final SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    public void saveSession(String uid, String name, String role) {
        editor.putString(Constants.PREF_USER_UID, uid);
        editor.putString(Constants.PREF_USER_NAME, name);
        editor.putString(Constants.PREF_USER_ROLE, role);
        editor.apply();
    }

    public void saveSchoolId(String schoolId) {
        editor.putString(Constants.PREF_SCHOOL_ID, schoolId);
        editor.apply();
    }

    public void savePassportId(String passportId) {
        editor.putString(Constants.PREF_PASSPORT_ID, passportId);
        editor.apply();
    }

    public void saveStudentSchoolId(String schoolId) {
        editor.putString(Constants.PREF_STUDENT_SCHOOL_ID, schoolId);
        editor.apply();
    }

    public String getUserUid() { return prefs.getString(Constants.PREF_USER_UID, null); }
    public String getUserName() { return prefs.getString(Constants.PREF_USER_NAME, null); }
    public String getUserRole() { return prefs.getString(Constants.PREF_USER_ROLE, null); }
    public String getSchoolId() { return prefs.getString(Constants.PREF_SCHOOL_ID, null); }
    public String getPassportId() { return prefs.getString(Constants.PREF_PASSPORT_ID, null); }
    public String getStudentSchoolId() { return prefs.getString(Constants.PREF_STUDENT_SCHOOL_ID, null); }

    public boolean isLoggedIn() { return getUserUid() != null; }

    public void clearRoleSpecificData() {
        editor.remove(Constants.PREF_PASSPORT_ID);
        editor.remove(Constants.PREF_SCHOOL_ID);
        editor.remove(Constants.PREF_STUDENT_SCHOOL_ID);
        editor.apply();
    }

    public void clearSession() {
        editor.clear();
        editor.apply();
    }
}
