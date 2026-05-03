package com.example.edulocker.repositories;

import com.example.edulocker.models.School;
import com.example.edulocker.utils.Constants;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class SchoolRepository {
    private final FirebaseFirestore db;

    public interface SchoolCallback {
        void onSuccess(School school);
        void onFailure(String error);
    }

    public interface SchoolListCallback {
        void onSuccess(List<School> schools);
        void onFailure(String error);
    }

    public SchoolRepository() {
        db = Constants.db();
    }

    public void saveSchool(School school, SchoolCallback callback) {
        db.collection(Constants.COL_SCHOOLS)
                .document(school.getSchoolId())
                .set(school)
                .addOnSuccessListener(v -> callback.onSuccess(school))
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void getSchool(String schoolId, SchoolCallback callback) {
        db.collection(Constants.COL_SCHOOLS).document(schoolId).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        School s = doc.toObject(School.class);
                        callback.onSuccess(s);
                    } else {
                        callback.onFailure("School not found");
                    }
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void getSchoolByEmail(String email, SchoolCallback callback) {
        db.collection(Constants.COL_SCHOOLS)
                .whereEqualTo("loginEmail", email)
                .limit(1).get()
                .addOnSuccessListener(query -> {
                    if (!query.isEmpty()) {
                        School s = query.getDocuments().get(0).toObject(School.class);
                        callback.onSuccess(s);
                    } else {
                        callback.onFailure("School not found");
                    }
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void getAllSchools(SchoolListCallback callback) {
        db.collection(Constants.COL_SCHOOLS)
                .get()
                .addOnSuccessListener(query -> {
                    List<School> list = new ArrayList<>();
                    for (var doc : query.getDocuments()) {
                        School s = doc.toObject(School.class);
                        if (s != null) list.add(s);
                    }
                    // Sort newest-first client-side — no Firestore composite index required
                    list.sort((a, b) -> {
                        if (a.getCreatedAt() == null) return 1;
                        if (b.getCreatedAt() == null) return -1;
                        return b.getCreatedAt().compareTo(a.getCreatedAt());
                    });
                    callback.onSuccess(list);
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void incrementStudentCount(String schoolId) {
        db.collection(Constants.COL_SCHOOLS).document(schoolId)
                .update("studentCount", com.google.firebase.firestore.FieldValue.increment(1));
    }

    public void incrementTeacherCount(String schoolId) {
        db.collection(Constants.COL_SCHOOLS).document(schoolId)
                .update("teacherCount", com.google.firebase.firestore.FieldValue.increment(1));
    }
}
