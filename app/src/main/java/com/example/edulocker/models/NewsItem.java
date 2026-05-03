package com.example.edulocker.models;

import java.util.List;

public class NewsItem {
    private String articleId;
    private String title;
    private String description;
    private String sourceName;   // e.g. "The Hindu", "NDTV"
    private String imageUrl;
    private String pubDate;      // "2024-01-15 12:30:00" from NewsData.io
    private String link;
    private List<String> categories; // ["education","politics"] etc.

    public NewsItem() {}

    public NewsItem(String articleId, String title, String description,
                    String sourceName, String imageUrl, String pubDate,
                    String link, List<String> categories) {
        this.articleId   = articleId;
        this.title       = title;
        this.description = description;
        this.sourceName  = sourceName;
        this.imageUrl    = imageUrl;
        this.pubDate     = pubDate;
        this.link        = link;
        this.categories  = categories;
    }

    public String getArticleId()           { return articleId; }
    public String getTitle()               { return title; }
    public String getDescription()         { return description; }
    public String getSourceName()          { return sourceName; }
    public String getImageUrl()            { return imageUrl; }
    public String getPubDate()             { return pubDate; }
    public String getLink()                { return link; }
    public List<String> getCategories()    { return categories; }
}
