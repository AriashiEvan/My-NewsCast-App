package com.example.mynewscast.model;

import java.util.List;

public class NewsResponse {

    public String status;
    public int totalResults;
    public List<Article> articles;

    public static class Article {
        public String title;
        public String description;
        public String url;
        public String urlToImage;
    }
}
