package com.example.edulocker.utils;

public class Constants {

    // Named Firestore database — created in Firebase Console as "edulocker" (not the default)
    public static final String FIRESTORE_DB = "edulocker";

    public static com.google.firebase.firestore.FirebaseFirestore db() {
        return com.google.firebase.firestore.FirebaseFirestore
                .getInstance(com.google.firebase.FirebaseApp.getInstance(), FIRESTORE_DB);
    }

    // Firestore collections
    public static final String COL_USERS = "users";
    public static final String COL_SCHOOLS = "schools";
    public static final String COL_STUDENTS = "students";
    public static final String COL_DOCUMENTS = "documents";
    public static final String COL_TEACHERS = "teachers";
    public static final String COL_NOTICES = "notices";
    public static final String COL_SCHOLARSHIPS = "scholarships";
    public static final String COL_APPLICATIONS = "applications";
    public static final String COL_NEWS = "news";
    public static final String COL_DOC_TYPES = "documentTypes";
    public static final String COL_VACANCIES = "vacancies"; // subcollection under schools

    // Document visibility values
    public static final String VISIBILITY_PRIVATE = "private"; // parent + student only
    public static final String VISIBILITY_SHARED  = "shared";  // all authenticated users

    // Notice audience values
    public static final String AUDIENCE_ALL      = "all";
    public static final String AUDIENCE_STUDENTS = "students";
    public static final String AUDIENCE_FACULTY  = "faculty";

    // Intent extras (new)
    public static final String EXTRA_APPLICATION_ID  = "extra_application_id";
    public static final String EXTRA_SCHOLARSHIP_TITLE = "extra_scholarship_title";

    // SharedPreferences
    public static final String PREF_NAME = "EduLockerPrefs";
    public static final String PREF_USER_ROLE = "user_role";
    public static final String PREF_USER_UID = "user_uid";
    public static final String PREF_USER_NAME = "user_name";
    public static final String PREF_SCHOOL_ID = "school_id";
    public static final String PREF_PASSPORT_ID = "passport_id";
    public static final String PREF_STUDENT_SCHOOL_ID = "student_school_id";

    // Intent extras
    public static final String EXTRA_SCHOOL_ID = "extra_school_id";
    public static final String EXTRA_PASSPORT_ID = "extra_passport_id";
    public static final String EXTRA_SCHOLARSHIP_ID = "extra_scholarship_id";
    public static final String EXTRA_TEACHER_ID = "extra_teacher_id";
    public static final String EXTRA_SCHOOL = "extra_school";
    public static final String EXTRA_STUDENT = "extra_student";
    public static final String EXTRA_SCHOLARSHIP = "extra_scholarship";

    // All Indian States and Union Territories (ISO 3166-2:IN codes)
    public static final String[] STATE_NAMES = {
            "Andhra Pradesh", "Arunachal Pradesh", "Assam", "Bihar",
            "Chhattisgarh", "Goa", "Gujarat", "Haryana",
            "Himachal Pradesh", "Jharkhand", "Karnataka", "Kerala",
            "Madhya Pradesh", "Maharashtra", "Manipur", "Meghalaya",
            "Mizoram", "Nagaland", "Odisha", "Punjab",
            "Rajasthan", "Sikkim", "Tamil Nadu", "Telangana",
            "Tripura", "Uttar Pradesh", "Uttarakhand", "West Bengal",
            // Union Territories
            "Andaman and Nicobar Islands", "Chandigarh",
            "Dadra and Nagar Haveli and Daman and Diu", "Delhi",
            "Jammu and Kashmir", "Ladakh", "Lakshadweep", "Puducherry"
    };

    public static final String[] STATE_CODES = {
            "AP", "AR", "AS", "BR",
            "CG", "GA", "GJ", "HR",
            "HP", "JH", "KA", "KL",
            "MP", "MH", "MN", "ML",
            "MZ", "NL", "OD", "PB",
            "RJ", "SK", "TN", "TS",
            "TR", "UP", "UK", "WB",
            // Union Territories
            "AN", "CH",
            "DN", "DL",
            "JK", "LA", "LD", "PY"
    };

    public static String getStateCode(String stateName) {
        for (int i = 0; i < STATE_NAMES.length; i++) {
            if (STATE_NAMES[i].equals(stateName)) return STATE_CODES[i];
        }
        return "IN";
    }
}
