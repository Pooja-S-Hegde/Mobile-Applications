package com.example.smartnotes;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputLayout;

public class LoginActivity extends AppCompatActivity {
    private TextInputLayout emailLayout, passwordLayout, nameLayout;
    private EditText emailInput, passwordInput, nameInput;
    private Button loginButton, signupButton;
    private TextView forgotPasswordText;
    private SharedPreferences prefs;
    private boolean isSignupMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize SharedPreferences
        prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);

        // Initialize views
        nameLayout = findViewById(R.id.nameLayout);
        emailLayout = findViewById(R.id.emailLayout);
        passwordLayout = findViewById(R.id.passwordLayout);
        nameInput = findViewById(R.id.nameInput);
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        loginButton = findViewById(R.id.loginButton);
        signupButton = findViewById(R.id.signupButton);
        forgotPasswordText = findViewById(R.id.forgotPasswordText);

        // Add animations
        Animation fadeIn = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
        fadeIn.setDuration(1000);
        findViewById(R.id.loginContainer).startAnimation(fadeIn);

        // Login button click
        loginButton.setOnClickListener(v -> {
            String name = nameInput.getText().toString();
            String email = emailInput.getText().toString();
            String password = passwordInput.getText().toString();

            if (validateInput(name, email, password)) {
                if (isSignupMode) {
                    // Handle signup
                    if (prefs.contains(email)) {
                        Toast.makeText(this, "Email already registered!", Toast.LENGTH_SHORT).show();
                    } else {
                        prefs.edit().putString(email+"_name", name).apply();
                        prefs.edit().putString(email, password).apply();
                        prefs.edit().putString("logged_in_email", email).apply();
                        Toast.makeText(this, "Signup successful! Please login.", Toast.LENGTH_SHORT).show();
                        isSignupMode = false;
                        updateUI();
                    }
                } else {
                    // Handle login
                    String savedPassword = prefs.getString(email, null);
                    if (savedPassword != null && savedPassword.equals(password)) {
                        prefs.edit().putString("logged_in_email", email).apply();
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish();
                    } else {
                        Toast.makeText(this, "Invalid email or password!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        // Signup button click
        signupButton.setOnClickListener(v -> {
            isSignupMode = !isSignupMode;
            updateUI();
        });

        // Forgot password click
        forgotPasswordText.setOnClickListener(v -> {
            String email = emailInput.getText().toString();
            if (email.isEmpty()) {
                Toast.makeText(this, "Please enter your email first", Toast.LENGTH_SHORT).show();
                return;
            }
            if (prefs.contains(email)) {
                Toast.makeText(this, "Password reset link sent to your email!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Email not registered!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUI() {
        if (isSignupMode) {
            loginButton.setText("Sign Up");
            signupButton.setText("Back to Login");
            forgotPasswordText.setVisibility(View.GONE);
            nameLayout.setVisibility(View.VISIBLE);
        } else {
            loginButton.setText("Login");
            signupButton.setText("Sign Up");
            forgotPasswordText.setVisibility(View.VISIBLE);
            nameLayout.setVisibility(View.GONE);
        }
    }

    private boolean validateInput(String name, String email, String password) {
        boolean isValid = true;
        if (isSignupMode) {
            if (name.isEmpty()) {
                nameLayout.setError("Name is required");
                isValid = false;
            } else {
                nameLayout.setError(null);
            }
        }
        if (email.isEmpty()) {
            emailLayout.setError("Email is required");
            isValid = false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailLayout.setError("Invalid email format");
            isValid = false;
        } else {
            emailLayout.setError(null);
        }
        if (password.isEmpty()) {
            passwordLayout.setError("Password is required");
            isValid = false;
        } else if (password.length() < 6) {
            passwordLayout.setError("Password must be at least 6 characters");
            isValid = false;
        } else {
            passwordLayout.setError(null);
        }
        return isValid;
    }
} 