
package com.example.cashmxj;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
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

        View btnVolver = findViewById(R.id.btnVolver);
        View btnNuevaCuenta = findViewById(R.id.btnNuevaCuenta);

        contenedorCuentas = findViewById(R.id.contenedorCuentas);
        contenedorSinCuentas = findViewById(R.id.contenedorSinCuentas);
        tvSaldoCuentas = findViewById(R.id.tvSaldoCuentas);

        databaseHelper = new DatabaseHelper(this);

        btnVolver.setOnClickListener(v -> finish());

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
        cargarCuentas();
    }

    private void cargarCuentas() {

        List<Cuenta> cuentas = databaseHelper.obtenerCuentas();

        contenedorCuentas.removeAllViews();

        if (cuentas.isEmpty()) {

            contenedorSinCuentas.setVisibility(View.VISIBLE);

        } else {

            contenedorSinCuentas.setVisibility(View.GONE);

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

                vistaCuenta.setPadding(32, 24, 32, 24);

                LinearLayout.LayoutParams params =
                        new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                        );

                params.setMargins(0, 0, 0, 24);

                vistaCuenta.setLayoutParams(params);

                vistaCuenta.setClickable(true);
                vistaCuenta.setFocusable(true);

                vistaCuenta.setOnClickListener(v -> {
                    mostrarDetalleCuenta(cuenta);
                });

                contenedorCuentas.addView(vistaCuenta);
            }
        }

        long saldoTotal = databaseHelper.obtenerSaldoTotal();

        tvSaldoCuentas.setText(
                formatearDinero(saldoTotal)
        );
    }

    private void mostrarDetalleCuenta(Cuenta cuenta) {

        String descripcion = cuenta.getDescripcion();

        if (descripcion == null || descripcion.trim().isEmpty()) {
            descripcion = "Sin descripcion";
        }

        String informacion =
                "Nombre: " + cuenta.getNombre()
                        + "\n\nTipo: " + cuenta.getTipo()
                        + "\n\nSaldo: " + formatearDinero(cuenta.getSaldo())
                        + "\n\nDescripcion: " + descripcion;

        new AlertDialog.Builder(this)
                .setTitle("Detalle de la cuenta")
                .setMessage(informacion)

                .setNegativeButton("Cerrar", (dialog, which) -> {
                    dialog.dismiss();
                })

                .setNeutralButton("Eliminar", (dialog, which) -> {
                    confirmarEliminacion(cuenta);
                })

                .setPositiveButton("Editar", (dialog, which) -> {

                    Intent intent = new Intent(
                            CuentasActivity.this,
                            NuevaCuentaActivity.class
                    );

                    intent.putExtra(
                            "cuenta_id",
                            cuenta.getId()
                    );

                    startActivity(intent);
                })

                .show();
    }

    private void confirmarEliminacion(Cuenta cuenta) {

        new AlertDialog.Builder(this)
                .setTitle("Eliminar cuenta")
                .setMessage(
                        "¿Estas seguro de que deseas eliminar la cuenta \""
                                + cuenta.getNombre()
                                + "\"?\n\n"
                                + "Esta accion no se puede deshacer."
                )

                .setNegativeButton("Cancelar", (dialog, which) -> {
                    dialog.dismiss();
                })

                .setPositiveButton("Eliminar", (dialog, which) -> {
                    eliminarCuenta(cuenta);
                })

                .show();
    }

    private void eliminarCuenta(Cuenta cuenta) {

        int resultado = databaseHelper.eliminarCuenta(
                cuenta.getId()
        );

        if (resultado > 0) {

            Toast.makeText(
                    this,
                    "Cuenta eliminada correctamente",
                    Toast.LENGTH_SHORT
            ).show();

            cargarCuentas();

        } else {

            Toast.makeText(
                    this,
                    "No fue posible eliminar la cuenta",
                    Toast.LENGTH_SHORT
            ).show();
        }
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
