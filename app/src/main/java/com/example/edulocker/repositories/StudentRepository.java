package com.example.edulocker.repositories;

import com.example.edulocker.models.Student;
import com.example.edulocker.utils.Constants;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class StudentRepository {
    private final FirebaseFirestore db;

    public interface StudentCallback {
        void onSuccess(Student student);
        void onFailure(String error);
    }

    public interface StudentListCallback {
        void onSuccess(List<Student> students);
        void onFailure(String error);
    }

    public StudentRepository() {
        db = Constants.db();
    }

    public void saveStudent(Student student, StudentCallback callback) {
        db.collection(Constants.COL_STUDENTS)
                .document(student.getPassportId())
                .set(student)
                .addOnSuccessListener(v -> callback.onSuccess(student))
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void getStudent(String passportId, StudentCallback callback) {
        db.collection(Constants.COL_STUDENTS).document(passportId).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        Student s = doc.toObject(Student.class);
                        callback.onSuccess(s);
                    } else {
                        callback.onFailure("Student not found");
                    }
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void getStudentByUserId(String userId, StudentCallback callback) {
        db.collection(Constants.COL_STUDENTS)
                .whereEqualTo("userId", userId)
                .limit(1).get()
                .addOnSuccessListener(query -> {
                    if (!query.isEmpty()) {
                        Student s = query.getDocuments().get(0).toObject(Student.class);
                        callback.onSuccess(s);
                    } else {
                        callback.onFailure("No student record linked to this account");
                    }
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void getStudentByParentUserId(String parentUserId, StudentCallback callback) {
        db.collection(Constants.COL_STUDENTS)
                .whereEqualTo("parentUserId", parentUserId)
                .limit(1).get()
                .addOnSuccessListener(query -> {
                    if (!query.isEmpty()) {
                        Student s = query.getDocuments().get(0).toObject(Student.class);
                        callback.onSuccess(s);
                    } else {
                        callback.onFailure("No student linked to this parent");
                    }
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void getStudentsBySchool(String schoolId, StudentListCallback callback) {
        db.collection(Constants.COL_STUDENTS)
                .whereEqualTo("schoolId", schoolId)
                .get()
                .addOnSuccessListener(query -> {
                    List<Student> list = new ArrayList<>();
                    for (var doc : query.getDocuments()) {
                        Student s = doc.toObject(Student.class);
                        if (s != null) list.add(s);
                    }
                    callback.onSuccess(list);
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    // Returns students matching the teacher's assigned class (and division if set).
    // Filtering is done client-side after a schoolId query — no composite index required.
    public void getStudentsByClassAndSection(String schoolId, String assignedClass,
                                             String division, StudentListCallback callback) {
        db.collection(Constants.COL_STUDENTS)
                .whereEqualTo("schoolId", schoolId)
                .get()
                .addOnSuccessListener(query -> {
                    List<Student> list = new ArrayList<>();
                    boolean hasDivision = division != null && !division.trim().isEmpty();
                    for (var doc : query.getDocuments()) {
                        Student s = doc.toObject(Student.class);
                        if (s == null) continue;
                        if (!assignedClass.equals(s.getStudentClass())) continue;
                        if (hasDivision && !division.trim().equalsIgnoreCase(
                                s.getSection() != null ? s.getSection().trim() : "")) continue;
                        list.add(s);
                    }
                    callback.onSuccess(list);
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void updateStudentFields(String passportId, Map<String, Object> fields,
                                    StudentCallback callback) {
        db.collection(Constants.COL_STUDENTS).document(passportId)
                .update(fields)
                .addOnSuccessListener(v -> getStudent(passportId, callback))
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void updateStudentUserId(String passportId, String userId, StudentCallback callback) {
        db.collection(Constants.COL_STUDENTS).document(passportId)
                .update("userId", userId)
                .addOnSuccessListener(v -> getStudent(passportId, callback))
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public interface SimpleCallback {
        void onSuccess();
        void onFailure(String error);
    }

    public void updateKycStatus(String passportId, String status, SimpleCallback callback) {
        db.collection(Constants.COL_STUDENTS).document(passportId)
                .update("kycStatus", status)
                .addOnSuccessListener(v -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void saveAadhaarMasked(String passportId, String aadhaarMasked, SimpleCallback callback) {
        db.collection(Constants.COL_STUDENTS).document(passportId)
                .update("aadhaarMasked", aadhaarMasked)
                .addOnSuccessListener(v -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }
}
