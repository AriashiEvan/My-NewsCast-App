package com.example.mynewscast.api;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

    private static final String BASE_URL = "https://gnews.io/api/v4/";

    // PUT YOUR NEWSAPI KEY HERE
    public static final String API_KEY = "033e039acbb709c2d9e4c6ac198e92fe";

    private static Retrofit retrofit = null;

    public static Retrofit getRetrofit() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    public static ApiService getService() {
        return getRetrofit().create(ApiService.class);
    }
}
