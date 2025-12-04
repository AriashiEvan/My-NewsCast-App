package com.example.mynewscast.api;

import com.example.mynewscast.model.NewsResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiService {

    // Top headlines
    @GET("top-headlines")
    Call<NewsResponse> getTopHeadlines(
            @Query("category") String category,
            @Query("lang") String language,
            @Query("country") String country,
            @Query("max") int max,
            @Query("apikey") String apiKey
    );

    // Search
    @GET("search")
    Call<NewsResponse> searchNews(
            @Query("q") String query,
            @Query("lang") String language,
            @Query("max") int max,
            @Query("apikey") String apiKey
    );
}
