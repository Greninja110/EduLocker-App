package com.example.edulocker.utils;

import android.graphics.Bitmap;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.journeyapps.barcodescanner.BarcodeEncoder;

public class QRCodeHelper {

    public static Bitmap generateQR(String content, int width, int height) {
        try {
            BarcodeEncoder encoder = new BarcodeEncoder();
            return encoder.encodeBitmap(content, BarcodeFormat.QR_CODE, width, height);
        } catch (WriterException e) {
            return null;
        }
    }

    // QR content encodes the student's passport ID for verification
    public static String buildQrContent(String passportId, String studentName, String schoolName) {
        return "EDULOCKER-VERIFY\n"
                + "ID:" + passportId + "\n"
                + "Name:" + studentName + "\n"
                + "School:" + schoolName + "\n"
                + "Issued by: Govt.";
    }
}
