package com.example.edulocker.repositories;

import android.content.Context;
import android.net.Uri;

import com.example.edulocker.BuildConfig;
import com.example.edulocker.models.EduDocument;
import com.example.edulocker.models.User;
import com.example.edulocker.utils.Constants;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class DocumentRepository {
    private final FirebaseFirestore db;
    private static final OkHttpClient client = new OkHttpClient();

    public interface DocumentCallback {
        void onSuccess(EduDocument document);
        void onFailure(String error);
    }

    public interface DocumentListCallback {
        void onSuccess(List<EduDocument> documents);
        void onFailure(String error);
    }

    public interface UploadProgressCallback {
        void onProgress(int percent);
        void onSuccess(String downloadUrl);
        void onFailure(String error);
    }

    public interface DocumentDeleteCallback {
        void onSuccess();
        void onFailure(String error);
    }

    public DocumentRepository() {
        db = Constants.db();
    }

    public void uploadDocument(Context context, Uri fileUri, EduDocument document, UploadProgressCallback callback) {
        String cloudName = BuildConfig.CLOUDINARY_CLOUD_NAME;
        String uploadPreset = BuildConfig.CLOUDINARY_UPLOAD_PRESET;

        if (cloudName.isEmpty() || uploadPreset.isEmpty()) {
            callback.onFailure("Cloudinary not configured. Add credentials to secrets.properties.");
            return;
        }

        String folder = "documents/" + document.getPassportId() + "/" + document.getType();
        String uploadUrl = "https://api.cloudinary.com/v1_1/" + cloudName + "/auto/upload";

        try {
            InputStream inputStream = context.getContentResolver().openInputStream(fileUri);
            if (inputStream == null) {
                callback.onFailure("Could not read file");
                return;
            }
            byte[] fileBytes = inputStream.readAllBytes();
            inputStream.close();

            String mimeType = context.getContentResolver().getType(fileUri);
            if (mimeType == null) mimeType = "application/octet-stream";

            // Report 10% immediately so user sees progress start
            callback.onProgress(10);

            RequestBody fileBody = RequestBody.create(fileBytes, MediaType.parse(mimeType));
            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("file", UUID.randomUUID().toString(), fileBody)
                    .addFormDataPart("upload_preset", uploadPreset)
                    .addFormDataPart("folder", folder)
                    .build();

            Request request = new Request.Builder()
                    .url(uploadUrl)
                    .post(requestBody)
                    .build();

            callback.onProgress(30);

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    callback.onFailure("Upload failed: " + e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    callback.onProgress(80);
                    String body = response.body() != null ? response.body().string() : "";
                    if (!response.isSuccessful()) {
                        callback.onFailure("Cloudinary error: " + response.code() + " " + body);
                        return;
                    }
                    try {
                        JSONObject json = new JSONObject(body);
                        String secureUrl = json.getString("secure_url");
                        document.setFileUrl(secureUrl);
                        String docId = UUID.randomUUID().toString();
                        document.setDocId(docId);
                        document.setTimestamp(Timestamp.now());
                        // Set visibility based on uploader role
                        if (document.getVisibility() == null) {
                            String role = document.getUploadedByRole();
                            if (User.ROLE_PARENT.equals(role)) {
                                document.setVisibility(Constants.VISIBILITY_PRIVATE);
                            } else {
                                document.setVisibility(Constants.VISIBILITY_SHARED);
                            }
                        }
                        db.collection(Constants.COL_DOCUMENTS).document(docId).set(document)
                                .addOnSuccessListener(v -> {
                                    callback.onProgress(100);
                                    callback.onSuccess(secureUrl);
                                })
                                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
                    } catch (Exception e) {
                        callback.onFailure("Failed to parse Cloudinary response: " + e.getMessage());
                    }
                }
            });
        } catch (Exception e) {
            callback.onFailure("Error reading file: " + e.getMessage());
        }
    }

    public void deleteDocument(String docId, DocumentDeleteCallback callback) {
        db.collection(Constants.COL_DOCUMENTS).document(docId)
                .delete()
                .addOnSuccessListener(v -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void getDocumentsForStudent(String passportId, DocumentListCallback callback) {
        db.collection(Constants.COL_DOCUMENTS)
                .whereEqualTo("passportId", passportId)
                .get()
                .addOnSuccessListener(query -> {
                    List<EduDocument> list = new ArrayList<>();
                    for (var doc : query.getDocuments()) {
                        EduDocument d = doc.toObject(EduDocument.class);
                        if (d != null) list.add(d);
                    }
                    sortNewestFirst(list);
                    callback.onSuccess(list);
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    // Student/parent view: all docs for the student, but private docs filtered
    // to only those uploaded by the parent or the student themselves.
    public void getDocumentsForStudentWithPermission(String passportId, String viewerUserId,
                                                     String viewerRole, DocumentListCallback callback) {
        db.collection(Constants.COL_DOCUMENTS)
                .whereEqualTo("passportId", passportId)
                .get()
                .addOnSuccessListener(query -> {
                    List<EduDocument> list = new ArrayList<>();
                    for (var doc : query.getDocuments()) {
                        EduDocument d = doc.toObject(EduDocument.class);
                        if (d == null) continue;
                        if (canView(d, viewerUserId, viewerRole)) list.add(d);
                    }
                    sortNewestFirst(list);
                    callback.onSuccess(list);
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    // UI-layer enforcement: returns true if viewer can see this document
    public static boolean canView(EduDocument doc, String viewerUserId, String viewerRole) {
        if (Constants.VISIBILITY_SHARED.equals(doc.getVisibility())) return true;
        // private — only the uploader (parent) and the student can see
        if (viewerUserId != null && viewerUserId.equals(doc.getUploadedByUserId())) return true;
        if (User.ROLE_STUDENT.equals(viewerRole)) return true;
        if (User.ROLE_PARENT.equals(viewerRole)) return true;
        return false;
    }

    private void sortNewestFirst(List<EduDocument> list) {
        list.sort((a, b) -> {
            if (a.getTimestamp() == null) return 1;
            if (b.getTimestamp() == null) return -1;
            return b.getTimestamp().compareTo(a.getTimestamp());
        });
    }
}
