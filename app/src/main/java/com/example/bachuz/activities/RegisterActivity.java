package com.example.bachuz.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.bachuz.R;
import com.example.bachuz.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.common.hash.Hashing;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.nio.charset.StandardCharsets;
import java.util.List;

public class RegisterActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    private EditText emailInputField, usernameInputField, passwordInputField, retypePasswordInputField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        setViews();
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void registerUser() {
        String email = emailInputField.getText().toString();
        String username = usernameInputField.getText().toString();
        String password = passwordInputField.getText().toString();
        String encodedPassword = Hashing.sha256().hashString(email+password, StandardCharsets.UTF_8).toString();
        User user = new User(username, email, encodedPassword);
        db.collection("users").add(user)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        emailInputField.setError(getString(R.string.internal_error));
                    }
                });
    }

    private void tryRegister() {
        String email = emailInputField.getText().toString();

        if (!validateEmail() || !validatePassword()){
            return;
        }

        db.collection("users").whereEqualTo("email", email).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            QuerySnapshot querySnapshot = task.getResult();
                            List<DocumentSnapshot> documents = querySnapshot.getDocuments();
                            if (documents.isEmpty()) {
                                registerUser();
                            } else {
                                emailInputField.setError(getString(R.string.existing_email_error));
                            }
                        } else {
                            emailInputField.setError(getString(R.string.internal_error));
                        }
                    }
                });
    }

    private void setViews(){
        emailInputField = findViewById(R.id.emailRegisterInput);
        usernameInputField = findViewById(R.id.usernameRegisterInput);
        passwordInputField = findViewById(R.id.passwordRegisterInput);
        retypePasswordInputField = findViewById(R.id.retypePasswordRegisterInput);
        Button registerButton = findViewById(R.id.register_button);
        Button cancelButton = findViewById(R.id.cancel_button);

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tryRegister();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private boolean validateEmail(){
        String email = emailInputField.getText().toString();
        if (email.isEmpty()) {
            emailInputField.setError(getString(R.string.input_empty));
            return false;
        }
        return true;
    }

    private boolean validatePassword(){
        String password = passwordInputField.getText().toString();
        String repassword = retypePasswordInputField.getText().toString();
        if (password.isEmpty()) {
            passwordInputField.setError(getString(R.string.input_empty));
            return false;
        }
        if (password.length() < 8) {
            passwordInputField.setError(getString(R.string.password_too_short));
            return false;
        }
        if (!repassword.equals(password)){
            retypePasswordInputField.setError(getString(R.string.password_not_match));
            return false;
        }
        return true;
    }
}