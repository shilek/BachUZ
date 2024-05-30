package com.example.bachuz.models;

import com.example.bachuz.R;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Event {

    public String name;
    public Collection<String> members;
    public String localization;
    public String date;
    public Collection<String> images;

    public Event(String eventName) {
        name = eventName;
        members = new ArrayList<>();
        images = new ArrayList<>();
    }
}
