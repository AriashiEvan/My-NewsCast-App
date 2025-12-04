package com.example.mynewscast;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mynewscast.api.ApiClient;
import com.example.mynewscast.api.ApiService;
import com.example.mynewscast.model.NewsItem;
import com.example.mynewscast.model.NewsResponse;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    ImageView menu_button;
    SearchView searchbar;
    RecyclerView newsRecyclerView;

    NewsAdapter newsAdapter;
    List<NewsItem> newsList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        bindViews();
        setupRecyclerView();
        setupInsets();
        setupSearch();

        loadTopHeadlines();
    }

    private void bindViews() {
        menu_button = findViewById(R.id.menu_button);
        searchbar = findViewById(R.id.searchbar);
        newsRecyclerView = findViewById(R.id.newsRecyclerView);
    }

    private void setupRecyclerView() {
        newsAdapter = new NewsAdapter(this, newsList);
        newsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        newsRecyclerView.setAdapter(newsAdapter);
    }

    private void setupInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void setupSearch() {
        searchbar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                loadSearchResults(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false; // No live search
            }
        });
    }

    // -------------------------------
    // FETCH TOP HEADLINES (DEFAULT)
    // -------------------------------
    private void loadTopHeadlines() {
        ApiService service = ApiClient.getService();

        service.getTopHeadlines("in", ApiClient.API_KEY)
                .enqueue(new Callback<NewsResponse>() {
                    @Override
                    public void onResponse(Call<NewsResponse> call, Response<NewsResponse> response) {
                        if (response.body() != null) {

                            newsList.clear();

                            for (NewsResponse.Article a : response.body().articles) {
                                newsList.add(new NewsItem(
                                        a.title,
                                        a.description,
                                        a.urlToImage,
                                        a.url
                                ));
                            }

                            newsAdapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onFailure(Call<NewsResponse> call, Throwable t) {
                        Toast.makeText(MainActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e("NEWS_ERROR", t.getMessage());
                    }
                });
    }

    // -------------------------------
    // SEARCH NEWS
    // -------------------------------
    private void loadSearchResults(String query) {
        ApiService service = ApiClient.getService();

        service.searchNews(query, "en", ApiClient.API_KEY)
                .enqueue(new Callback<NewsResponse>() {
                    @Override
                    public void onResponse(Call<NewsResponse> call, Response<NewsResponse> response) {
                        if (response.body() != null) {
                            newsList.clear();

                            for (NewsResponse.Article a : response.body().articles) {
                                newsList.add(new NewsItem(
                                        a.title,
                                        a.description,
                                        a.urlToImage,
                                        a.url
                                ));
                            }

                            newsAdapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onFailure(Call<NewsResponse> call, Throwable t) {
                        Toast.makeText(MainActivity.this, "Search failed!", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
