package com.example.cashmxj;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Carga la interfaz principal de Cash MxJ
        setContentView(R.layout.activity_main);
    }
}