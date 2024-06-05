package com.example.bachuz.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.app.DatePickerDialog;
import android.widget.DatePicker;
import android.location.Location;


import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.bachuz.R;
import com.example.bachuz.models.Event;
import com.example.bachuz.models.KeyValue;
import com.example.bachuz.models.Product;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;


public class EventActivity extends AppCompatActivity implements SensorEventListener {

    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    private EditText eventNameEditText;
    private FloatingActionButton saveButton;
    private FloatingActionButton cancelButton;
    private ImageButton goToShoppingListButton;
    private Event eventData;
    private String eventId;
    private SensorManager sensorManager;
    private Sensor stepCounterSensor;
    private boolean isCountingSteps = false;
    private int stepCount = 0;
    private TextView stepCountTextView;

    private TextView chosenDate;

    private Button dateEditButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_event);
        setViews();
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        //chosenDate = findViewById(R.id.eventDateTextView);
        //dateEditButton = findViewById(R.id.chooseEventDateButton);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            eventNameEditText.setText(extras.getString("name"));
            chosenDate.setText(extras.getString("date"));
            eventId = extras.getString("id");
            getEventData();

        }

        dateEditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDatePicker();
            }
        });
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
                                if(document.get("products") != null){
                                    eventData.products = mapToProductsArray(((ArrayList<HashMap<Object, Object>>)document.get("products")));
                                }
                                if(document.get("members") != null){
                                    eventData.members = (ArrayList<String>)document.get("members");
                                }
                                startStepCounting();
                            }
                        }
                    }
                });
    }

    private void saveData() {
        eventData.name = eventNameEditText.getText().toString();
        eventData.date = chosenDate.getText().toString();
        firestore.collection("events").document(eventId).set(eventData)
                .addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        Intent intent = new Intent();
                        intent.putExtra("id", eventId);
                        intent.putExtra("name", eventData.name);
                        intent.putExtra("date", eventData.date);
                        setResult(Activity.RESULT_OK, intent);
                        finish();
                    }
                });
    }

    private void setViews() {
        eventNameEditText = findViewById(R.id.eventNameEditText);
        saveButton = findViewById(R.id.saveEventEditButton);
        cancelButton = findViewById(R.id.cancelEventEditButton);
        stepCountTextView = findViewById(R.id.stepCountTextView);
        chosenDate = findViewById(R.id.eventDateTextView);
        dateEditButton = findViewById(R.id.chooseEventDateButton);
        goToShoppingListButton = findViewById(R.id.goToShoppingListButton);

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
       setShoppingCartButton();
    }

    private void openDatePicker(){
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, R.style.Theme_BachUZ , new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {

                //Showing the picked value in the textView
                chosenDate.setText(String.valueOf(year)+ "-"+String.valueOf(month)+ "-"+String.valueOf(day));

            }
        }, 2023, 06, 4);

        datePickerDialog.show();
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
//to be completed later -
// as the step counter for events should start once the party starts -
// and the feature for calendar is not implemented for now.
    @Override
    protected void onResume() {
        super.onResume();
        if (isCountingSteps) {
            sensorManager.registerListener(this, stepCounterSensor, SensorManager.SENSOR_DELAY_UI);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isCountingSteps) {
            sensorManager.unregisterListener(this);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
            stepCount = (int) event.values[0];
            stepCountTextView.setText("Steps: " + stepCount);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do something here if sensor accuracy changes.
    }

    private void startStepCounting() {
        isCountingSteps = true;
        sensorManager.registerListener(this, stepCounterSensor, SensorManager.SENSOR_DELAY_UI);
    }

    ActivityResultLauncher<Intent> someActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent intent = result.getData();
                        if(intent != null){
                            Bundle extras = intent.getExtras();
                            if(extras != null) {
                                ArrayList<String> names = extras.getStringArrayList("productNames");
                                ArrayList<Integer> counts = extras.getIntegerArrayList("productCounts");
                                ArrayList<Product> productList = new ArrayList<Product>();
                                if(names != null){
                                    for(int i=0; i < names.size(); i++) {
                                        productList.add(new Product(names.get(i), counts.get(i)));
                                    }
                                }
                                eventData.products = productList;
                                setShoppingCartButton();
                            }
                        }
                    }
                }
            });

    private void setShoppingCartButton(){
        goToShoppingListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), ShoppingCartActivity.class);
                ArrayList<String> names = new ArrayList<>();
                ArrayList<Integer> counts = new ArrayList<>();
                for(int i=0; i < eventData.products.size(); i++){
                    names.add(eventData.products.get(i).productName);
                    counts.add(eventData.products.get(i).productCount);
                }
                intent.putStringArrayListExtra("productNames", names);
                intent.putIntegerArrayListExtra("productCounts", counts);
                someActivityResultLauncher.launch(intent);
            }
        });
    }

    private ArrayList<Product> mapToProductsArray(ArrayList<HashMap<Object, Object>> array){
        ArrayList<Product> products = new ArrayList<Product>();
        for(int i=0; i < array.size(); i++) {
            products.add(new Product((String)array.get(i).get("productName"), Math.toIntExact((Long)array.get(i).get("productCount"))));
        }
        return products;
    }
}