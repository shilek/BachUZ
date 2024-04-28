package com.example.bachuz;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.common.hash.Hashing;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class LoginActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    private EditText loginInputField, passwordInputField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        setViews();
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void login() {
        String email = loginInputField.getText().toString();
        String password = passwordInputField.getText().toString();
        db.collection("users").whereEqualTo("email", email).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            QuerySnapshot querySnapshot = task.getResult();
                            List<DocumentSnapshot> documents = querySnapshot.getDocuments();
                            if (documents.isEmpty()) {
                                loginInputField.setError(getString(R.string.email_not_registered));
                            } else {
                                Map<String, Object> document = documents.get(0).getData();
                                String encodedPassword = Hashing.sha256().hashString(email+password, StandardCharsets.UTF_8).toString();
                                if (document == null) {
                                    loginInputField.setError(getString(R.string.internal_error));
                                    return;
                                }
                                if (Objects.equals(document.get("password"), encodedPassword)) {
                                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                    startActivity(intent);
                                } else {
                                    passwordInputField.setError(getString(R.string.wrong_password));
                                }
                            }
                        } else {
                            loginInputField.setError(getString(R.string.internal_error));
                        }
                    }
                });
    }

    private void setViews(){
        loginInputField = findViewById(R.id.emailLoginInput);
        passwordInputField = findViewById(R.id.passwordLoginInput);
        Button loginButton = findViewById(R.id.login_button);
        Button registerButton = findViewById(R.id.start_register_button);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateFields()) {
                    login();
                }
            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }

    private boolean validateFields(){
        String email = loginInputField.getText().toString();
        String password = passwordInputField.getText().toString();

        if (email.isEmpty()) {
            loginInputField.setError(getString(R.string.input_empty));
            return false;
        } else if (password.isEmpty()) {
            passwordInputField.setError(getString(R.string.input_empty));
            return false;
        } else {
            return true;
        }
    }
}