package com.example.smartnotes;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.appbar.MaterialToolbar;
import android.view.View;
import android.widget.Toast;

public class ViewNoteActivity extends AppCompatActivity {
    private TextView titleView;
    private TextView contentView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_note);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        titleView = findViewById(R.id.viewNoteTitle);
        contentView = findViewById(R.id.viewNoteContent);

        Intent intent = getIntent();
        String title = intent.getStringExtra("title");
        String content = intent.getStringExtra("content");

        titleView.setText(title);

        if (content != null && content.startsWith("[PDF]")) {
            // Handle PDF content
            String[] parts = content.substring(5).split("\\|");
            if (parts.length == 2) {
                String fileName = parts[0];
                String uriString = parts[1];
                contentView.setText("PDF File: " + fileName);
                contentView.setOnClickListener(v -> openPdf(Uri.parse(uriString)));
            }
        } else if (content != null && content.startsWith("[FILE]")) {
            // Handle other file types
            String[] parts = content.substring(6).split("\\|");
            if (parts.length == 2) {
                String fileName = parts[0];
                String uriString = parts[1];
                contentView.setText("File: " + fileName);
                contentView.setOnClickListener(v -> openFile(Uri.parse(uriString)));
            }
        } else {
            // Regular text content
            contentView.setText(content);
            contentView.setOnClickListener(null);
        }
    }

    private void openPdf(Uri uri) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "application/pdf");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "No PDF viewer app found", Toast.LENGTH_SHORT).show();
        }
    }

    private void openFile(Uri uri) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            String mimeType = getContentResolver().getType(uri);
            intent.setDataAndType(uri, mimeType);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "No app found to open this file type", Toast.LENGTH_SHORT).show();
        }
    }
}