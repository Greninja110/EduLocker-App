package com.example.edulocker.utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.view.View;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;

public class VirtualIdCardGenerator {

    // Export any View as a PNG bitmap (Canvas-based, works on Android 16+)
    public static Bitmap viewToBitmap(View view) {
        if (view.getWidth() == 0 || view.getHeight() == 0) return null;
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        return bitmap;
    }

    // Save bitmap to cache and return URI for sharing
    public static Uri saveBitmapForSharing(Context context, Bitmap bitmap, String filename) {
        try {
            File cachDir = new File(context.getCacheDir(), "shared_cards");
            if (!cachDir.exists()) cachDir.mkdirs();
            File file = new File(cachDir, filename + ".png");
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();
            return FileProvider.getUriForFile(context,
                    context.getPackageName() + ".provider", file);
        } catch (Exception e) {
            return null;
        }
    }

    // Share bitmap via Android share sheet
    public static void shareCard(Context context, Bitmap bitmap, String studentName) {
        Uri uri = saveBitmapForSharing(context, bitmap, "edulocker_id_" + studentName);
        if (uri == null) return;
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("image/png");
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
        shareIntent.putExtra(Intent.EXTRA_TEXT, "EduLocker Virtual Identity Card — " + studentName);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        context.startActivity(Intent.createChooser(shareIntent, "Share ID Card"));
    }

    // Save a PdfDocument to cache and share via share sheet
    public static void sharePdf(Context context, PdfDocument pdf, String studentName) {
        try {
            File cacheDir = new File(context.getCacheDir(), "shared_cards");
            if (!cacheDir.exists()) cacheDir.mkdirs();
            File file = new File(cacheDir, "edulocker_id_" + studentName + ".pdf");
            FileOutputStream fos = new FileOutputStream(file);
            pdf.writeTo(fos);
            fos.flush();
            fos.close();
            pdf.close();

            Uri uri = FileProvider.getUriForFile(context,
                    context.getPackageName() + ".provider", file);
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("application/pdf");
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
            shareIntent.putExtra(Intent.EXTRA_TEXT, "EduLocker Virtual Identity Card — " + studentName);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            context.startActivity(Intent.createChooser(shareIntent, "Share ID Card as PDF"));
        } catch (Exception e) {
            if (pdf != null) pdf.close();
        }
    }
}
