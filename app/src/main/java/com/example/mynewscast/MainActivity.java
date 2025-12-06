package com.example.mynewscast;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
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

    int currentPage = 1;
    boolean isLoading = false;
    boolean isLastPage = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        bindViews();
        setupMenuPopup();
        setupRecyclerView();
        setupInsets();
        setupSearch();
        applySystemUiSetting();

        loadTopHeadlines(currentPage);
    }

    private void bindViews() {
        menu_button = findViewById(R.id.menu_button);
        searchbar = findViewById(R.id.searchbar);
        newsRecyclerView = findViewById(R.id.newsRecyclerView);
    }

    private void setupMenuPopup() {
        menu_button.setOnClickListener(v -> showMenu(v));
    }

    private void showMenu(View v) {
        PopupMenu popup = new PopupMenu(MainActivity.this, v);
        popup.getMenuInflater().inflate(R.menu.main_menu, popup.getMenu());

        popup.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.main_settings) {
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
                return true;
            }
            return false;
        });

        popup.show();
    }

    private void setupRecyclerView() {
        newsAdapter = new NewsAdapter(this, newsList);
        newsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        newsRecyclerView.setAdapter(newsAdapter);

        newsRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();

                int visibleItemCount = layoutManager.getChildCount();
                int totalItemCount = layoutManager.getItemCount();
                int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                if (!isLoading && !isLastPage) {
                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount - 10
                            && firstVisibleItemPosition >= 0) {

                        currentPage++;
                        loadTopHeadlines(currentPage);
                    }
                }
            }
        });

    }

    private void setupInsets() {
        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
        boolean hideUi = prefs.getBoolean("hide_ui", false);

        if (hideUi) return;

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
                return false;
            }
        });
    }

    private void applySystemUiSetting() {
        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
        boolean hideUi = prefs.getBoolean("hide_ui", false);

        if (hideUi) hideSystemBars();
        else showSystemBars();
    }

    private void hideSystemBars() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        );
    }

    private void showSystemBars() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        );
    }

    private void loadTopHeadlines(int page) {
        isLoading = true;

        ApiClient.getService().getTopHeadlines(
                "general",
                "en",
                "in",
                100,     // max articles per page
                page,    // correct page number
                ApiClient.API_KEY
        ).enqueue(new Callback<NewsResponse>() {
            @Override
            public void onResponse(@NonNull Call<NewsResponse> call, Response<NewsResponse> response) {
                isLoading = false;

                if (response.body() != null) {
                    List<NewsResponse.Article> articles = response.body().articles;

                    if (articles.isEmpty()) {
                        isLastPage = true;
                        return;
                    }

                    for (NewsResponse.Article a : articles) {
                        newsList.add(new NewsItem(
                                a.title,
                                a.description,
                                a.image,
                                a.url
                        ));
                    }

                    newsAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<NewsResponse> call, Throwable t) {
                isLoading = false;
                Toast.makeText(MainActivity.this, "Error loading more news", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void loadSearchResults(String query) {
        ApiClient.getService().searchNews(
                query,
                "en",
                100,
                1,
                ApiClient.API_KEY
        ).enqueue(new Callback<NewsResponse>() {
            @Override
            public void onResponse(Call<NewsResponse> call, Response<NewsResponse> response) {
                if (response.body() != null) {
                    newsList.clear();

                    for (NewsResponse.Article a : response.body().articles) {
                        newsList.add(new NewsItem(
                                a.title,
                                a.description,
                                a.image,
                                a.url
                        ));
                    }

                    newsAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<NewsResponse> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Search failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        applySystemUiSetting();
    }
}
