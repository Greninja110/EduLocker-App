package com.example.edulocker.utils;

import android.content.Context;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.Calendar;
import java.util.TimeZone;

/**
 * Stores the raw NewsData.io JSON response to internal app storage.
 * Cache is valid for the current 3-hour IST window (00:00, 03:00, 06:00 ... 21:00).
 * At most 8 API calls per day, well within the 200/day free tier.
 */
public class NewsCache {

    private static final String CACHE_FILE = "news_cache.json";
    private static final String KEY_SLOT   = "slot";
    private static final String KEY_DATA   = "data";

    private final Context context;

    public NewsCache(Context context) {
        this.context = context.getApplicationContext();
    }

    /**
     * Returns true if cached data belongs to the current 3-hour IST slot.
     * Slots reset at 00:00, 03:00, 06:00, 09:00, 12:00, 15:00, 18:00, 21:00 IST.
     */
    public boolean isCacheValid() {
        try {
            File file = new File(context.getFilesDir(), CACHE_FILE);
            if (!file.exists()) return false;
            String content = readFile(file);
            JSONObject wrapper = new JSONObject(content);
            long savedSlot = wrapper.getLong(KEY_SLOT);
            return savedSlot == getCurrentIstSlot();
        } catch (Exception e) {
            return false;
        }
    }

    /** Returns the raw JSON string saved from the last API call, or null. */
    public String getCachedJson() {
        try {
            File file = new File(context.getFilesDir(), CACHE_FILE);
            if (!file.exists()) return null;
            String content = readFile(file);
            JSONObject wrapper = new JSONObject(content);
            return wrapper.getString(KEY_DATA);
        } catch (Exception e) {
            return null;
        }
    }

    /** Saves raw API response JSON along with the current slot identifier. */
    public void saveCache(String rawApiJson) {
        try {
            JSONObject wrapper = new JSONObject();
            wrapper.put(KEY_SLOT, getCurrentIstSlot());
            wrapper.put(KEY_DATA, rawApiJson);
            File file = new File(context.getFilesDir(), CACHE_FILE);
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(wrapper.toString().getBytes("UTF-8"));
            fos.close();
        } catch (Exception ignored) {}
    }

    // Slot ID = (year * 10000) + (dayOfYear * 10) + (hourOfDay / 3)
    // Each slot is unique per 3-hour IST window.
    private long getCurrentIstSlot() {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Kolkata"));
        int year      = cal.get(Calendar.YEAR);
        int dayOfYear = cal.get(Calendar.DAY_OF_YEAR);
        int hourBlock = cal.get(Calendar.HOUR_OF_DAY) / 3; // 0..7
        return (long)(year * 10000) + (dayOfYear * 10) + hourBlock;
    }

    private String readFile(File file) throws Exception {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) sb.append(line);
        reader.close();
        return sb.toString();
    }
}
