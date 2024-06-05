package com.example.bachuz.models;

import com.example.bachuz.R;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Event {

    public String name;
    public ArrayList<String> members;
    public String localization;
    public String date;
    public ArrayList<Product> products;


    public Event(String eventName) {
        name = eventName;
        members = new ArrayList<>();
        products = new ArrayList<>();
    }
}
