package com.example.mynewscast;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

public class WebViewActivity extends AppCompatActivity {

    WebView webView;
    ProgressBar progressBar;
    String url;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);

        applySystemUiSetting();

        webView = findViewById(R.id.webView);
        progressBar = findViewById(R.id.webProgress);

        url = getIntent().getStringExtra("news_url");

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);

        webView.setWebChromeClient(new WebChromeClient());

        webView.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageFinished(WebView view, String url) {
                progressBar.animate().alpha(0f).setDuration(300);
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {

                Intent intent = new Intent(WebViewActivity.this, ErrorActivity.class);
                int code = error.getErrorCode();

                if (code == WebViewClient.ERROR_HOST_LOOKUP) {
                    intent.putExtra("error_title", "No Internet");
                    intent.putExtra("error_message", "Cannot load page without internet.");
                }
                else if (code == WebViewClient.ERROR_TIMEOUT) {
                    intent.putExtra("error_title", "Timeout");
                    intent.putExtra("error_message", "The page took too long to load.");
                }
                else {
                    intent.putExtra("error_title", "Page Error");
                    intent.putExtra("error_message", "Error code: " + code);
                }

                intent.putExtra("retry_action", "retry_webview");
                intent.putExtra("failed_url", url); // always use original URL
                startActivity(intent);

                finish();
            }
        });

        webView.loadUrl(url);
    }

    private void applySystemUiSetting() {
        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
        boolean hideUi = prefs.getBoolean("hide_ui", false);

        if (hideUi) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            );
        }
    }

    @SuppressLint("GestureBackNavigation")
    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
