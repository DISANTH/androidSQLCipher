package com.example.securedatabasenkioskmode;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.securedatabasenkioskmode.Database.DBConnection;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        DBConnection dbConnection = DBConnection.get();
        try {
            dbConnection.openDatabase(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}