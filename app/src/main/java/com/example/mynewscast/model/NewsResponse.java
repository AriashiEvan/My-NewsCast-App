package com.example.mynewscast.model;

import java.util.List;

public class NewsResponse {
    public int totalArticles;
    public List<Article> articles;

    public static class Article {
        public String title;
        public String description;
        public String url;
        public String image;
        public String publishedAt;
    }
}
