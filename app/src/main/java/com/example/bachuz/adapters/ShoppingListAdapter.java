package com.example.bachuz.adapters;

import android.app.Activity;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;

import com.example.bachuz.R;
import com.example.bachuz.activities.ShoppingCartActivity;
import com.example.bachuz.models.Product;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class ShoppingListAdapter extends BaseAdapter {

    private ArrayList<Product> products;
    private Context context;


    public ShoppingListAdapter(Context context, ArrayList<Product> products) {
        this.context = context;
        this.products = products;
    }


    @Override
    public int getCount() {
        return products.size();
    }

    @Override
    public Object getItem(int position) {
        return products.get(position);
    }

    @Override
    public long getItemId(int position) {
        return products.indexOf(getItem(position));
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater vi = LayoutInflater.from(context);
        View view = vi.inflate(R.layout.shopping_list_item, null);

        Product p = (Product) getItem(position);

        if (p != null) {
            ViewHolder holder = new ViewHolder();
            holder.productName = view.findViewById(R.id.itemNameText);
            holder.productCount = view.findViewById(R.id.numberOfItemsText);
            holder.delBtn = view.findViewById(R.id.removeButton);
            holder.productName.setText(products.get(position).productName);
            holder.productCount.setText(String.valueOf(products.get(position).productCount));
            holder.delBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((ShoppingCartActivity)context).deleteItem(position);
                }
            });
            holder.productName.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    products.get(position).productName = s.toString();
                }
            });

            holder.productCount.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    if(!s.toString().isEmpty()){
                        products.get(position).productCount = Integer.parseInt(s.toString());
                    }
                }
            });
        }

        return view;
    }

    private class ViewHolder{
        EditText productName;
        EditText productCount;
        ImageButton delBtn;
    }
}
