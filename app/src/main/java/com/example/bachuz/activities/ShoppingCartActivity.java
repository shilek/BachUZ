package com.example.bachuz.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.bachuz.R;
import com.example.bachuz.adapters.ShoppingListAdapter;
import com.example.bachuz.models.KeyValue;
import com.example.bachuz.models.Product;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.common.hash.Hashing;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ShoppingCartActivity extends AppCompatActivity {

    ListView productsListView;
    FloatingActionButton addButton;
    FloatingActionButton cancelButton;
    FloatingActionButton saveButton;
    ArrayList<Product> products;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_shopping_cart);
        productsListView = findViewById(R.id.shoppingItemsListView);
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        ArrayList<String> names = extras.getStringArrayList("productNames");
        ArrayList<Integer> counts = extras.getIntegerArrayList("productCounts");
        products = new ArrayList<Product>();
        if(names != null){
            for(int i=0; i < names.size(); i++) {
                products.add(new Product(names.get(i), counts.get(i)));
            }
        }
        setListView();
        setViews();
    }

    private void setViews(){
        addButton = findViewById(R.id.addShoppingCartItemButton);
        saveButton = findViewById(R.id.saveShoppingCartEditButton);
        cancelButton = findViewById(R.id.cancelShoppingCartEditButton);

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                products.add(new Product(getString(R.string.new_shopping_cart_item), 0));
                setListView();
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                ArrayList<String> names = new ArrayList<>();
                ArrayList<Integer> counts = new ArrayList<>();
                for(int i=0; i < products.size(); i++){
                    names.add(products.get(i).productName);
                    counts.add(products.get(i).productCount);
                }
                intent.putStringArrayListExtra("productNames", names);
                intent.putIntegerArrayListExtra("productCounts", counts);
                setResult(Activity.RESULT_OK, intent);
                finish();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void setListView(){
        ShoppingListAdapter adapter = new ShoppingListAdapter(this, products);
        productsListView.setAdapter(adapter);
    }

    public void deleteItem(int index){
        products.remove(index);
        setListView();
    }
}