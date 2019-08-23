package com.hfad.social_network_app;

import com.google.firebase.database.FirebaseDatabase;

public class Friends
{
    public String date;
    public Friends()
    {

    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Friends(String date) {
        this.date = date;
    }
}
