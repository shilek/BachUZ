package com.example.bachuz.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.example.bachuz.models.Event;
import com.example.bachuz.models.KeyValue;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.bachuz.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.common.hash.Hashing;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.SortedMap;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    private AdView adView;
    private String email;
    private ArrayList<KeyValue> events = new ArrayList<KeyValue>();

    private TextView toolbarUserName;
    private ListView eventsList;
    private FloatingActionButton addNewEventButton;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        setViews();
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            toolbarUserName.setText(extras.getString("username"));
            email = extras.getString("email");
        }
        AdView mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        getUserEvents();

    }

    private void getUserEvents() {
        db.collection("events").whereArrayContains("members", email).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            QuerySnapshot querySnapshot = task.getResult();
                            List<DocumentSnapshot> documents = querySnapshot.getDocuments();
                            for(int i = 0; i < documents.size(); i++) {
                                events.add(new KeyValue(documents.get(i).getId(), documents.get(i).getString("name")));
                            }
                            if(!events.isEmpty()) {
                                setListView();
                            }
                        }
                    }
                });
    }

    private void addEvent() {
        String uid = UUID.randomUUID().toString();
        Event event = new Event(getString(R.string.new_event_name));
        event.members.add(email);
        db.collection("events").document(uid).set(event)
                .addOnSuccessListener(new OnSuccessListener() {
                    @Override
                    public void onSuccess(Object o) {
                        events.add(new KeyValue(uid, event.name));
                        setListView();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("MAIN_ACTIVITY", e.getMessage());
                    }
                });
    }

    private void setViews() {
        toolbarUserName = findViewById(R.id.toolbarUserName);
        addNewEventButton = findViewById(R.id.addNewEventButton);
        eventsList = findViewById(R.id.eventsListView);
        addNewEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addEvent();
            }
        });
    }

    private ArrayList<String> getEventNames() {
        ArrayList<String> values = new ArrayList<String>();
        for(int i=0; i < events.size(); i++) {
            values.add(events.get(i).Value);
        }
        return values;
    }

    private void setListView(){
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.event_list_item, getEventNames());
        eventsList.setAdapter(adapter);
        eventsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                String eventId = events.get(position).Key;
                String eventName = events.get(position).Value;

                Intent i = new Intent(getApplicationContext(), EventActivity.class);
                i.putExtra("id", eventId);
                i.putExtra("name", eventName);
                someActivityResultLauncher.launch(i);
            }
        });
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    ActivityResultLauncher<Intent> someActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if(data != null) {
                            Bundle extras = data.getExtras();
                            String name = extras.getString("name");
                            String id = extras.getString("id");
                            for(int i = 0; i < events.size(); i++){
                                if(events.get(i).Key.equals(id)){
                                    events.set(i, new KeyValue(id, name));
                                    setListView();
                                    break;
                                }
                            }
                        }
                    } else if (result.getResultCode() == 2) {
                        Intent data = result.getData();
                        if (data != null) {
                            Bundle extras = data.getExtras();
                            String id = extras.getString("id");
                            removeEvent(id);
                        }
                    } else if (result.getResultCode() == 3) {
                        Intent data = result.getData();
                        if (data != null) {
                            Bundle extras = data.getExtras();
                            String id = extras.getString("id");
                            leaveEvent(id);
                        }
                    }
                }
            });

    private void removeEvent(String eventId){
        for(int i=0; i < events.size(); i++) {
            if(events.get(i).Key.equals(eventId)) {
                events.remove(i);
                db.collection("events").document(eventId).delete()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                setListView();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                            }
                        });
                break;
            }
        }
    }

    private void leaveEvent(String eventId){
        for(int i=0; i < events.size(); i++) {
            if(events.get(i).Key.equals(eventId)) {
                events.remove(i);
                db.collection("events").document(eventId).get()
                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                Map<String, Object> document = documentSnapshot.getData();
                                if(document != null) {
                                    if(document.get("members") != null){
                                        ArrayList<String> eventMembers = (ArrayList<String>)document.get("members");
                                        for(int i=0; i < eventMembers.size(); i++){
                                            if(eventMembers.get(i).equals(email)){
                                                eventMembers.remove(i);
                                                db.collection("events").document(eventId).update("members", eventMembers);
                                                setListView();
                                                break;
                                            }
                                        }
                                    }
                                }
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                            }
                        });
                break;
            }
        }
    }
}