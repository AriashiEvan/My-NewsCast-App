package com.example.mynewscast;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ErrorActivity extends AppCompatActivity {

    String retryAction = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_error);

        TextView title = findViewById(R.id.error_title);
        TextView message = findViewById(R.id.error_message);
        Button retryBtn = findViewById(R.id.retry_button);

        String errorTitle = getIntent().getStringExtra("error_title");
        String errorMessage = getIntent().getStringExtra("error_message");
        retryAction = getIntent().getStringExtra("retry_action");

        title.setText(errorTitle);
        message.setText(errorMessage);

        retryBtn.setOnClickListener(v -> {
            if ("retry_main".equals(retryAction)) {
                Intent i = new Intent(ErrorActivity.this, MainActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
            }
            else if ("retry_webview".equals(retryAction)) {
                Intent i = new Intent(ErrorActivity.this, WebViewActivity.class);
                i.putExtra("news_url", getIntent().getStringExtra("failed_url"));
                startActivity(i);
            }

            finish();
        });
    }
}
