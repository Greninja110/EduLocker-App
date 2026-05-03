package com.example.edulocker.activities;

import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.edulocker.databinding.ActivityVirtualIdCardBinding;
import com.example.edulocker.models.Student;
import com.example.edulocker.repositories.StudentRepository;
import com.example.edulocker.utils.QRCodeHelper;
import com.example.edulocker.utils.SessionManager;
import com.example.edulocker.utils.VirtualIdCardGenerator;

public class VirtualIdCardActivity extends AppCompatActivity {

    private ActivityVirtualIdCardBinding binding;
    private Student currentStudent;
    private boolean showingFront = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityVirtualIdCardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        WindowCompat.getInsetsController(getWindow(), binding.getRoot())
                .setAppearanceLightStatusBars(true);

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            int statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
            v.setPadding(0, statusBarHeight, 0, 0);
            return insets;
        });

        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnShare.setOnClickListener(v -> shareAsPdf());
        binding.btnFlip.setOnClickListener(v -> flipCard());
        binding.btnCopyId.setOnClickListener(v -> copyPassportId());

        loadStudent();
    }

    private void flipCard() {
        if (showingFront) {
            binding.cardFront.setVisibility(View.INVISIBLE);
            binding.cardBack.setVisibility(View.VISIBLE);
            binding.btnFlip.setText("Show Front");
        } else {
            binding.cardFront.setVisibility(View.VISIBLE);
            binding.cardBack.setVisibility(View.INVISIBLE);
            binding.btnFlip.setText("Show Back");
        }
        showingFront = !showingFront;
    }

    private void loadStudent() {
        String passportId = new SessionManager(this).getPassportId();
        if (passportId == null) return;

        new StudentRepository().getStudent(passportId, new StudentRepository.StudentCallback() {
            @Override
            public void onSuccess(Student student) {
                currentStudent = student;
                runOnUiThread(() -> populateCard(student));
            }
            @Override
            public void onFailure(String error) {}
        });
    }

    private void populateCard(Student student) {
        binding.tvCardSchoolName.setText(student.getSchoolName());
        binding.tvCardStudentName.setText(student.getName());
        // Tabular rows: set value only (key labels are in the XML)
        binding.tvCardFather.setText(student.getFatherName());
        binding.tvCardMother.setText(student.getMotherName());
        binding.tvCardDob.setText(student.getDob());
        binding.tvCardBlood.setText(student.getBloodGroup());
        binding.tvCardAddress.setText(student.getAddress());
        binding.tvCardContact.setText(student.getContactNumber());
        binding.tvCardPassportId.setText(student.getPassportId());
        binding.tvCardState.setText(student.getState() != null ? student.getState() : "");
        binding.tvCardVerified.setText("Verified Student");
        binding.tvCardUserId.setText("User ID: " + student.getUserId());

        android.graphics.Bitmap qr = QRCodeHelper.generateQR(
                QRCodeHelper.buildQrContent(student.getPassportId(), student.getName(),
                        student.getSchoolName()), 200, 200);
        if (qr != null) binding.ivCardQr.setImageBitmap(qr);

        binding.tvBackSchoolName.setText(student.getSchoolName());
        binding.tvBackAddress.setText(student.getAddress());
    }

    private void copyPassportId() {
        if (currentStudent == null) return;
        android.content.ClipboardManager clipboard =
                (android.content.ClipboardManager) getSystemService(android.content.Context.CLIPBOARD_SERVICE);
        android.content.ClipData clip = android.content.ClipData.newPlainText(
                "Passport ID", currentStudent.getPassportId());
        clipboard.setPrimaryClip(clip);
        android.widget.Toast.makeText(this, "Passport ID copied!", android.widget.Toast.LENGTH_SHORT).show();
    }

    private void shareAsPdf() {
        if (currentStudent == null) return;

        // Both cards are always laid out (INVISIBLE keeps their dimensions).
        // Use post() to ensure the layout pass is complete before drawing.
        binding.cardFront.post(() -> {
            PdfDocument pdf = new PdfDocument();

            // Page 1 — Front card
            int fw = binding.cardFront.getWidth();
            int fh = binding.cardFront.getHeight();
            if (fw > 0 && fh > 0) {
                PdfDocument.PageInfo pi1 = new PdfDocument.PageInfo.Builder(fw, fh, 1).create();
                PdfDocument.Page p1 = pdf.startPage(pi1);
                binding.cardFront.draw(p1.getCanvas());
                pdf.finishPage(p1);
            }

            // Page 2 — Back card (INVISIBLE but still laid out)
            int bw = binding.cardBack.getWidth();
            int bh = binding.cardBack.getHeight();
            if (bw > 0 && bh > 0) {
                PdfDocument.PageInfo pi2 = new PdfDocument.PageInfo.Builder(bw, bh, 2).create();
                PdfDocument.Page p2 = pdf.startPage(pi2);
                binding.cardBack.draw(p2.getCanvas());
                pdf.finishPage(p2);
            }

            VirtualIdCardGenerator.sharePdf(this, pdf, currentStudent.getName());
        });
    }
}
