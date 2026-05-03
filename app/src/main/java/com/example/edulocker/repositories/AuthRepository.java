package com.example.edulocker.repositories;

import com.example.edulocker.models.User;
import com.example.edulocker.utils.Constants;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class AuthRepository {
    private final FirebaseAuth auth;
    private final FirebaseFirestore db;

    public interface AuthCallback {
        void onSuccess(FirebaseUser user);
        void onFailure(String error);
    }

    public interface RoleCallback {
        void onResult(String role);
    }

    public AuthRepository() {
        auth = FirebaseAuth.getInstance();
        db = Constants.db();
    }

    // Use a secondary FirebaseApp so that creating sub-accounts does NOT sign out
    // the currently logged-in government/school admin user.
    private static synchronized FirebaseApp getOrCreateSecondaryApp() {
        try {
            return FirebaseApp.getInstance("secondary");
        } catch (IllegalStateException e) {
            return FirebaseApp.initializeApp(
                    FirebaseApp.getInstance().getApplicationContext(),
                    FirebaseApp.getInstance().getOptions(),
                    "secondary");
        }
    }

    private static FirebaseAuth getSecondaryAuth() {
        return FirebaseAuth.getInstance(getOrCreateSecondaryApp());
    }

    public void login(String email, String password, AuthCallback callback) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> callback.onSuccess(result.getUser()))
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void createAccount(String email, String password, User userModel, AuthCallback callback) {
        getSecondaryAuth().createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> {
                    FirebaseUser firebaseUser = result.getUser();
                    if (firebaseUser == null) {
                        callback.onFailure("Account creation failed");
                        return;
                    }
                    userModel.setUid(firebaseUser.getUid());
                    // Use primary Firestore (govt admin is authenticated) — rules only require
                    // request.auth != null, so no need for secondary auth context here.
                    // Using secondary Firestore caused the auth token propagation to hang.
                    db.collection(Constants.COL_USERS)
                            .document(firebaseUser.getUid())
                            .set(userModel)
                            .addOnSuccessListener(v -> callback.onSuccess(firebaseUser))
                            .addOnFailureListener(e -> {
                                // Delete the orphaned auth user so the email can be retried later
                                firebaseUser.delete();
                                String msg = e.getMessage() != null ? e.getMessage() : "";
                                if (msg.contains("does not exist") || msg.contains("NOT_FOUND")) {
                                    callback.onFailure("Firestore database not set up.\n\nGo to Firebase Console → Firestore Database → Create Database, then try again.");
                                } else {
                                    callback.onFailure("Profile save failed: " + msg);
                                }
                            });
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void getUserRole(String uid, RoleCallback callback) {
        db.collection(Constants.COL_USERS).document(uid).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        callback.onResult(doc.getString("role"));
                    } else {
                        callback.onResult(null);
                    }
                })
                .addOnFailureListener(e -> callback.onResult(null));
    }

    public FirebaseUser getCurrentUser() {
        return auth.getCurrentUser();
    }

    public void logout() {
        auth.signOut();
    }
}
