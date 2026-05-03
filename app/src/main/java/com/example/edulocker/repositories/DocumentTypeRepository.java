package com.example.edulocker.repositories;

import com.example.edulocker.models.DocumentType;
import com.example.edulocker.utils.Constants;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class DocumentTypeRepository {
    private final FirebaseFirestore db;

    public interface DocTypeListCallback {
        void onSuccess(List<DocumentType> types);
        void onFailure(String error);
    }

    public interface DocTypeCallback {
        void onSuccess();
        void onFailure(String error);
    }

    // Default canonical document list seeded if Firestore list is empty
    private static final List<DocumentType> DEFAULT_TYPES = Arrays.asList(
            new DocumentType("Aadhaar Card", true),
            new DocumentType("Marksheet Class 10", false),
            new DocumentType("Marksheet Class 12", false),
            new DocumentType("Caste Certificate", false),
            new DocumentType("Income Certificate", false),
            new DocumentType("Birth Certificate", false),
            new DocumentType("Transfer Certificate", false),
            new DocumentType("Passport Photo", false),
            new DocumentType("Domicile Certificate", false),
            new DocumentType("Bank Passbook", false)
    );

    public DocumentTypeRepository() {
        db = Constants.db();
    }

    public void getDocumentTypes(DocTypeListCallback callback) {
        db.collection(Constants.COL_DOC_TYPES)
                .orderBy("name")
                .get()
                .addOnSuccessListener(query -> {
                    List<DocumentType> list = new ArrayList<>();
                    for (var doc : query.getDocuments()) {
                        DocumentType dt = doc.toObject(DocumentType.class);
                        if (dt != null) list.add(dt);
                    }
                    if (list.isEmpty()) {
                        seedDefaultTypes(callback);
                    } else {
                        callback.onSuccess(list);
                    }
                })
                .addOnFailureListener(e -> {
                    // Return defaults so upload always works even on network failure
                    callback.onSuccess(DEFAULT_TYPES);
                });
    }

    private void seedDefaultTypes(DocTypeListCallback callback) {
        List<DocumentType> seeded = new ArrayList<>();
        int[] pending = {DEFAULT_TYPES.size()};

        for (DocumentType dt : DEFAULT_TYPES) {
            String id = UUID.randomUUID().toString();
            dt.setDocTypeId(id);
            dt.setCreatedAt(Timestamp.now());
            db.collection(Constants.COL_DOC_TYPES).document(id).set(dt)
                    .addOnSuccessListener(v -> {
                        seeded.add(dt);
                        pending[0]--;
                        if (pending[0] == 0) callback.onSuccess(DEFAULT_TYPES);
                    })
                    .addOnFailureListener(e -> {
                        pending[0]--;
                        if (pending[0] == 0) callback.onSuccess(DEFAULT_TYPES);
                    });
        }
    }

    public void addDocumentType(String name, boolean mandatory, DocTypeCallback callback) {
        String id = UUID.randomUUID().toString();
        DocumentType dt = new DocumentType(name, mandatory);
        dt.setDocTypeId(id);
        dt.setCreatedAt(Timestamp.now());
        db.collection(Constants.COL_DOC_TYPES).document(id).set(dt)
                .addOnSuccessListener(v -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void deleteDocumentType(String docTypeId, DocTypeCallback callback) {
        db.collection(Constants.COL_DOC_TYPES).document(docTypeId)
                .delete()
                .addOnSuccessListener(v -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void updateDocumentType(DocumentType dt, DocTypeCallback callback) {
        db.collection(Constants.COL_DOC_TYPES).document(dt.getDocTypeId())
                .update("name", dt.getName(), "mandatory", dt.isMandatory())
                .addOnSuccessListener(v -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }
}
