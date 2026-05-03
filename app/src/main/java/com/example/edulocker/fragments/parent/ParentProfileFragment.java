package com.example.edulocker.fragments.parent;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.edulocker.activities.LoginActivity;
import com.example.edulocker.activities.SettingsActivity;
import com.example.edulocker.databinding.FragmentParentProfileBinding;
import com.example.edulocker.models.Student;
import com.example.edulocker.repositories.StudentRepository;
import com.example.edulocker.utils.SessionManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ParentProfileFragment extends Fragment {

    private FragmentParentProfileBinding binding;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentParentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SessionManager session = new SessionManager(requireContext());
        FirebaseUser u = FirebaseAuth.getInstance().getCurrentUser();

        binding.tvUserName.setText(session.getUserName() != null ? session.getUserName() : "Parent");
        binding.tvUserEmail.setText(u != null && u.getEmail() != null ? u.getEmail() : "");

        loadChildInfo(u != null ? u.getUid() : null);

        binding.ivSettings.setOnClickListener(v ->
                startActivity(new Intent(getContext(), SettingsActivity.class)));

        binding.btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            session.clearSession();
            startActivity(new Intent(getContext(), LoginActivity.class));
            requireActivity().finishAffinity();
        });

        binding.swipeRefresh.setColorSchemeResources(
                com.example.edulocker.R.color.primary, com.example.edulocker.R.color.accent);
        binding.swipeRefresh.setOnRefreshListener(() -> {
            FirebaseUser current = FirebaseAuth.getInstance().getCurrentUser();
            loadChildInfo(current != null ? current.getUid() : null);
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        FirebaseUser u = FirebaseAuth.getInstance().getCurrentUser();
        loadChildInfo(u != null ? u.getUid() : null);
    }

    private void loadChildInfo(String parentUid) {
        if (parentUid == null) return;
        if (binding != null) binding.progressBar.setVisibility(View.VISIBLE);

        new StudentRepository().getStudentByParentUserId(parentUid, new StudentRepository.StudentCallback() {
            @Override
            public void onSuccess(Student student) {
                if (!isAdded() || binding == null) return;
                requireActivity().runOnUiThread(() -> {
                    if (binding == null) return;
                    binding.progressBar.setVisibility(View.GONE);
                    binding.swipeRefresh.setRefreshing(false);
                    populateChild(student);
                });
            }

            @Override
            public void onFailure(String error) {
                if (!isAdded() || binding == null) return;
                requireActivity().runOnUiThread(() -> {
                    if (binding == null) return;
                    binding.progressBar.setVisibility(View.GONE);
                    binding.swipeRefresh.setRefreshing(false);
                });
            }
        });
    }

    private void populateChild(Student student) {
        binding.tvChildName.setText(student.getName() != null ? student.getName() : "—");
        binding.tvChildPassportId.setText(student.getPassportId() != null ? student.getPassportId() : "—");
        binding.tvChildClass.setText(student.getStudentClass() != null ? student.getStudentClass() : "—");
        binding.tvChildSchool.setText(student.getSchoolName() != null ? student.getSchoolName() : "—");
        String kyc = student.getKycStatus() != null ? student.getKycStatus() : "Pending";
        binding.tvChildKyc.setText(kyc);

        boolean verified = "Verified".equals(kyc);
        binding.tvKycBadge.setText(verified ? "✓ Verified" : "Pending KYC");
        binding.tvKycBadge.setTextColor(verified
                ? getResources().getColor(com.example.edulocker.R.color.verified_green, null)
                : 0xFFFFAA00);
        binding.tvKycBadge.setVisibility(View.VISIBLE);

        binding.cardChildInfo.setVisibility(View.VISIBLE);
    }

    @Override public void onDestroyView() { super.onDestroyView(); binding = null; }
}
