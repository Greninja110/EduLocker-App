package com.example.edulocker.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.edulocker.models.SavedAccount;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class AccountManager {

    private static final String PREFS_NAME = "EduLockerAccounts";
    private static final String KEY_ACCOUNTS = "saved_accounts";

    public static void saveAccount(Context ctx, String email, String password,
                                   String uid, String name, String role) {
        List<SavedAccount> accounts = getAccounts(ctx);
        // Update existing entry if uid already saved
        for (SavedAccount a : accounts) {
            if (a.getUid().equals(uid)) {
                a.setEmail(email);
                a.setPassword(password);
                a.setName(name);
                a.setRole(role);
                persist(ctx, accounts);
                return;
            }
        }
        // New entry — no alias yet
        accounts.add(new SavedAccount(uid, email, password, role, name, null));
        persist(ctx, accounts);
    }

    public static List<SavedAccount> getAccounts(Context ctx) {
        List<SavedAccount> list = new ArrayList<>();
        try {
            String json = prefs(ctx).getString(KEY_ACCOUNTS, "[]");
            JSONArray arr = new JSONArray(json);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                SavedAccount a = new SavedAccount(
                        o.optString("uid"),
                        o.optString("email"),
                        o.optString("password"),
                        o.optString("role"),
                        o.optString("name"),
                        o.optString("alias", null)
                );
                list.add(a);
            }
        } catch (Exception ignored) {}
        return list;
    }

    public static void updateAlias(Context ctx, String uid, String alias) {
        List<SavedAccount> accounts = getAccounts(ctx);
        for (SavedAccount a : accounts) {
            if (a.getUid().equals(uid)) {
                a.setAlias(alias.trim().isEmpty() ? null : alias.trim());
                break;
            }
        }
        persist(ctx, accounts);
    }

    public static void deleteAccount(Context ctx, String uid) {
        List<SavedAccount> accounts = getAccounts(ctx);
        accounts.removeIf(a -> a.getUid().equals(uid));
        persist(ctx, accounts);
    }

    private static void persist(Context ctx, List<SavedAccount> accounts) {
        try {
            JSONArray arr = new JSONArray();
            for (SavedAccount a : accounts) {
                JSONObject o = new JSONObject();
                o.put("uid",      a.getUid());
                o.put("email",    a.getEmail());
                o.put("password", a.getPassword());
                o.put("role",     a.getRole());
                o.put("name",     a.getName());
                o.put("alias",    a.getAlias() != null ? a.getAlias() : "");
                arr.put(o);
            }
            prefs(ctx).edit().putString(KEY_ACCOUNTS, arr.toString()).apply();
        } catch (Exception ignored) {}
    }

    private static SharedPreferences prefs(Context ctx) {
        return ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
}
