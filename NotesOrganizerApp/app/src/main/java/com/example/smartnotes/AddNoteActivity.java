package com.example.smartnotes;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import java.util.Calendar;
import android.app.Activity;
import android.net.Uri;
import android.provider.OpenableColumns;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import com.google.android.material.appbar.MaterialToolbar;
import java.io.File;
import java.io.FileOutputStream;


public class AddNoteActivity extends AppCompatActivity {
    private static final int FILE_PICK_REQUEST = 1001;
    private Uri lastSelectedFileUri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_note);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        EditText editTitle = findViewById(R.id.editTitle);
        EditText editContent = findViewById(R.id.editContent);
        EditText editDate = findViewById(R.id.editDate);
        Button btnSave = findViewById(R.id.btnSave);
        Button btnUploadFile = findViewById(R.id.btnUploadFile);

        Intent intent = getIntent();
        int editIndex = intent.getIntExtra("editIndex", -1);
        if (intent.hasExtra("title")) {
            editTitle.setText(intent.getStringExtra("title"));
        }
        if (intent.hasExtra("content")) {
            editContent.setText(intent.getStringExtra("content"));
        }
        if (intent.hasExtra("date")) {
            editDate.setText(intent.getStringExtra("date"));
        }
        editDate.setFocusable(false);
        editDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year1, month1, dayOfMonth) -> {
                String date = dayOfMonth + "/" + (month1 + 1) + "/" + year1;
                editDate.setText(date);
            }, year, month, day);
            datePickerDialog.show();
        });

        btnUploadFile.setOnClickListener(v -> {
            Intent fileIntent = new Intent(Intent.ACTION_GET_CONTENT);
            fileIntent.setType("*/*");
            fileIntent.addCategory(Intent.CATEGORY_OPENABLE);
            startActivityForResult(Intent.createChooser(fileIntent, "Select Note File"), FILE_PICK_REQUEST);
        });

        btnSave.setOnClickListener(v -> {
            String title = editTitle.getText().toString().trim();
            String content = editContent.getText().toString().trim();
            String date = editDate.getText().toString().trim();
            if (title.isEmpty() || content.isEmpty() || date.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent resultIntent = new Intent();
            resultIntent.putExtra("title", title);
            resultIntent.putExtra("content", content);
            resultIntent.putExtra("date", date);
            if (editIndex >= 0) {
                resultIntent.putExtra("editIndex", editIndex);
            }
            if (lastSelectedFileUri != null) {
                resultIntent.putExtra("fileUri", lastSelectedFileUri.toString());
            }
            setResult(RESULT_OK, resultIntent);
            finish();
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILE_PICK_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri != null) {
                String mimeType = getContentResolver().getType(uri);
                EditText editContent = findViewById(R.id.editContent);
                String fileName = getFileName(uri);

                if (mimeType != null && mimeType.equals("application/pdf")) {
                    try {
                        // Create a temporary file to store the PDF
                        File tempFile = new File(getCacheDir(), "temp_" + fileName);
                        copyUriToFile(uri, tempFile);

                        // Save the file path and URI for later use
                        lastSelectedFileUri = uri;
                        String pdfMarker = "[PDF]" + fileName + "|" + uri.toString();
                        editContent.setText(pdfMarker);

                        Toast.makeText(this, "PDF attached: " + fileName, Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        Toast.makeText(this, "Failed to process PDF", Toast.LENGTH_SHORT).show();
                    }
                } else if (mimeType != null && mimeType.startsWith("text")) {
                    try {
                        String content = readTextFromUri(uri);
                        editContent.setText(content);
                    } catch (IOException e) {
                        Toast.makeText(this, "Failed to read file", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // For other file types
                    lastSelectedFileUri = uri;
                    String fileMarker = "[FILE]" + fileName + "|" + uri.toString();
                    editContent.setText(fileMarker);
                    Toast.makeText(this, "File attached: " + fileName, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private String getFileName(Uri uri) {
        String fileName = "File";
        try {
            android.database.Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (nameIndex >= 0) {
                    fileName = cursor.getString(nameIndex);
                }
                cursor.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fileName;
    }

    private String readTextFromUri(Uri uri) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        try (InputStream inputStream = getContentResolver().openInputStream(uri);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
                stringBuilder.append("\n");
            }
        }
        return stringBuilder.toString().trim();
    }

    private void copyUriToFile(Uri uri, File destination) throws IOException {
        try (InputStream is = getContentResolver().openInputStream(uri);
             FileOutputStream os = new FileOutputStream(destination)) {
            byte[] buffer = new byte[4096];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
            os.flush();
        }
    }
}
