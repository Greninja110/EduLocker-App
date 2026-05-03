package com.example.edulocker.fragments.govt;

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
import com.example.edulocker.activities.govt.GovDocTypesActivity;
import com.example.edulocker.databinding.FragmentGovtProfileBinding;
import com.example.edulocker.utils.SessionManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class GovtProfileFragment extends Fragment {

    private FragmentGovtProfileBinding binding;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentGovtProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        populate();

        binding.ivSettings.setOnClickListener(v ->
                startActivity(new Intent(getContext(), SettingsActivity.class)));

        binding.btnDocTypes.setOnClickListener(v ->
                startActivity(new Intent(getContext(), GovDocTypesActivity.class)));

        binding.btnLogout.setOnClickListener(v -> {
            SessionManager session = new SessionManager(requireContext());
            FirebaseAuth.getInstance().signOut();
            session.clearSession();
            startActivity(new Intent(getContext(), LoginActivity.class));
            requireActivity().finishAffinity();
        });

        binding.swipeRefresh.setColorSchemeResources(
                com.example.edulocker.R.color.primary, com.example.edulocker.R.color.accent);
        binding.swipeRefresh.setOnRefreshListener(() -> {
            populate();
            binding.swipeRefresh.setRefreshing(false);
        });
    }

    private void populate() {
        SessionManager session = new SessionManager(requireContext());
        String name = session.getUserName();
        binding.tvUserName.setText(name != null ? name : "Government");

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String email = user != null ? user.getEmail() : "";
        binding.tvUserEmail.setText(email != null ? email : "");
        binding.tvDetailEmail.setText(email != null ? email : "");

        String uid = user != null ? user.getUid() : "";
        binding.tvUserUid.setText("UID: " + uid);
    }

    @Override public void onDestroyView() { super.onDestroyView(); binding = null; }
}
