package com.example.cashmxj;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        View btnCuentas = findViewById(R.id.btnCuentas);

        btnCuentas.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CuentasActivity.class);
            startActivity(intent);
        });
    }
}