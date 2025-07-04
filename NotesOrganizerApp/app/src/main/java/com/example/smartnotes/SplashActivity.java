package com.example.smartnotes;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {
    TextView typingText;
    String fullText = "Welcome to Notes Organizer app";
    int index = 0;
    Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        typingText = findViewById(R.id.typingText);

        typeText();
    }

    private void typeText() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (index < fullText.length()) {
                    typingText.setText(fullText.substring(0, index + 1));
                    index++;
                    handler.postDelayed(this, 100);
                } else {
                    // Delay and move to login page
                    handler.postDelayed(() -> {
                        startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                        finish();
                    }, 1000);
                }
            }
        }, 100);
    }
}
