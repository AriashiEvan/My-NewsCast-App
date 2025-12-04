package com.example.mynewscast.api;

import com.example.mynewscast.model.NewsResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiService {

    @GET("top-headlines")
    Call<NewsResponse> getTopHeadlines(
            @Query("country") String country,
            @Query("apiKey") String apiKey
    );

    @GET("everything")
    Call<NewsResponse> searchNews(
            @Query("q") String query,
            @Query("language") String language,
            @Query("apiKey") String apiKey
    );
}
