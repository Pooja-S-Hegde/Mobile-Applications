package com.example.smartnotes;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewOutlineProvider;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.graphics.Color;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.util.ArrayList;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

public class MainActivity extends AppCompatActivity {

    // Inner Note class
    static class Note {
        String title, content, date;
        boolean isFavorite = false;
        boolean isTrashed = false;
        Note(String title, String content, String date) {
            this.title = title;
            this.content = content;
            this.date = date;
        }
    }

    // UI elements
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    MaterialToolbar toolbar;
    FloatingActionButton fab;
    LinearLayout notesContainer;

    // Data
    private static final int ADD_NOTE_REQUEST = 1;
    ArrayList<LinearLayout> noteBoxes = new ArrayList<>();
    ArrayList<Note> notes = new ArrayList<>();
    SharedPreferences prefs;
    Gson gson = new Gson();

    // Add a field to track the current filter
    enum FilterType { ALL, FAVORITES, TRASH }
    FilterType currentFilter = FilterType.ALL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        toolbar = findViewById(R.id.toolbar);
        fab = findViewById(R.id.fab);
        notesContainer = findViewById(R.id.notes_container);

        // Set up toolbar
        setSupportActionBar(toolbar);
        updateToolbar();

        // Set up navigation drawer
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_favorites) {
                currentFilter = FilterType.FAVORITES;
                updateToolbar();
                refreshNotes();
            } else if (id == R.id.nav_trash) {
                currentFilter = FilterType.TRASH;
                updateToolbar();
                refreshNotes();
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(MainActivity.this, ProfileActivity.class));
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        // FAB to open AddNoteActivity
        fab.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddNoteActivity.class);
            startActivityForResult(intent, ADD_NOTE_REQUEST);
        });

        // Load notes from SharedPreferences
        prefs = getSharedPreferences("notes_prefs", MODE_PRIVATE);
        loadNotes();
        refreshNotes();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ADD_NOTE_REQUEST && resultCode == RESULT_OK && data != null) {
            String title = data.getStringExtra("title");
            String content = data.getStringExtra("content");
            String date = data.getStringExtra("date");
            int editIndex = data.getIntExtra("editIndex", -1);

            if (editIndex >= 0) {
                Note edited = notes.get(editIndex);
                edited.title = title;
                edited.content = content;
                edited.date = date;
                edited.isTrashed = false;
            } else {
                Note newNote = new Note(title, content, date);
                newNote.isTrashed = false;
                newNote.isFavorite = false;
                notes.add(newNote);
            }
            currentFilter = FilterType.ALL;
            refreshNotes();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveNotes();
    }

    @Override
    protected void onResume() {
        super.onResume();
        currentFilter = FilterType.ALL;
        refreshNotes();
    }

    private void saveNotes() {
        String json = gson.toJson(notes);
        prefs.edit().putString("notes", json).apply();
    }

    private void loadNotes() {
        String json = prefs.getString("notes", null);
        if (json != null) {
            notes = gson.fromJson(json, new TypeToken<ArrayList<Note>>(){}.getType());
            if (notes == null) notes = new ArrayList<>();
        }
        if (notes.isEmpty()) {
            Note defaultNote = new Note(
                "Welcome to Notes Organizer App!",
                "This app helps you organize your notes efficiently.\n\nFeatures:\n- Add, edit, and delete notes\n- Attach files and PDFs\n- Mark notes as favorites\n- Move notes to trash and restore them\n- Beautiful and user-friendly interface\n\nGet started by adding your first note!",
                "1/1/2025"
            );
            notes.add(defaultNote);
            saveNotes();
        }
    }

    private void refreshNotes() {
        notesContainer.removeAllViews();
        // Sort notes by date descending (most recent first)
        notes.sort((a, b) -> {
            try {
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("d/M/yyyy");
                java.util.Date da = sdf.parse(a.date);
                java.util.Date db = sdf.parse(b.date);
                return db.compareTo(da);
            } catch (Exception e) { return 0; }
        });
        // Move default note to the top if present
        for (int i = 0; i < notes.size(); i++) {
            if (notes.get(i).title.startsWith("Welcome to Notes Organizer App")) {
                Note def = notes.remove(i);
                notes.add(0, def);
                break;
            }
        }
        ArrayList<Note> filteredNotes = new ArrayList<>();
        if (currentFilter == FilterType.ALL) {
            for (Note n : notes) if (!n.isTrashed) filteredNotes.add(n);
        } else if (currentFilter == FilterType.FAVORITES) {
            for (Note n : notes) if (n.isFavorite && !n.isTrashed) filteredNotes.add(n);
        } else if (currentFilter == FilterType.TRASH) {
            for (Note n : notes) if (n.isTrashed) filteredNotes.add(n);
        }
        if (filteredNotes.isEmpty()) {
            TextView hint = new TextView(this);
            hint.setText("No notes yet. Tap the + button below to add your first note!");
            hint.setTextSize(18);
            hint.setTextColor(0xFF888888);
            hint.setGravity(Gravity.CENTER);
            hint.setTypeface(android.graphics.Typeface.create("times_new_roman", android.graphics.Typeface.NORMAL));
            notesContainer.addView(hint);
        } else {
            for (int i = 0; i < filteredNotes.size(); i++) {
                addNoteBox(notes.indexOf(filteredNotes.get(i)));
            }
        }
    }

    private void addNoteBox(int index) {
        Note note = notes.get(index);
        if (currentFilter == FilterType.ALL && note.isTrashed) return;
        if (currentFilter == FilterType.FAVORITES && (!note.isFavorite || note.isTrashed)) return;
        if (currentFilter == FilterType.TRASH && !note.isTrashed) return;
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setBackgroundResource(R.drawable.content_background);
        box.setPadding(32, 24, 32, 24);
        box.setElevation(4f);
        box.setOutlineProvider(ViewOutlineProvider.BACKGROUND);
        box.setClipToOutline(true);
        // Title TextView
        TextView titleView = new TextView(this);
        titleView.setText(note.title);
        titleView.setTextSize(20);
        titleView.setTextColor(Color.parseColor("#222222"));
        titleView.setTypeface(android.graphics.Typeface.create("times_new_roman", android.graphics.Typeface.BOLD));
        box.addView(titleView);
        // Date TextView
        TextView dateView = new TextView(this);
        dateView.setText(note.date);
        dateView.setTextSize(14);
        dateView.setTextColor(Color.parseColor("#666666"));
        dateView.setPadding(0, 8, 0, 16);
        dateView.setTypeface(android.graphics.Typeface.create("times_new_roman", android.graphics.Typeface.NORMAL));
        box.addView(dateView);
        box.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ViewNoteActivity.class);
            intent.putExtra("title", note.title);
            intent.putExtra("content", note.content);
            startActivity(intent);
        });
        // Favorite Icon Button
        ImageButton favBtn = new ImageButton(this);
        favBtn.setImageResource(note.isFavorite ? R.drawable.ic_favorite_filled : R.drawable.ic_favorite_border);
        favBtn.setBackgroundColor(0x00000000);
        favBtn.setPadding(24, 24, 24, 24);
        favBtn.setOnClickListener(v -> {
            note.isFavorite = !note.isFavorite;
            saveNotes();
            refreshNotes();
        });
        // Edit Icon Button
        ImageButton editBtn = new ImageButton(this);
        editBtn.setImageResource(R.drawable.ic_edit);
        editBtn.setBackgroundColor(0x00000000);
        editBtn.setPadding(24, 24, 24, 24);
        editBtn.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddNoteActivity.class);
            intent.putExtra("title", note.title);
            intent.putExtra("content", note.content);
            intent.putExtra("date", note.date);
            intent.putExtra("editIndex", index);
            startActivityForResult(intent, ADD_NOTE_REQUEST);
        });
        // Trash Icon Button
        ImageButton trashBtn = new ImageButton(this);
        trashBtn.setImageResource(R.drawable.ic_delete);
        trashBtn.setBackgroundColor(0x00000000);
        trashBtn.setPadding(24, 24, 24, 24);
        trashBtn.setOnClickListener(v -> {
            if (currentFilter == FilterType.TRASH) {
                notes.remove(index);
            } else {
                note.isTrashed = !note.isTrashed;
            }
            saveNotes();
            refreshNotes();
        });
        LinearLayout iconRow = new LinearLayout(this);
        iconRow.setOrientation(LinearLayout.HORIZONTAL);
        iconRow.setGravity(Gravity.END);
        iconRow.addView(favBtn);
        iconRow.addView(editBtn);
        iconRow.addView(trashBtn);
        box.addView(iconRow);
        Animation anim = AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left);
        box.startAnimation(anim);
        notesContainer.addView(box);
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else if (currentFilter != FilterType.ALL) {
            currentFilter = FilterType.ALL;
            updateToolbar();
            refreshNotes();
        } else {
            super.onBackPressed();
        }
    }

    private void updateToolbar() {
        if (currentFilter == FilterType.FAVORITES) {
            toolbar.setTitle("Favorites");
            toolbar.setNavigationIcon(R.drawable.ic_back);
            toolbar.setNavigationOnClickListener(v -> onBackPressed());
        } else if (currentFilter == FilterType.TRASH) {
            toolbar.setTitle("Trash");
            toolbar.setNavigationIcon(R.drawable.ic_back);
            toolbar.setNavigationOnClickListener(v -> onBackPressed());
        } else {
            toolbar.setTitle("Smart Notes");
            toolbar.setNavigationIcon(R.drawable.ic_menu);
            toolbar.setNavigationOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));
        }
    }
}
