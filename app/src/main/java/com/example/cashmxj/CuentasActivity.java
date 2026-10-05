package com.example.cashmxj;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cashmxj.database.DatabaseHelper;
import com.example.cashmxj.models.Cuenta;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class CuentasActivity extends AppCompatActivity {

    private DatabaseHelper databaseHelper;

    private LinearLayout contenedorCuentas;
    private View contenedorSinCuentas;
    private TextView tvSaldoCuentas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_cuentas);

        // Referencias de la interfaz
        View btnVolver = findViewById(R.id.btnVolver);
        View btnNuevaCuenta = findViewById(R.id.btnNuevaCuenta);

        contenedorCuentas = findViewById(R.id.contenedorCuentas);
        contenedorSinCuentas = findViewById(R.id.contenedorSinCuentas);
        tvSaldoCuentas = findViewById(R.id.tvSaldoCuentas);

        // Conexión con SQLite
        databaseHelper = new DatabaseHelper(this);

        // Volver al Dashboard
        btnVolver.setOnClickListener(v -> finish());

        // Abrir formulario Nueva cuenta
        btnNuevaCuenta.setOnClickListener(v -> {

            Intent intent = new Intent(
                    CuentasActivity.this,
                    NuevaCuentaActivity.class
            );

            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Cada vez que volvemos a esta pantalla,
        // consultamos nuevamente la base de datos.
        cargarCuentas();
    }

    private void cargarCuentas() {

        // Obtener las cuentas almacenadas en SQLite
        List<Cuenta> cuentas = databaseHelper.obtenerCuentas();

        // Limpiar listado antes de volver a cargarlo
        contenedorCuentas.removeAllViews();

        if (cuentas.isEmpty()) {

            // Si no existen cuentas, mostrar mensaje
            contenedorSinCuentas.setVisibility(View.VISIBLE);

        } else {

            // Si existen cuentas, ocultar mensaje
            contenedorSinCuentas.setVisibility(View.GONE);

            // Crear visualmente una tarjeta por cada cuenta
            for (Cuenta cuenta : cuentas) {

                TextView vistaCuenta = new TextView(this);

                String informacion =
                        cuenta.getNombre()
                                + "\n"
                                + cuenta.getTipo()
                                + "\n"
                                + formatearDinero(cuenta.getSaldo());

                vistaCuenta.setText(informacion);

                vistaCuenta.setTextSize(17);

                vistaCuenta.setTextColor(
                        getColor(R.color.cash_text_primary)
                );

                vistaCuenta.setBackgroundResource(
                        R.drawable.bg_card
                );

                vistaCuenta.setPadding(
                        32,
                        24,
                        32,
                        24
                );

                LinearLayout.LayoutParams params =
                        new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                        );

                params.setMargins(
                        0,
                        0,
                        0,
                        24
                );

                vistaCuenta.setLayoutParams(params);

                // Agregar tarjeta al listado
                contenedorCuentas.addView(vistaCuenta);
            }
        }

        // Calcular saldo total de todas las cuentas
        long saldoTotal = databaseHelper.obtenerSaldoTotal();

        tvSaldoCuentas.setText(
                formatearDinero(saldoTotal)
        );
    }

    private String formatearDinero(long monto) {

        NumberFormat formato =
                NumberFormat.getCurrencyInstance(
                        new Locale("es", "CL")
                );

        // CLP sin decimales
        formato.setMaximumFractionDigits(0);

        return formato.format(monto);
    }
}