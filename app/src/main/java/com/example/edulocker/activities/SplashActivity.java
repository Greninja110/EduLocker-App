package com.example.edulocker.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.example.edulocker.R;
import com.example.edulocker.activities.govt.GovtDashboardActivity;
import com.example.edulocker.activities.parent.ParentDashboardActivity;
import com.example.edulocker.activities.school.SchoolDashboardActivity;
import com.example.edulocker.activities.teacher.TeacherDashboardActivity;
import com.example.edulocker.models.User;
import com.example.edulocker.utils.SessionManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser == null) {
                startActivity(new Intent(this, LoginActivity.class));
                finish();
            } else {
                routeByRole(); // navigateTo() inside always calls finish()
            }
        }, 3000);
    }

    private void routeByRole() {
        SessionManager session = new SessionManager(this);
        String role = session.getUserRole();
        if (role == null) {
            // Fetch role from Firestore if not cached
            new com.example.edulocker.repositories.AuthRepository().getUserRole(
                    FirebaseAuth.getInstance().getCurrentUser().getUid(),
                    fetchedRole -> runOnUiThread(() -> navigateTo(fetchedRole))
            );
        } else {
            navigateTo(role);
        }
    }

    private void navigateTo(String role) {
        Intent intent;
        if (role == null) {
            intent = new Intent(this, LoginActivity.class);
        } else switch (role) {
            case User.ROLE_GOVERNMENT:
                intent = new Intent(this, GovtDashboardActivity.class);
                break;
            case User.ROLE_SCHOOL:
                intent = new Intent(this, SchoolDashboardActivity.class);
                break;
            case User.ROLE_TEACHER:
                intent = new Intent(this, TeacherDashboardActivity.class);
                break;
            case User.ROLE_PARENT:
                intent = new Intent(this, ParentDashboardActivity.class);
                break;
            case User.ROLE_STUDENT:
                intent = new Intent(this, MainActivity.class);
                break;
            default:
                intent = new Intent(this, LoginActivity.class);
        }
        startActivity(intent);
        finish();
    }
}
