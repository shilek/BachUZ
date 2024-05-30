package com.example.bachuz.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.bachuz.R;
import com.example.bachuz.models.Event;
import com.example.bachuz.models.KeyValue;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;


public class EventActivity extends AppCompatActivity {

    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    private EditText eventNameEditText;
    private FloatingActionButton saveButton;
    private FloatingActionButton cancelButton;
    private Event eventData;
    private String eventId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_event);
        setViews();
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            eventNameEditText.setText(extras.getString("name"));
            eventId = extras.getString("id");
            getEventData();
        }
    }

    private void getImage(String image) {
        StorageReference storageRef = storage.getReference();
        StorageReference islandRef = storageRef.child(image);

        final long ONE_MEGABYTE = 1024 * 1024;
        islandRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                Log.d("download", "SUCCESS");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
            }
        });
    }

    private void getEventData(){
        firestore.collection("events").document(eventId).get()
                .addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot documentSnapshot = (DocumentSnapshot)task.getResult();
                            Map<String, Object> document = documentSnapshot.getData();
                            if(document != null){
                                eventData = new Event(document.get("name").toString());
                                if(document.get("date") != null) {
                                    eventData.date = (String)document.get("date");
                                }
                                if(document.get("localization") != null) {
                                    eventData.localization = (String)document.get("localization");
                                }
                                if(document.get("images") != null){
                                    eventData.images = (List<String>)document.get("images");
                                }
                                if(document.get("members") != null){
                                    eventData.members = (List<String>)document.get("members");
                                }
                            }
                        }
                    }
                });
    }

    private void saveData() {
        eventData.name = eventNameEditText.getText().toString();
        firestore.collection("events").document(eventId).set(eventData)
                .addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        Intent intent = new Intent();
                        intent.putExtra("id", eventId);
                        intent.putExtra("name", eventData.name);
                        setResult(Activity.RESULT_OK, intent);
                        finish();
                    }
                });
    }

    private void setViews() {
        eventNameEditText = findViewById(R.id.eventNameEditText);
        saveButton = findViewById(R.id.saveEventEditButton);
        cancelButton = findViewById(R.id.cancelEventEditButton);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveData();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if ((keyCode == KeyEvent.KEYCODE_BACK))
        {
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }
}