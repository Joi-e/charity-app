package com.example.charity_app;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Login extends AppCompatActivity {

    private EditText emailEditText, passwordEditText;
    private TextView forgotPasswordTextView, noAccountTextView;
    private Button loginButton;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_screen);

        // Initialize UI elements
        emailEditText = findViewById(R.id.accountEmail);
        passwordEditText = findViewById(R.id.accountPassword);
        forgotPasswordTextView = findViewById(R.id.forgotPassword);
        noAccountTextView = findViewById(R.id.noAccount);
        loginButton = findViewById(R.id.buttonAccount);

        // Initialize Firebase Authentication
        firebaseAuth = FirebaseAuth.getInstance();

        loginButton.setOnClickListener(view -> loginUser());

        forgotPasswordTextView.setOnClickListener(view -> forgotPassword());

        noAccountTextView.setOnClickListener(view -> {
            // Navigate to the registration screen
            Intent intent = new Intent(Login.this, Register.class);
            startActivity(intent);
        });
    }

    public void forgotPassword() {
        String email = emailEditText.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Enter your email address first.", Toast.LENGTH_SHORT).show();
            return;
        }

        firebaseAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(Login.this, "If there is an account associated with this email, a password reset code has been sent.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(Login.this, "Password request failed. Please check your email address.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loginUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString();

        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d("LoginActivity", "signInWithEmail:success");
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        Toast.makeText(Login.this, "Login successful.", Toast.LENGTH_SHORT).show();

                        // Example: Navigate to MainActivity
                        Intent intent = new Intent(Login.this, MainActivity.class);
                        startActivity(intent);
                    } else {
                        Log.w("LoginActivity", "signInWithEmail:failure", task.getException());
                        Toast.makeText(Login.this, "Authentication failed. Check your email and password.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}



