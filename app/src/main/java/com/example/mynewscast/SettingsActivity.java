package com.example.mynewscast;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Switch;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

public class SettingsActivity extends AppCompatActivity {

    private Switch darkModeSwitch, hideUiSwitch;
    private ImageButton backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        darkModeSwitch = findViewById(R.id.darkModeSwitch);
        hideUiSwitch = findViewById(R.id.hideUiSwitch);
        backButton = findViewById(R.id.back_button);

        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);

        boolean isDark = prefs.getBoolean("dark_mode", false);
        boolean isUiHidden = prefs.getBoolean("hide_ui", false);

        darkModeSwitch.setChecked(isDark);
        hideUiSwitch.setChecked(isUiHidden);

        AppCompatDelegate.setDefaultNightMode(
                isDark ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );

        darkModeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("dark_mode", isChecked);
            editor.apply();

            AppCompatDelegate.setDefaultNightMode(
                    isChecked ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
            );
        });

        hideUiSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("hide_ui", isChecked);
            editor.apply();
        });

        backButton.setOnClickListener(v -> finish());
    }
}
