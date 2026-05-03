package com.example.edulocker.fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.edulocker.activities.KycVerificationActivity;
import com.example.edulocker.activities.SettingsActivity;
import com.example.edulocker.activities.VirtualIdCardActivity;
import com.example.edulocker.databinding.FragmentProfileBinding;
import com.example.edulocker.models.Student;
import com.example.edulocker.repositories.StudentRepository;
import com.example.edulocker.utils.QRCodeHelper;
import com.example.edulocker.utils.SessionManager;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadProfile();

        binding.btnViewIdCard.setVisibility(View.GONE); // shown only after KYC verified
        binding.btnViewIdCard.setOnClickListener(v ->
                startActivity(new Intent(getContext(), VirtualIdCardActivity.class)));

        binding.ivSettings.setOnClickListener(v ->
                startActivity(new Intent(getContext(), SettingsActivity.class)));
    

        binding.swipeRefresh.setColorSchemeResources(
                com.example.edulocker.R.color.primary, com.example.edulocker.R.color.accent);
        binding.swipeRefresh.setOnRefreshListener(() -> {
            loadProfile();
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        loadProfile();
    }

    private void loadProfile() {
        String passportId = new SessionManager(requireContext()).getPassportId();
        if (passportId == null) return;

        new StudentRepository().getStudent(passportId, new StudentRepository.StudentCallback() {
            @Override
            public void onSuccess(Student student) {
                if (!isAdded() || binding == null) return;
                requireActivity().runOnUiThread(() -> {
                    if (binding != null) binding.swipeRefresh.setRefreshing(false);
                    if (binding == null) return;
                    populateProfile(student);
                });
            }
            @Override
            public void onFailure(String error) {}
        });
    }

    private void populateProfile(Student student) {
        binding.tvStudentName.setText(student.getName());
        binding.tvPassportId.setText("Passport ID: " + student.getPassportId());
        binding.tvSchoolName.setText(student.getSchoolName());
        binding.tvUserId.setText("User ID: " + student.getUserId());
        binding.tvKycStatus.setText(student.getKycStatus());
        binding.tvFatherName.setText(student.getFatherName());
        binding.tvDob.setText(student.getDob());
        binding.tvBloodGroup.setText(student.getBloodGroup());
        binding.tvContact.setText(student.getContactNumber());
        binding.tvAddress.setText(student.getAddress());
        binding.tvPinCode.setText(student.getPinCode());
        binding.tvClass.setText(student.getStudentClass());

        Bitmap qr = QRCodeHelper.generateQR(
                QRCodeHelper.buildQrContent(student.getPassportId(), student.getName(),
                        student.getSchoolName()), 150, 150);
        if (qr != null) binding.ivQrCode.setImageBitmap(qr);

        boolean isVerified = "Verified".equals(student.getKycStatus());
        binding.tvVerifiedBadge.setVisibility(isVerified ? View.VISIBLE : View.GONE);
        binding.btnViewIdCard.setVisibility(isVerified ? View.VISIBLE : View.GONE);

        if (!isVerified) {
            binding.btnGetVerified.setVisibility(View.VISIBLE);
            binding.btnGetVerified.setOnClickListener(v -> {
                Intent intent = new Intent(getContext(), KycVerificationActivity.class);
                intent.putExtra(KycVerificationActivity.EXTRA_PASSPORT_ID, student.getPassportId());
                intent.putExtra(KycVerificationActivity.EXTRA_NAME, student.getName());
                intent.putExtra(KycVerificationActivity.EXTRA_PHONE, student.getContactNumber());
                startActivity(intent);
            });
        } else {
            binding.btnGetVerified.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
