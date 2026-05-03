package com.example.edulocker.repositories;

import android.content.Context;

import com.example.edulocker.BuildConfig;
import com.example.edulocker.models.NewsItem;
import com.example.edulocker.utils.NewsCache;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class NewsRepository {

    private static final String BASE_URL =
            "https://newsdata.io/api/1/news?apikey=%s&country=in&language=en&category=education,politics,top,science";

    public interface NewsListCallback {
        void onSuccess(List<NewsItem> items);
        void onFailure(String error);
    }

    private static final OkHttpClient client = new OkHttpClient();

    /**
     * Returns news for the given category filter.
     * Serves from the local 3-hour IST cache when valid; fetches from the API otherwise.
     * On network failure falls back to stale cache so the UI is never empty.
     *
     * @param categoryFilter  NewsData.io category string ("education", "politics", "top",
     *                        "science") or null / "all" to show everything.
     */
    public void getNews(Context context, String categoryFilter, NewsListCallback callback) {
        NewsCache cache = new NewsCache(context);

        if (cache.isCacheValid()) {
            String cachedJson = cache.getCachedJson();
            if (cachedJson != null) {
                callback.onSuccess(filterByCategory(parseResponse(cachedJson), categoryFilter));
                return;
            }
        }

        // Cache expired or missing — fetch fresh from API
        String url = String.format(BASE_URL, BuildConfig.NEWSDATA_API_KEY);
        Request request = new Request.Builder().url(url).get().build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // Network error — return stale cache rather than showing an empty screen
                String stale = cache.getCachedJson();
                if (stale != null) {
                    callback.onSuccess(filterByCategory(parseResponse(stale), categoryFilter));
                } else {
                    callback.onFailure("No internet and no cached news available.");
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String body = response.body() != null ? response.body().string() : "";
                if (!response.isSuccessful()) {
                    String stale = cache.getCachedJson();
                    if (stale != null) {
                        callback.onSuccess(filterByCategory(parseResponse(stale), categoryFilter));
                    } else {
                        callback.onFailure("News API error: " + response.code());
                    }
                    return;
                }
                cache.saveCache(body);
                callback.onSuccess(filterByCategory(parseResponse(body), categoryFilter));
            }
        });
    }

    // Parse the raw NewsData.io JSON into NewsItem list
    private List<NewsItem> parseResponse(String json) {
        List<NewsItem> items = new ArrayList<>();
        try {
            JSONObject root    = new JSONObject(json);
            JSONArray results  = root.optJSONArray("results");
            if (results == null) return items;

            for (int i = 0; i < results.length(); i++) {
                JSONObject obj = results.getJSONObject(i);

                List<String> categories = new ArrayList<>();
                JSONArray catArr = obj.optJSONArray("category");
                if (catArr != null) {
                    for (int c = 0; c < catArr.length(); c++) {
                        categories.add(catArr.getString(c));
                    }
                }

                String imageUrl = obj.isNull("image_url") ? null : obj.optString("image_url", null);

                items.add(new NewsItem(
                        obj.optString("article_id", ""),
                        obj.optString("title", ""),
                        obj.optString("description", ""),
                        obj.optString("source_name", ""),
                        imageUrl,
                        obj.optString("pubDate", ""),
                        obj.optString("link", ""),
                        categories
                ));
            }
        } catch (Exception ignored) {}
        return items;
    }

    // Client-side filter by NewsData.io category string
    private List<NewsItem> filterByCategory(List<NewsItem> items, String categoryFilter) {
        if (categoryFilter == null || "all".equalsIgnoreCase(categoryFilter)) return items;
        List<NewsItem> filtered = new ArrayList<>();
        String lowerFilter = categoryFilter.toLowerCase();
        for (NewsItem item : items) {
            if (item.getCategories() != null && item.getCategories().contains(lowerFilter)) {
                filtered.add(item);
            }
        }
        return filtered;
    }
}
