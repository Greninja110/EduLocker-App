package com.example.edulocker.utils;

import com.example.edulocker.BuildConfig;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Msg91Helper {
    private static final String BASE_URL = "https://control.msg91.com/api/v5/";
    private static final OkHttpClient client = new OkHttpClient();
    private static final MediaType JSON = MediaType.get("application/json");

    public interface OtpCallback {
        void onSuccess(String message);
        void onFailure(String error);
    }

    private static final String FALLBACK_OTP = "1234";

    public static void sendOtp(String phone, OtpCallback callback) {
        if (!BuildConfig.MSG91_CONFIGURED) {
            callback.onSuccess("OTP sent (dev mode — use " + FALLBACK_OTP + ")");
            return;
        }
        try {
            JSONObject body = new JSONObject();
            body.put("template_id", BuildConfig.MSG91_TEMPLATE_ID);
            body.put("mobile", "91" + phone);
            body.put("authkey", BuildConfig.MSG91_API_KEY);

            Request request = new Request.Builder()
                    .url(BASE_URL + "otp")
                    .post(RequestBody.create(body.toString(), JSON))
                    .addHeader("accept", "application/json")
                    .addHeader("content-type", "application/json")
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    callback.onFailure("Network error: " + e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        callback.onSuccess("OTP sent successfully");
                    } else {
                        callback.onFailure("Failed to send OTP. Code: " + response.code());
                    }
                }
            });
        } catch (Exception e) {
            callback.onFailure("Error: " + e.getMessage());
        }
    }

    public static void verifyOtp(String phone, String otp, OtpCallback callback) {
        if (!BuildConfig.MSG91_CONFIGURED) {
            if (FALLBACK_OTP.equals(otp)) {
                callback.onSuccess("OTP verified");
            } else {
                callback.onFailure("Invalid OTP");
            }
            return;
        }
        try {
            Request request = new Request.Builder()
                    .url(BASE_URL + "otp/verify?authkey=" + BuildConfig.MSG91_API_KEY
                            + "&mobile=91" + phone + "&otp=" + otp)
                    .get()
                    .addHeader("accept", "application/json")
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    callback.onFailure("Network error: " + e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        callback.onSuccess("OTP verified");
                    } else {
                        callback.onFailure("Invalid OTP");
                    }
                }
            });
        } catch (Exception e) {
            callback.onFailure("Error: " + e.getMessage());
        }
    }

    // Send credentials SMS to newly created parent/teacher/student
    public static void sendCredentialsSms(String phone, String name, String email, String password, OtpCallback callback) {
        if (!BuildConfig.MSG91_CONFIGURED) {
            callback.onSuccess("Credentials SMS skipped (dev mode)");
            return;
        }
        try {
            String message = "Welcome to EduLocker! Your account has been created.\n"
                    + "Name: " + name + "\n"
                    + "Email: " + email + "\n"
                    + "Password: " + password + "\n"
                    + "Download EduLocker app and login. -EduLocker";

            JSONObject body = new JSONObject();
            body.put("sender", BuildConfig.MSG91_SENDER_ID);
            body.put("route", "4");
            body.put("sms", new org.json.JSONArray()
                    .put(new JSONObject()
                            .put("message", message)
                            .put("to", new org.json.JSONArray().put("91" + phone))));

            Request request = new Request.Builder()
                    .url("https://api.msg91.com/api/v2/sendsms")
                    .post(RequestBody.create(body.toString(), JSON))
                    .addHeader("authkey", BuildConfig.MSG91_API_KEY)
                    .addHeader("accept", "application/json")
                    .addHeader("content-type", "application/json")
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    callback.onFailure("SMS failed: " + e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    callback.onSuccess("Credentials SMS sent");
                }
            });
        } catch (Exception e) {
            callback.onFailure("Error: " + e.getMessage());
        }
    }
}
