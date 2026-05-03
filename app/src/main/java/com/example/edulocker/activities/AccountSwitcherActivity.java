package com.example.edulocker.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.edulocker.activities.MainActivity;
import com.example.edulocker.R;
import com.example.edulocker.activities.govt.GovtDashboardActivity;
import com.example.edulocker.activities.parent.ParentDashboardActivity;
import com.example.edulocker.activities.school.SchoolDashboardActivity;
import com.example.edulocker.activities.teacher.TeacherDashboardActivity;
import com.example.edulocker.adapters.SavedAccountAdapter;
import com.example.edulocker.models.SavedAccount;
import com.example.edulocker.models.User;
import com.example.edulocker.repositories.StudentRepository;
import com.example.edulocker.utils.AccountManager;
import com.example.edulocker.utils.Constants;
import com.example.edulocker.utils.SessionManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

public class AccountSwitcherActivity extends AppCompatActivity {

    private RecyclerView rvAccounts;
    private ProgressBar progressBar;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_switcher);
        session = new SessionManager(this);

        WindowCompat.getInsetsController(getWindow(), findViewById(android.R.id.content))
                .setAppearanceLightStatusBars(true);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            v.setPadding(0, insets.getInsets(WindowInsetsCompat.Type.statusBars()).top, 0, 0);
            return insets;
        });

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        rvAccounts  = findViewById(R.id.rv_accounts);
        progressBar = findViewById(R.id.progress_bar);
        rvAccounts.setLayoutManager(new LinearLayoutManager(this));

        findViewById(R.id.btn_add_account).setOnClickListener(v -> showAddAccountDialog());

        refreshList();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshList();
    }

    // ── List ──────────────────────────────────────────────────────────────────

    private void refreshList() {
        FirebaseUser current = FirebaseAuth.getInstance().getCurrentUser();
        String activeUid = current != null ? current.getUid() : "";
        List<SavedAccount> accounts = AccountManager.getAccounts(this);

        rvAccounts.setAdapter(new SavedAccountAdapter(accounts, activeUid,
                new SavedAccountAdapter.Listener() {
                    @Override public void onSwitch(SavedAccount account) { confirmSwitch(account); }
                    @Override public void onEditAlias(SavedAccount account) { showAliasDialog(account); }
                    @Override public void onDelete(SavedAccount account) { confirmDelete(account); }
                }));
    }

    // ── Add Account ───────────────────────────────────────────────────────────

    private void showAddAccountDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_account, null, false);
        EditText etEmail    = view.findViewById(R.id.et_email);
        EditText etPassword = view.findViewById(R.id.et_password);

        new AlertDialog.Builder(this)
                .setTitle("Add Account")
                .setView(view)
                .setPositiveButton("Sign In & Add", (d, w) -> {
                    String email = etEmail.getText().toString().trim();
                    String pass  = etPassword.getText().toString().trim();
                    if (email.isEmpty() || pass.isEmpty()) {
                        Toast.makeText(this, "Enter email and password", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    signInAndSave(email, pass);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void signInAndSave(String email, String password) {
        setLoading(true);
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> {
                    FirebaseUser user = result.getUser();
                    if (user == null) { setLoading(false); return; }
                    String uid = user.getUid();

                    // Fetch role then name, then save + switch
                    new com.example.edulocker.repositories.AuthRepository()
                            .getUserRole(uid, role -> {
                                if (role == null) {
                                    runOnUiThread(() -> {
                                        setLoading(false);
                                        Toast.makeText(this, "No role found for this account",
                                                Toast.LENGTH_SHORT).show();
                                    });
                                    return;
                                }
                                Constants.db().collection("users").document(uid).get()
                                        .addOnSuccessListener(doc -> {
                                            String name = doc.getString("name");
                                            AccountManager.saveAccount(this, email, password,
                                                    uid, name != null ? name : "", role);
                                            session.saveSession(uid, name != null ? name : "", role);
                                            fetchRoleSpecificDataAndRoute(uid, role, email, password);
                                        })
                                        .addOnFailureListener(e -> {
                                            AccountManager.saveAccount(this, email, password, uid, "", role);
                                            session.saveSession(uid, "", role);
                                            fetchRoleSpecificDataAndRoute(uid, role, email, password);
                                        });
                            });
                })
                .addOnFailureListener(e -> runOnUiThread(() -> {
                    setLoading(false);
                    Toast.makeText(this, "Login failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }));
    }

    // ── Switch Account ────────────────────────────────────────────────────────

    private void confirmSwitch(SavedAccount account) {
        new AlertDialog.Builder(this)
                .setTitle("Switch Account")
                .setMessage("Switch to " + account.getDisplayName() + "?")
                .setPositiveButton("Switch", (d, w) -> doSwitch(account))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void doSwitch(SavedAccount account) {
        setLoading(true);
        FirebaseAuth.getInstance()
                .signInWithEmailAndPassword(account.getEmail(), account.getPassword())
                .addOnSuccessListener(result -> {
                    FirebaseUser user = result.getUser();
                    if (user == null) { setLoading(false); return; }
                    session.saveSession(user.getUid(), account.getName(), account.getRole());
                    fetchRoleSpecificDataAndRoute(user.getUid(), account.getRole(),
                            account.getEmail(), account.getPassword());
                })
                .addOnFailureListener(e -> runOnUiThread(() -> {
                    setLoading(false);
                    Toast.makeText(this, "Switch failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }));
    }

    private void fetchRoleSpecificDataAndRoute(String uid, String role, String email, String pass) {
        session.clearRoleSpecificData(); // wipe stale passport/school IDs before saving new ones
        if (User.ROLE_SCHOOL.equals(role) || User.ROLE_TEACHER.equals(role)) {
            Constants.db().collection("users").document(uid).get()
                    .addOnSuccessListener(doc -> {
                        String schoolId = doc.getString("schoolId");
                        if (schoolId != null) session.saveSchoolId(schoolId);
                        runOnUiThread(() -> route(role));
                    })
                    .addOnFailureListener(e -> runOnUiThread(() -> route(role)));
        } else if (User.ROLE_STUDENT.equals(role)) {
            new StudentRepository().getStudentByUserId(uid, new StudentRepository.StudentCallback() {
                @Override public void onSuccess(com.example.edulocker.models.Student s) {
                    session.savePassportId(s.getPassportId());
                    session.saveStudentSchoolId(s.getSchoolId());
                    runOnUiThread(() -> route(role));
                }
                @Override public void onFailure(String error) {
                    runOnUiThread(() -> route(role));
                }
            });
        } else {
            runOnUiThread(() -> route(role));
        }
    }

    private void route(String role) {
        setLoading(false);
        Intent intent;
        switch (role) {
            case User.ROLE_GOVERNMENT: intent = new Intent(this, GovtDashboardActivity.class); break;
            case User.ROLE_SCHOOL:     intent = new Intent(this, SchoolDashboardActivity.class); break;
            case User.ROLE_TEACHER:    intent = new Intent(this, TeacherDashboardActivity.class); break;
            case User.ROLE_PARENT:     intent = new Intent(this, ParentDashboardActivity.class); break;
            default:                   intent = new Intent(this, MainActivity.class); break;
        }
        startActivity(intent);
        finishAffinity();
    }

    // ── Edit Alias ────────────────────────────────────────────────────────────

    private void showAliasDialog(SavedAccount account) {
        EditText et = new EditText(this);
        et.setInputType(InputType.TYPE_CLASS_TEXT);
        et.setHint("e.g. My Gov Account");
        et.setText(account.getAlias() != null ? account.getAlias() : "");

        LinearLayout container = new LinearLayout(this);
        container.setPadding(48, 24, 48, 8);
        container.addView(et);

        new AlertDialog.Builder(this)
                .setTitle("Set Alias for " + account.getName())
                .setView(container)
                .setPositiveButton("Save", (d, w) -> {
                    AccountManager.updateAlias(this, account.getUid(),
                            et.getText().toString().trim());
                    refreshList();
                })
                .setNegativeButton("Clear Alias", (d, w) -> {
                    AccountManager.updateAlias(this, account.getUid(), "");
                    refreshList();
                })
                .setNeutralButton("Cancel", null)
                .show();
    }

    // ── Delete ────────────────────────────────────────────────────────────────

    private void confirmDelete(SavedAccount account) {
        FirebaseUser current = FirebaseAuth.getInstance().getCurrentUser();
        boolean isActive = current != null && current.getUid().equals(account.getUid());

        String msg = isActive
                ? "This is your currently active account. Deleting it will log you out."
                : "Remove \"" + account.getDisplayName() + "\" from saved accounts?";

        new AlertDialog.Builder(this)
                .setTitle("Remove Account")
                .setMessage(msg)
                .setPositiveButton("Remove", (d, w) -> {
                    AccountManager.deleteAccount(this, account.getUid());
                    if (isActive) {
                        FirebaseAuth.getInstance().signOut();
                        session.clearSession();
                        startActivity(new Intent(this, LoginActivity.class));
                        finishAffinity();
                    } else {
                        refreshList();
                        Toast.makeText(this, "Account removed", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        findViewById(R.id.btn_add_account).setEnabled(!loading);
    }
}
