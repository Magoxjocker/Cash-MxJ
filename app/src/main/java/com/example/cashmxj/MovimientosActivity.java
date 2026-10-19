
package com.example.cashmxj;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cashmxj.database.DatabaseHelper;
import com.example.cashmxj.models.Cuenta;
import com.example.cashmxj.models.Movimiento;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MovimientosActivity extends AppCompatActivity {

    private DatabaseHelper databaseHelper;

    private LinearLayout contenedorMovimientos;
    private TextView tvSinMovimientos;
    private TextView tvTotalIngresos;
    private TextView tvTotalGastos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_movimientos);

        View btnVolver = findViewById(
                R.id.btnVolverMovimientos
        );

        View btnNuevoMovimiento = findViewById(
                R.id.btnNuevoMovimiento
        );

        contenedorMovimientos = findViewById(
                R.id.contenedorMovimientos
        );

        tvSinMovimientos = findViewById(
                R.id.tvSinMovimientos
        );

        tvTotalIngresos = findViewById(
                R.id.tvTotalIngresos
        );

        tvTotalGastos = findViewById(
                R.id.tvTotalGastos
        );

        databaseHelper = new DatabaseHelper(this);

        btnVolver.setOnClickListener(v -> finish());

        btnNuevoMovimiento.setOnClickListener(v -> {

            Intent intent = new Intent(
                    MovimientosActivity.this,
                    NuevoMovimientoActivity.class
            );

            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarMovimientos();
    }

    private void cargarMovimientos() {

        List<Movimiento> movimientos =
                databaseHelper.obtenerMovimientos();

        contenedorMovimientos.removeAllViews();

        long ingresos = 0;
        long gastos = 0;

        if (movimientos.isEmpty()) {
            tvSinMovimientos.setVisibility(View.VISIBLE);
        } else {
            tvSinMovimientos.setVisibility(View.GONE);
        }

        SimpleDateFormat formatoFecha =
                new SimpleDateFormat(
                        "dd/MM/yyyy",
                        new Locale("es", "CL")
                );

        for (Movimiento movimiento : movimientos) {

            boolean esIngreso =
                    "Ingreso".equals(movimiento.getTipo());

            if (esIngreso) {
                ingresos += movimiento.getMonto();
            } else {
                gastos += movimiento.getMonto();
            }

            Cuenta cuenta = databaseHelper.obtenerCuentaPorId(
                    movimiento.getCuentaId()
            );

            String nombreCuenta = cuenta != null
                    ? cuenta.getNombre()
                    : "Cuenta no disponible";

            String signo = esIngreso ? "+ " : "- ";

            String fecha = formatoFecha.format(
                    new Date(movimiento.getFecha())
            );

            TextView vistaMovimiento = new TextView(this);

            String informacion =
                    movimiento.getCategoria()
                            + "\n"
                            + nombreCuenta + " · " + movimiento.getTipo()
                            + "\n"
                            + fecha
                            + "\n"
                            + signo + formatearDinero(movimiento.getMonto());

            vistaMovimiento.setText(informacion);
            vistaMovimiento.setTextSize(16);

            vistaMovimiento.setTextColor(
                    getColor(R.color.cash_text_primary)
            );

            vistaMovimiento.setBackgroundResource(
                    R.drawable.bg_card
            );

            vistaMovimiento.setPadding(32, 24, 32, 24);

            LinearLayout.LayoutParams params =
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );

            params.setMargins(0, 0, 0, 24);

            vistaMovimiento.setLayoutParams(params);

            contenedorMovimientos.addView(vistaMovimiento);
        }

        tvTotalIngresos.setText(
                formatearDinero(ingresos)
        );

        tvTotalGastos.setText(
                formatearDinero(gastos)
        );
    }

    private String formatearDinero(long monto) {

        NumberFormat formato =
                NumberFormat.getCurrencyInstance(
                        new Locale("es", "CL")
                );

        formato.setMaximumFractionDigits(0);
        formato.setMinimumFractionDigits(0);

        return formato.format(monto);
    }

    @Override
    protected void onDestroy() {

        if (databaseHelper != null) {
            databaseHelper.close();
        }

        super.onDestroy();
    }
}
