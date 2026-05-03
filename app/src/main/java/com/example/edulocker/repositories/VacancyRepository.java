package com.example.edulocker.repositories;

import com.example.edulocker.models.Vacancy;
import com.example.edulocker.utils.Constants;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class VacancyRepository {
    private final FirebaseFirestore db;

    public interface VacancyListCallback {
        void onSuccess(List<Vacancy> vacancies);
        void onFailure(String error);
    }

    public interface VacancyCallback {
        void onSuccess();
        void onFailure(String error);
    }

    public VacancyRepository() {
        db = Constants.db();
    }

    public void getVacancies(String schoolId, VacancyListCallback callback) {
        db.collection(Constants.COL_SCHOOLS).document(schoolId)
                .collection(Constants.COL_VACANCIES)
                .get()
                .addOnSuccessListener(query -> {
                    List<Vacancy> list = new ArrayList<>();
                    for (var doc : query.getDocuments()) {
                        Vacancy v = doc.toObject(Vacancy.class);
                        if (v != null) list.add(v);
                    }
                    list.sort((a, b) -> {
                        if (a.getCreatedAt() == null) return 1;
                        if (b.getCreatedAt() == null) return -1;
                        return a.getCreatedAt().compareTo(b.getCreatedAt());
                    });
                    callback.onSuccess(list);
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void addVacancy(String schoolId, Vacancy vacancy, VacancyCallback callback) {
        String id = UUID.randomUUID().toString();
        vacancy.setVacancyId(id);
        vacancy.setSchoolId(schoolId);
        vacancy.setCreatedAt(Timestamp.now());
        db.collection(Constants.COL_SCHOOLS).document(schoolId)
                .collection(Constants.COL_VACANCIES).document(id).set(vacancy)
                .addOnSuccessListener(v -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void updateVacancy(String schoolId, Vacancy vacancy, VacancyCallback callback) {
        db.collection(Constants.COL_SCHOOLS).document(schoolId)
                .collection(Constants.COL_VACANCIES).document(vacancy.getVacancyId())
                .set(vacancy)
                .addOnSuccessListener(v -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void deleteVacancy(String schoolId, String vacancyId, VacancyCallback callback) {
        db.collection(Constants.COL_SCHOOLS).document(schoolId)
                .collection(Constants.COL_VACANCIES).document(vacancyId)
                .delete()
                .addOnSuccessListener(v -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }
}
