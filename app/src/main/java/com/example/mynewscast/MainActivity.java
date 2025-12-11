package com.example.mynewscast;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.SearchView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mynewscast.api.ApiClient;
import com.example.mynewscast.model.NewsItem;
import com.example.mynewscast.model.NewsResponse;

import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
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
        menu_button.setOnClickListener(this::showMenu);
    }

    private void showMenu(View v) {
        PopupMenu popup = new PopupMenu(MainActivity.this, v);
        popup.getMenuInflater().inflate(R.menu.main_menu, popup.getMenu());

        popup.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.main_settings) {
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
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

                LinearLayoutManager layoutManager =
                        (LinearLayoutManager) recyclerView.getLayoutManager();

                int visibleItemCount = layoutManager.getChildCount();
                int totalItemCount = layoutManager.getItemCount();
                int firstVisibleItemPos = layoutManager.findFirstVisibleItemPosition();

                if (!isLoading && !isLastPage) {
                    if ((visibleItemCount + firstVisibleItemPos) >= totalItemCount - 10
                            && firstVisibleItemPos >= 0) {

                        currentPage++;
                        loadTopHeadlines(currentPage);
                    }
                }
            }
        });
    }

    private void setupInsets() {
        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
        if (prefs.getBoolean("hide_ui", false)) return;

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
            public boolean onQueryTextChange(String text) {
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
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
    }

    private void loadTopHeadlines(int page) {
        isLoading = true;

        ApiClient.getService().getTopHeadlines(
                "general",
                "en",
                "in",
                10,
                page,
                ApiClient.API_KEY
        ).enqueue(new Callback<NewsResponse>() {

            @Override
            public void onResponse(Call<NewsResponse> call, Response<NewsResponse> response) {
                isLoading = false;

                if (!response.isSuccessful()) {
                    openErrorPageFromCode(response.code());
                    return;
                }

                if (response.body() == null) {
                    openErrorPage("Invalid Response", "Server returned no data.");
                    return;
                }

                List<NewsResponse.Article> articles = response.body().articles;
                if (articles.isEmpty()) {
                    isLastPage = true;
                    return;
                }

                for (NewsResponse.Article a : articles) {
                    newsList.add(new NewsItem(a.title, a.description, a.image, a.url));
                }
                newsAdapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Call<NewsResponse> call, Throwable t) {
                isLoading = false;
                handleThrowable(t);
            }
        });
    }
    private void loadSearchResults(String query) {
        ApiClient.getService().searchNews(
                query, "en", 10, 1, ApiClient.API_KEY
        ).enqueue(new Callback<NewsResponse>() {

            @Override
            public void onResponse(Call<NewsResponse> call, Response<NewsResponse> response) {

                if (!response.isSuccessful()) {
                    openErrorPageFromCode(response.code());
                    return;
                }

                if (response.body() == null) {
                    openErrorPage("Invalid Response", "Server returned no data.");
                    return;
                }

                newsList.clear();
                for (NewsResponse.Article a : response.body().articles) {
                    newsList.add(new NewsItem(a.title, a.description, a.image, a.url));
                }

                newsAdapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Call<NewsResponse> call, Throwable t) {
                handleThrowable(t);
            }
        });
    }
    private void handleThrowable(Throwable t) {
        if (t instanceof UnknownHostException) {
            openErrorPage("No Internet",
                    "Please check your connection and try again.");
        } else if (t instanceof SocketTimeoutException) {
            openErrorPage("Timeout",
                    "The server took too long to respond.");
        } else {
            openErrorPage("Unexpected Error", t.getMessage());
        }
    }

    private void openErrorPageFromCode(int code) {
        if (code == 404)
            openErrorPage("Page Not Found", "Error 404 — Content unavailable.");
        else if (code == 500)
            openErrorPage("Server Error", "Error 500 — Server is down.");
        else if (code == 400)
            openErrorPage("Bad Request", "Error 400 — Something went wrong.");
        else
            openErrorPage("Error " + code, "Unexpected server error occurred.");
    }

    private void openErrorPage(String title, String message) {
        Intent intent = new Intent(MainActivity.this, ErrorActivity.class);
        intent.putExtra("error_title", title);
        intent.putExtra("error_message", message);
        intent.putExtra("retry_action", "retry_main");
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        applySystemUiSetting();
    }
}
