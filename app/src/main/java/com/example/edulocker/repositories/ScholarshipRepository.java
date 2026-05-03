package com.example.edulocker.repositories;

import com.example.edulocker.models.Scholarship;
import com.example.edulocker.models.ScholarshipApplication;
import com.example.edulocker.models.Student;
import com.example.edulocker.utils.Constants;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class ScholarshipRepository {
    private final FirebaseFirestore db;

    public interface ScholarshipListCallback {
        void onSuccess(List<Scholarship> scholarships);
        void onFailure(String error);
    }

    public interface ScholarshipCallback {
        void onSuccess(String scholarshipId);
        void onFailure(String error);
    }

    public interface ApplicationCallback {
        void onSuccess(String applicationId);
        void onFailure(String error);
    }

    public interface ApplicationListCallback {
        void onSuccess(List<ScholarshipApplication> applications);
        void onFailure(String error);
    }

    public interface SimpleCallback {
        void onSuccess();
        void onFailure(String error);
    }

    public ScholarshipRepository() {
        db = Constants.db();
    }

    public void postScholarship(Scholarship scholarship, ScholarshipCallback callback) {
        String id = UUID.randomUUID().toString();
        scholarship.setScholarshipId(id);
        scholarship.setCreatedAt(Timestamp.now());
        db.collection(Constants.COL_SCHOLARSHIPS).document(id).set(scholarship)
                .addOnSuccessListener(v -> callback.onSuccess(id))
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void updateScholarship(Scholarship scholarship, SimpleCallback callback) {
        db.collection(Constants.COL_SCHOLARSHIPS).document(scholarship.getScholarshipId())
                .set(scholarship)
                .addOnSuccessListener(v -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void deleteScholarship(String scholarshipId, SimpleCallback callback) {
        db.collection(Constants.COL_SCHOLARSHIPS).document(scholarshipId)
                .delete()
                .addOnSuccessListener(v -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void getAllScholarships(ScholarshipListCallback callback) {
        db.collection(Constants.COL_SCHOLARSHIPS).get()
                .addOnSuccessListener(query -> {
                    List<Scholarship> list = new ArrayList<>();
                    for (var doc : query.getDocuments()) {
                        Scholarship s = doc.toObject(Scholarship.class);
                        if (s != null) list.add(s);
                    }
                    callback.onSuccess(list);
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    // Returns eligible scholarships for a student — both gov (schoolId=null) and
    // school-specific (schoolId matches student's school).
    public void getEligibleScholarships(Student student, ScholarshipListCallback callback) {
        getAllScholarships(new ScholarshipListCallback() {
            @Override
            public void onSuccess(List<Scholarship> all) {
                List<Scholarship> eligible = new ArrayList<>();
                for (Scholarship s : all) {
                    if (!isScholarshipVisible(s, student.getSchoolId())) continue;
                    if (isEligible(student, s)) eligible.add(s);
                }
                callback.onSuccess(eligible);
            }
            @Override
            public void onFailure(String error) { callback.onFailure(error); }
        });
    }

    // Scholarships visible to org admin / school for review
    public void getScholarshipsForSchool(String schoolId, ScholarshipListCallback callback) {
        getAllScholarships(new ScholarshipListCallback() {
            @Override
            public void onSuccess(List<Scholarship> all) {
                List<Scholarship> result = new ArrayList<>();
                for (Scholarship s : all) {
                    if (isScholarshipVisible(s, schoolId)) result.add(s);
                }
                callback.onSuccess(result);
            }
            @Override
            public void onFailure(String error) { callback.onFailure(error); }
        });
    }

    private boolean isScholarshipVisible(Scholarship s, String schoolId) {
        // Gov scholarship has schoolId=null, visible to all
        if (s.getSchoolId() == null || s.getSchoolId().isEmpty()) return true;
        // School scholarship visible only to that school
        return s.getSchoolId().equals(schoolId);
    }

    private boolean isEligible(Student student, Scholarship s) {
        if (!isDeadlineOpen(s.getDeadline())) return false;
        if (!"All".equals(s.getEligibleCategory())
                && !categoryMatches(s.getEligibleCategory(), student.getCategory())) return false;
        if (!"All".equals(s.getEligibleClass())
                && !s.getEligibleClass().equals(student.getStudentClass())) return false;
        if (s.getMinMarksPercent() > 0
                && student.getAttendancePercentage() < s.getMinMarksPercent()) return false;
        return true;
    }

    private boolean categoryMatches(String eligibleCategory, String studentCategory) {
        if (eligibleCategory == null || studentCategory == null) return false;
        if (eligibleCategory.equals(studentCategory)) return true;
        // Handle "SC/ST" combined category
        if ("SC/ST".equals(eligibleCategory)) {
            return "SC".equals(studentCategory) || "ST".equals(studentCategory);
        }
        return false;
    }

    private boolean isDeadlineOpen(String deadline) {
        if (deadline == null || deadline.isEmpty()) return true;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            sdf.setLenient(false);
            Date deadlineDate = sdf.parse(deadline);
            return deadlineDate != null && !deadlineDate.before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    public void submitApplication(ScholarshipApplication application, ApplicationCallback callback) {
        String id = UUID.randomUUID().toString();
        application.setApplicationId(id);
        application.setStatus(ScholarshipApplication.STATUS_SUBMITTED);
        application.setSubmittedAt(Timestamp.now());
        db.collection(Constants.COL_APPLICATIONS).document(id).set(application)
                .addOnSuccessListener(v -> callback.onSuccess(id))
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void updateApplicationStatus(String applicationId, String status, String reviewNote,
                                        String reviewerUid, SimpleCallback callback) {
        db.collection(Constants.COL_APPLICATIONS).document(applicationId)
                .update("status", status,
                        "reviewNote", reviewNote,
                        "reviewedByUserId", reviewerUid,
                        "reviewedAt", Timestamp.now())
                .addOnSuccessListener(v -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void getApplicationsForStudent(String passportId, ApplicationListCallback callback) {
        db.collection(Constants.COL_APPLICATIONS)
                .whereEqualTo("passportId", passportId)
                .get()
                .addOnSuccessListener(query -> {
                    List<ScholarshipApplication> list = new ArrayList<>();
                    for (var doc : query.getDocuments()) {
                        ScholarshipApplication a = doc.toObject(ScholarshipApplication.class);
                        if (a != null) list.add(a);
                    }
                    callback.onSuccess(list);
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void getApplicationsForScholarship(String scholarshipId, ApplicationListCallback callback) {
        db.collection(Constants.COL_APPLICATIONS)
                .whereEqualTo("scholarshipId", scholarshipId)
                .get()
                .addOnSuccessListener(query -> {
                    List<ScholarshipApplication> list = new ArrayList<>();
                    for (var doc : query.getDocuments()) {
                        ScholarshipApplication a = doc.toObject(ScholarshipApplication.class);
                        if (a != null) list.add(a);
                    }
                    callback.onSuccess(list);
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    // School admin: all applications from their school's students for a scholarship
    public void getApplicationsForSchoolAndScholarship(String schoolId, String scholarshipId,
                                                        ApplicationListCallback callback) {
        db.collection(Constants.COL_APPLICATIONS)
                .whereEqualTo("scholarshipId", scholarshipId)
                .whereEqualTo("schoolId", schoolId)
                .get()
                .addOnSuccessListener(query -> {
                    List<ScholarshipApplication> list = new ArrayList<>();
                    for (var doc : query.getDocuments()) {
                        ScholarshipApplication a = doc.toObject(ScholarshipApplication.class);
                        if (a != null) list.add(a);
                    }
                    callback.onSuccess(list);
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }
}
