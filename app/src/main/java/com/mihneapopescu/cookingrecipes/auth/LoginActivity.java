package com.mihneapopescu.cookingrecipes.auth;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.mihneapopescu.cookingrecipes.MainActivity;
import com.mihneapopescu.cookingrecipes.R;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    private EditText etEmail;
    private EditText etPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);

        Button btnLogin = findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginUser();
            }
        });

        Button btnRegister = findViewById(R.id.btnRegister);
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerUser();
            }
        });
    }

    private void loginUser() {
        String email = etEmail.getText().toString();
        String password = etPassword.getText().toString();

        if (!isValidEmail(email)) {
            etEmail.setError("Invalid email");
            return;
        }

        if (!isValidPassword(password)) {
            etPassword.setError("Invalid password");
            return;
        }

        MainActivity.hideKeyboard(this);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // If sign in is successful, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();
                            Toast.makeText(LoginActivity.this, "Successfully Logged in.", Toast.LENGTH_SHORT).show();
                            // Start the MainActivity
                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                            // Finish the LoginActivity
                            finish();
                        } else {
                            // If sign in fails, display a message to the user
                            Toast.makeText(LoginActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                            // updateUI(null);
                        }
                    }
                });
    }

    private void registerUser() {
        String email = etEmail.getText().toString();
        String password = etPassword.getText().toString();

        if (!isValidEmail(email)) {
            etEmail.setError("Invalid email");
            return;
        }

        if (!isValidPassword(password)) {
            etPassword.setError("Invalid password");
            return;
        }

        MainActivity.hideKeyboard(this);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // If sign up is successful, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();
                            Toast.makeText(LoginActivity.this, "Successfully Registered.", Toast.LENGTH_SHORT).show();
                            // Start the MainActivity
                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                            // Finish the LoginActivity
                            finish();
                        } else {
                            // If sign up fails, display a message to the user
                            Log.w("Authentication", "signInWithEmail:failure", task.getException());  // Log the error
                            Toast.makeText(LoginActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                            // updateUI(null);
                        }
                    }
                });
    }

    // Validation
    private boolean isValidEmail(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private boolean isValidPassword(String password) {
        return password.length() >= 6;
    }
}
