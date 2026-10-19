
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

    private int cuentaId = -1;
    private boolean modoEdicion = false;

    private final String[] tiposCuenta = {
            "Efectivo",
            "Cuenta bancaria",
            "Tarjeta",
            "Ahorros",
            "Otro"
    };

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

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                tiposCuenta
        );

        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spTipoCuenta.setAdapter(adapter);

        cuentaId = getIntent().getIntExtra("cuenta_id", -1);
        modoEdicion = cuentaId != -1;

        if (modoEdicion) {
            cargarCuentaParaEditar();
        }

        btnVolver.setOnClickListener(v -> finish());

        btnGuardarCuenta.setOnClickListener(v -> guardarCuenta());
    }

    private void cargarCuentaParaEditar() {

        Cuenta cuenta = databaseHelper.obtenerCuentaPorId(cuentaId);

        if (cuenta == null) {
            Toast.makeText(
                    this,
                    "No se encontro la cuenta",
                    Toast.LENGTH_SHORT
            ).show();
            finish();
            return;
        }

        etNombreCuenta.setText(cuenta.getNombre());
        etSaldoInicial.setText(String.valueOf(cuenta.getSaldo()));
        etDescripcionCuenta.setText(cuenta.getDescripcion());

        for (int i = 0; i < tiposCuenta.length; i++) {
            if (tiposCuenta[i].equals(cuenta.getTipo())) {
                spTipoCuenta.setSelection(i);
                break;
            }
        }
    }

    private void guardarCuenta() {

        String nombre = etNombreCuenta.getText().toString().trim();
        String saldoTexto = etSaldoInicial.getText().toString().trim();
        String descripcion = etDescripcionCuenta.getText().toString().trim();
        String tipo = spTipoCuenta.getSelectedItem().toString();

        if (nombre.isEmpty()) {
            etNombreCuenta.setError("Ingresa un nombre");
            etNombreCuenta.requestFocus();
            return;
        }

        if (saldoTexto.isEmpty()) {
            etSaldoInicial.setError("Ingresa el saldo");
            etSaldoInicial.requestFocus();
            return;
        }

        long saldo;

        try {
            saldo = Long.parseLong(saldoTexto);
        } catch (NumberFormatException e) {
            etSaldoInicial.setError("Ingresa un monto valido");
            etSaldoInicial.requestFocus();
            return;
        }

        if (saldo < 0) {
            etSaldoInicial.setError("El saldo no puede ser negativo");
            etSaldoInicial.requestFocus();
            return;
        }

        Cuenta cuenta = new Cuenta(
                modoEdicion ? cuentaId : 0,
                nombre,
                tipo,
                saldo,
                descripcion
        );

        boolean guardado;

        if (modoEdicion) {
            guardado = databaseHelper.actualizarCuenta(cuenta) > 0;
        } else {
            guardado = databaseHelper.insertarCuenta(cuenta) != -1;
        }

        if (guardado) {

            Toast.makeText(
                    this,
                    modoEdicion
                            ? "Cuenta actualizada correctamente"
                            : "Cuenta guardada correctamente",
                    Toast.LENGTH_SHORT
            ).show();

            setResult(RESULT_OK);
            finish();

        } else {

            Toast.makeText(
                    this,
                    "No fue posible guardar los cambios",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    @Override
    protected void onDestroy() {
        if (databaseHelper != null) {
            databaseHelper.close();
        }
        super.onDestroy();
    }
}
