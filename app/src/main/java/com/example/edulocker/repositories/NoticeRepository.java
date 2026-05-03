package com.example.edulocker.repositories;

import com.example.edulocker.models.Notice;
import com.example.edulocker.utils.Constants;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class NoticeRepository {
    private final FirebaseFirestore db;

    public interface NoticeListCallback {
        void onSuccess(List<Notice> notices);
        void onFailure(String error);
    }

    public interface NoticeCallback {
        void onSuccess();
        void onFailure(String error);
    }

    public NoticeRepository() {
        db = Constants.db();
    }

    public void postNotice(Notice notice, NoticeCallback callback) {
        String id = UUID.randomUUID().toString();
        notice.setNoticeId(id);
        notice.setTimestamp(Timestamp.now());
        db.collection(Constants.COL_NOTICES).document(id).set(notice)
                .addOnSuccessListener(v -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void getAllNotices(NoticeListCallback callback) {
        db.collection(Constants.COL_NOTICES)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(50)
                .get()
                .addOnSuccessListener(query -> {
                    List<Notice> list = new ArrayList<>();
                    for (var doc : query.getDocuments()) {
                        Notice n = doc.toObject(Notice.class);
                        if (n != null) list.add(n);
                    }
                    callback.onSuccess(list);
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    // Fetches notices for a student: org notices (audience=all or students) +
    // state-level notices. Parents see the same notices as students.
    public void getStudentNotices(String schoolId, String studentState, NoticeListCallback callback) {
        List<Notice> merged = new ArrayList<>();
        int[] pending = {2};

        // Query 1 — org notices for this school; filter audience client-side
        db.collection(Constants.COL_NOTICES)
                .whereEqualTo("schoolId", schoolId)
                .get()
                .addOnSuccessListener(q -> {
                    for (var doc : q.getDocuments()) {
                        Notice n = doc.toObject(Notice.class);
                        if (n != null && isVisibleToStudents(n)) merged.add(n);
                    }
                    pending[0]--;
                    if (pending[0] == 0) deliverMerged(merged, callback);
                })
                .addOnFailureListener(e -> {
                    pending[0]--;
                    if (pending[0] == 0) deliverMerged(merged, callback);
                });

        // Query 2 — state-level notices; filter by state client-side
        db.collection(Constants.COL_NOTICES)
                .whereEqualTo("noticeType", Notice.TYPE_STATE)
                .get()
                .addOnSuccessListener(q -> {
                    for (var doc : q.getDocuments()) {
                        Notice n = doc.toObject(Notice.class);
                        if (n != null) {
                            boolean matchesState = "All".equals(n.getState())
                                    || (studentState != null && studentState.equals(n.getState()));
                            if (matchesState) merged.add(n);
                        }
                    }
                    pending[0]--;
                    if (pending[0] == 0) deliverMerged(merged, callback);
                })
                .addOnFailureListener(e -> {
                    pending[0]--;
                    if (pending[0] == 0) deliverMerged(merged, callback);
                });
    }

    // Faculty (teachers + school admin) see notices with audience=all or faculty
    public void getFacultyNotices(String schoolId, NoticeListCallback callback) {
        db.collection(Constants.COL_NOTICES)
                .whereEqualTo("schoolId", schoolId)
                .get()
                .addOnSuccessListener(q -> {
                    List<Notice> list = new ArrayList<>();
                    for (var doc : q.getDocuments()) {
                        Notice n = doc.toObject(Notice.class);
                        if (n != null && isVisibleToFaculty(n)) list.add(n);
                    }
                    sortNewestFirst(list);
                    callback.onSuccess(list);
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    private boolean isVisibleToStudents(Notice n) {
        String aud = n.getAudience();
        return aud == null || aud.isEmpty()
                || Constants.AUDIENCE_ALL.equals(aud)
                || Constants.AUDIENCE_STUDENTS.equals(aud);
    }

    private boolean isVisibleToFaculty(Notice n) {
        String aud = n.getAudience();
        return aud == null || aud.isEmpty()
                || Constants.AUDIENCE_ALL.equals(aud)
                || Constants.AUDIENCE_FACULTY.equals(aud);
    }

    private void sortNewestFirst(List<Notice> list) {
        list.sort((a, b) -> {
            if (a.getTimestamp() == null) return 1;
            if (b.getTimestamp() == null) return -1;
            return b.getTimestamp().compareTo(a.getTimestamp());
        });
    }

    private void deliverMerged(List<Notice> raw, NoticeListCallback callback) {
        // Deduplicate by noticeId (a notice can't appear in both queries normally,
        // but guard against it) then sort newest-first
        Map<String, Notice> seen = new LinkedHashMap<>();
        for (Notice n : raw) {
            if (n.getNoticeId() != null) seen.put(n.getNoticeId(), n);
        }
        List<Notice> result = new ArrayList<>(seen.values());
        result.sort((a, b) -> {
            if (a.getTimestamp() == null) return 1;
            if (b.getTimestamp() == null) return -1;
            return b.getTimestamp().compareTo(a.getTimestamp());
        });
        callback.onSuccess(result);
    }

    public void deleteNotice(String noticeId, NoticeCallback callback) {
        db.collection(Constants.COL_NOTICES).document(noticeId)
                .delete()
                .addOnSuccessListener(v -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void updateNotice(Notice notice, NoticeCallback callback) {
        db.collection(Constants.COL_NOTICES).document(notice.getNoticeId())
                .update("title", notice.getTitle(),
                        "content", notice.getContent(),
                        "state", notice.getState())
                .addOnSuccessListener(v -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void getNoticesForSchool(String schoolId, NoticeListCallback callback) {
        db.collection(Constants.COL_NOTICES)
                .whereEqualTo("schoolId", schoolId)
                .get()
                .addOnSuccessListener(query -> {
                    List<Notice> list = new ArrayList<>();
                    for (var doc : query.getDocuments()) {
                        Notice n = doc.toObject(Notice.class);
                        if (n != null) list.add(n);
                    }
                    // Sort newest-first client-side — no composite index required
                    list.sort((a, b) -> {
                        if (a.getTimestamp() == null) return 1;
                        if (b.getTimestamp() == null) return -1;
                        return b.getTimestamp().compareTo(a.getTimestamp());
                    });
                    callback.onSuccess(list);
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }
}
