package com.example.expensetracker;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";

    private EditText etUsername, etEmail, etPassword, etConfirmPassword;
    private Button btnRegister;
    private TextView tvLogin;
    private FirebaseAuth mAuth;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize views
        etUsername = findViewById(R.id.et_username);
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        btnRegister = findViewById(R.id.btn_register);
        tvLogin = findViewById(R.id.tv_login);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Initialize ProgressDialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Creating account...");
        progressDialog.setCancelable(false);

        // Button click listener for registration
        btnRegister.setOnClickListener(v -> registerUser());

        // TextView click listener for navigation to LoginActivity
        tvLogin.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    private void registerUser() {
        // Get data from the input fields
        String username = etUsername.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString();
        String confirmPassword = etConfirmPassword.getText().toString();

        // Validation checks
        if (username.isEmpty()) {
            etUsername.setError("Please enter username");
            etUsername.requestFocus();
            return;
        }
        if (email.isEmpty()) {
            etEmail.setError("Please enter email");
            etEmail.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Enter a valid email");
            etEmail.requestFocus();
            return;
        }
        if (password.isEmpty()) {
            etPassword.setError("Please enter password");
            etPassword.requestFocus();
            return;
        }
        if (password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            etPassword.requestFocus();
            return;
        }
        if (confirmPassword.isEmpty()) {
            etConfirmPassword.setError("Please confirm password");
            etConfirmPassword.requestFocus();
            return;
        }
        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Passwords do not match");
            etConfirmPassword.requestFocus();
            return;
        }

        // Show progress dialog before starting Firebase operation
        progressDialog.show();

        // Create user in Firebase Authentication
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "FirebaseAuth: User account created successfully.");

                        // Get the current user and update their display name
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            // Update user profile with username
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(username)
                                    .build();

                            firebaseUser.updateProfile(profileUpdates)
                                    .addOnCompleteListener(profileTask -> {
                                        if (profileTask.isSuccessful()) {
                                            Log.d(TAG, "User profile updated with username.");
                                        }
                                    });

                            // Get the user UID and save data in Firebase Database
                            String uid = firebaseUser.getUid();
                            User user = new User(username, email);

                            // Save user data to Firebase Realtime Database
                            FirebaseDatabase.getInstance().getReference("Users")
                                    .child(uid)
                                    .setValue(user)
                                    .addOnCompleteListener(dbTask -> {
                                        // Dismiss the dialog once all operations complete
                                        progressDialog.dismiss();

                                        if (dbTask.isSuccessful()) {
                                            Log.d(TAG, "FirebaseDatabase: User data saved successfully.");

                                            // Clear all fields
                                            clearFields();

                                            // Show success message with better feedback
                                            Toast.makeText(this, "Account created successfully! Welcome " + username + "!", Toast.LENGTH_LONG).show();

                                            // Go to login layout
                                            Intent intent = new Intent(this, LoginActivity.class);
                                            intent.putExtra("registration_success", true);
                                            intent.putExtra("user_email", email);
                                            startActivity(intent);
                                            finish();
                                        } else {
                                            // Log the database error
                                            Log.e(TAG, "FirebaseDatabase: Failed to save user data.", dbTask.getException());
                                            String dbError = dbTask.getException() != null ? dbTask.getException().getMessage() : "Unknown error";
                                            Toast.makeText(this, "Failed to save user data: " + dbError, Toast.LENGTH_LONG).show();
                                        }
                                    });
                        }
                    } else {
                        // Dismiss dialog on failure
                        progressDialog.dismiss();

                        // Log Firebase Authentication failure
                        Log.e(TAG, "FirebaseAuth: Registration failed.", task.getException());
                        String errorMessage = "Registration failed.";
                        if (task.getException() != null) {
                            String firebaseError = task.getException().getMessage();
                            if (firebaseError != null) {
                                if (firebaseError.contains("already in use")) {
                                    errorMessage = "This email is already registered. Please use a different email or try logging in.";
                                } else if (firebaseError.contains("weak password")) {
                                    errorMessage = "Password is too weak. Please use a stronger password.";
                                } else {
                                    errorMessage = "Registration failed: " + firebaseError;
                                }
                            }
                        }
                        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void clearFields() {
        etUsername.setText("");
        etEmail.setText("");
        etPassword.setText("");
        etConfirmPassword.setText("");

        // Clear any existing errors
        etUsername.setError(null);
        etEmail.setError(null);
        etPassword.setError(null);
        etConfirmPassword.setError(null);
    }

    // User class to store user details in Firebase
    public static class User {
        public String username, email;

        public User() {} // Default constructor required for Firebase

        public User(String username, String email) {
            this.username = username;
            this.email = email;
        }
    }
}