
package com.example.cashmxj;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cashmxj.database.DatabaseHelper;
import com.example.cashmxj.models.Cuenta;
import com.example.cashmxj.models.Movimiento;

import java.util.ArrayList;
import java.util.List;

public class NuevoMovimientoActivity extends AppCompatActivity {

    private DatabaseHelper databaseHelper;

    private RadioGroup rgTipoMovimiento;
    private RadioButton rbIngreso;
    private Spinner spCuentaMovimiento;
    private Spinner spCategoriaMovimiento;
    private EditText etMontoMovimiento;
    private EditText etDescripcionMovimiento;
    private Button btnGuardarMovimiento;

    private List<Cuenta> cuentas;

    private final String[] categoriasIngreso = {
            "Sueldo",
            "Honorarios",
            "Ventas",
            "Transferencias recibidas",
            "Regalos",
            "Intereses",
            "Otros ingresos"
    };

    private final String[] categoriasGasto = {
            "Alimentación",
            "Transporte",
            "Vivienda",
            "Servicios básicos",
            "Salud",
            "Educación",
            "Entretenimiento",
            "Compras",
            "Mascotas",
            "Otros gastos"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_nuevo_movimiento);

        databaseHelper = new DatabaseHelper(this);

        View btnVolver = findViewById(
                R.id.btnVolverNuevoMovimiento
        );

        rgTipoMovimiento = findViewById(
                R.id.rgTipoMovimiento
        );

        rbIngreso = findViewById(R.id.rbIngreso);

        spCuentaMovimiento = findViewById(
                R.id.spCuentaMovimiento
        );

        spCategoriaMovimiento = findViewById(
                R.id.spCategoriaMovimiento
        );

        etMontoMovimiento = findViewById(
                R.id.etMontoMovimiento
        );

        etDescripcionMovimiento = findViewById(
                R.id.etDescripcionMovimiento
        );

        btnGuardarMovimiento = findViewById(
                R.id.btnGuardarMovimiento
        );

        btnVolver.setOnClickListener(v -> finish());

        cargarCuentas();

        actualizarCategorias();

        rgTipoMovimiento.setOnCheckedChangeListener(
                (group, checkedId) -> actualizarCategorias()
        );

        btnGuardarMovimiento.setOnClickListener(
                v -> guardarMovimiento()
        );
    }

    private void cargarCuentas() {

        cuentas = databaseHelper.obtenerCuentas();

        List<String> nombresCuentas = new ArrayList<>();

        for (Cuenta cuenta : cuentas) {
            nombresCuentas.add(cuenta.getNombre());
        }

        if (cuentas.isEmpty()) {
            nombresCuentas.add("No tienes cuentas registradas");
            btnGuardarMovimiento.setEnabled(false);

            Toast.makeText(
                    this,
                    "Primero debes crear una cuenta",
                    Toast.LENGTH_LONG
            ).show();
        }

        ArrayAdapter<String> adaptador = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                nombresCuentas
        );

        adaptador.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spCuentaMovimiento.setAdapter(adaptador);
    }

    private void actualizarCategorias() {

        boolean esIngreso = rbIngreso.isChecked();

        String[] categorias = esIngreso
                ? categoriasIngreso
                : categoriasGasto;

        ArrayAdapter<String> adaptador = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                categorias
        );

        adaptador.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spCategoriaMovimiento.setAdapter(adaptador);
    }

    private void guardarMovimiento() {

        if (cuentas == null || cuentas.isEmpty()) {

            Toast.makeText(
                    this,
                    "Debes crear una cuenta primero",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        String textoMonto = etMontoMovimiento
                .getText()
                .toString()
                .trim();

        if (textoMonto.isEmpty()) {

            etMontoMovimiento.setError(
                    "Ingresa el monto"
            );

            etMontoMovimiento.requestFocus();
            return;
        }

        long monto;

        try {
            monto = Long.parseLong(textoMonto);
        } catch (NumberFormatException e) {

            etMontoMovimiento.setError(
                    "Ingresa un monto válido"
            );

            etMontoMovimiento.requestFocus();
            return;
        }

        if (monto <= 0) {

            etMontoMovimiento.setError(
                    "El monto debe ser mayor que cero"
            );

            etMontoMovimiento.requestFocus();
            return;
        }

        int posicionCuenta = spCuentaMovimiento
                .getSelectedItemPosition();

        if (posicionCuenta < 0 ||
                posicionCuenta >= cuentas.size()) {

            Toast.makeText(
                    this,
                    "Selecciona una cuenta",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        Cuenta cuenta = cuentas.get(posicionCuenta);

        String tipo = rbIngreso.isChecked()
                ? "Ingreso"
                : "Gasto";

        String categoria = spCategoriaMovimiento
                .getSelectedItem()
                .toString();

        String descripcion = etDescripcionMovimiento
                .getText()
                .toString()
                .trim();

        // Verificar saldo disponible antes de guardar.
        Cuenta cuentaActualizada = databaseHelper
                .obtenerCuentaPorId(cuenta.getId());

        if (cuentaActualizada == null) {

            Toast.makeText(
                    this,
                    "La cuenta seleccionada ya no existe",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        if ("Gasto".equals(tipo) &&
                monto > cuentaActualizada.getSaldo()) {

            etMontoMovimiento.setError(
                    "Saldo insuficiente"
            );

            etMontoMovimiento.requestFocus();
            return;
        }

        Movimiento movimiento = new Movimiento(
                0,
                cuenta.getId(),
                tipo,
                monto,
                categoria,
                descripcion,
                System.currentTimeMillis()
        );

        long resultado = databaseHelper.insertarMovimiento(
                movimiento
        );

        if (resultado > 0) {

            Toast.makeText(
                    this,
                    "Movimiento guardado correctamente",
                    Toast.LENGTH_SHORT
            ).show();

            finish();

        } else {

            Toast.makeText(
                    this,
                    "No fue posible guardar el movimiento",
                    Toast.LENGTH_LONG
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
