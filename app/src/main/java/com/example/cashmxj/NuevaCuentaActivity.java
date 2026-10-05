package com.example.cashmxj;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cashmxj.database.DatabaseHelper;
import com.example.cashmxj.models.Cuenta;

public class NuevaCuentaActivity extends AppCompatActivity {

    private EditText etNombreCuenta;
    private EditText etSaldoInicial;
    private EditText etDescripcionCuenta;
    private Spinner spTipoCuenta;

    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_nueva_cuenta);

        View btnVolver = findViewById(R.id.btnVolver);
        View btnGuardarCuenta = findViewById(R.id.btnGuardarCuenta);

        etNombreCuenta = findViewById(R.id.etNombreCuenta);
        etSaldoInicial = findViewById(R.id.etSaldoInicial);
        etDescripcionCuenta = findViewById(R.id.etDescripcionCuenta);
        spTipoCuenta = findViewById(R.id.spTipoCuenta);

        databaseHelper = new DatabaseHelper(this);

        String[] tiposCuenta = {
                "Efectivo",
                "Cuenta bancaria",
                "Tarjeta",
                "Ahorros",
                "Otro"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                tiposCuenta
        );

        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spTipoCuenta.setAdapter(adapter);

        btnVolver.setOnClickListener(v -> finish());

        btnGuardarCuenta.setOnClickListener(v -> guardarCuenta());
    }

    private void guardarCuenta() {

        String nombre = etNombreCuenta.getText().toString().trim();
        String saldoTexto = etSaldoInicial.getText().toString().trim();
        String descripcion = etDescripcionCuenta.getText().toString().trim();
        String tipo = spTipoCuenta.getSelectedItem().toString();

        // Validar nombre
        if (nombre.isEmpty()) {
            etNombreCuenta.setError("Ingresa un nombre");
            etNombreCuenta.requestFocus();
            return;
        }

        // Validar saldo
        if (saldoTexto.isEmpty()) {
            etSaldoInicial.setError("Ingresa el saldo inicial");
            etSaldoInicial.requestFocus();
            return;
        }

        long saldo;

        try {
            saldo = Long.parseLong(saldoTexto);
        } catch (NumberFormatException e) {
            etSaldoInicial.setError("Ingresa un monto válido");
            return;
        }

        // Crear objeto Cuenta
        Cuenta cuenta = new Cuenta(
                0,
                nombre,
                tipo,
                saldo,
                descripcion
        );

        // Guardar en SQLite
        long resultado = databaseHelper.insertarCuenta(cuenta);

        if (resultado != -1) {

            Toast.makeText(
                    this,
                    "Cuenta guardada correctamente",
                    Toast.LENGTH_SHORT
            ).show();

            finish();

        } else {

            Toast.makeText(
                    this,
                    "No fue posible guardar la cuenta",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }
}