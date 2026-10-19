package com.example.cashmxj;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.cashmxj.database.DatabaseHelper;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ComprasActivity extends AppCompatActivity {

    private DatabaseHelper databaseHelper;
    private CashMxJApplication aplicacion;

    private LinearLayout contenedorCompras;
    private TextView tvResumenCompras;

    private Button btnNuevaCompra;
    private Button btnVolverCompras;

    private final Locale localeChile = new Locale("es", "CL");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_compras);

        aplicacion = (CashMxJApplication) getApplication();
        databaseHelper = new DatabaseHelper(this);

        contenedorCompras = findViewById(R.id.contenedorCompras);
        tvResumenCompras = findViewById(R.id.tvResumenCompras);
        btnNuevaCompra = findViewById(R.id.btnNuevaCompra);
        btnVolverCompras = findViewById(R.id.btnVolverCompras);

        // VOLVER AL MENU ANTERIOR

        btnVolverCompras.setOnClickListener(v -> finish());

        // REGISTRAR NUEVA COMPRA

        btnNuevaCompra.setOnClickListener(v -> {

            if (aplicacion.requiereAutenticacion()) {
                return;
            }

            Intent intent = new Intent(
                    ComprasActivity.this,
                    NuevaCompraActivity.class
            );

            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!aplicacion.requiereAutenticacion()) {
            cargarCompras();
        }
    }

    // =====================================
    // CARGAR HISTORIAL DE COMPRAS
    // =====================================

    private void cargarCompras() {

        contenedorCompras.removeAllViews();

        List<String[]> compras = databaseHelper.obtenerCompras();

        tvResumenCompras.setText(
                "Compras registradas: " + compras.size()
        );

        if (compras.isEmpty()) {

            TextView tvSinCompras = new TextView(this);

            tvSinCompras.setText(
                    "Todavía no tienes compras registradas."
            );

            tvSinCompras.setTextSize(15);
            tvSinCompras.setTextColor(
                    Color.parseColor("#657085")
            );

            tvSinCompras.setGravity(Gravity.CENTER);

            tvSinCompras.setPadding(
                    dp(16),
                    dp(32),
                    dp(16),
                    dp(32)
            );

            contenedorCompras.addView(tvSinCompras);
            return;
        }

        for (String[] compra : compras) {
            agregarTarjetaCompra(compra);
        }
    }

    // =====================================
    // CREAR TARJETAS DE COMPRAS
    // =====================================

    private void agregarTarjetaCompra(String[] compra) {

        // [0] ID
        // [1] Descripción
        // [2] Monto
        // [3] Fecha
        // [4] ID cuenta
        // [5] ID contacto
        // [6] Estado
        // [7] ID movimiento
        // [8] Nombre cuenta
        // [9] Nombre contacto

        int compraId = Integer.parseInt(compra[0]);

        String descripcion = compra[1];
        long monto = Long.parseLong(compra[2]);
        long fecha = Long.parseLong(compra[3]);
        String estado = compra[6];
        String nombreCuenta = compra[8];
        String nombreContacto = compra[9];

        boolean programada =
                DatabaseHelper.COMPRA_PROGRAMADA.equals(estado);

        LinearLayout tarjeta = new LinearLayout(this);
        tarjeta.setOrientation(LinearLayout.VERTICAL);

        tarjeta.setPadding(
                dp(16),
                dp(16),
                dp(16),
                dp(16)
        );

        GradientDrawable fondo = new GradientDrawable();

        fondo.setColor(Color.WHITE);
        fondo.setCornerRadius(dp(14));

        fondo.setStroke(
                dp(1),
                Color.parseColor("#E3E8EF")
        );

        tarjeta.setBackground(fondo);
        tarjeta.setElevation(dp(2));

        LinearLayout.LayoutParams paramsTarjeta =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        paramsTarjeta.setMargins(
                0,
                dp(4),
                0,
                dp(14)
        );

        tarjeta.setLayoutParams(paramsTarjeta);

        TextView tvDescripcion = crearTexto(
                descripcion,
                18,
                "#172033",
                true
        );

        tarjeta.addView(tvDescripcion);

        TextView tvMonto = crearTexto(
                formatearMonto(monto),
                21,
                "#146C43",
                true
        );

        tvMonto.setPadding(
                0,
                dp(8),
                0,
                dp(8)
        );

        tarjeta.addView(tvMonto);

        TextView tvEstado = crearTexto(
                programada
                        ? "Estado: PROGRAMADA"
                        : "Estado: REALIZADA",
                13,
                programada ? "#A66300" : "#146C43",
                true
        );

        tarjeta.addView(tvEstado);

        tarjeta.addView(crearTexto(
                "Fecha: " + formatearFecha(fecha),
                14,
                "#657085",
                false
        ));

        tarjeta.addView(crearTexto(
                "Cuenta: " + nombreCuenta,
                14,
                "#657085",
                false
        ));

        if (nombreContacto != null
                && !nombreContacto.trim().isEmpty()) {

            tarjeta.addView(crearTexto(
                    "Contacto: " + nombreContacto,
                    14,
                    "#657085",
                    false
            ));
        }

        // ACCIONES DE COMPRAS PROGRAMADAS

        if (programada) {

            Button btnEditar = new Button(this);
            btnEditar.setText("Editar compra");
            btnEditar.setAllCaps(false);

            LinearLayout.LayoutParams paramsEditar =
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );

            paramsEditar.topMargin = dp(12);
            btnEditar.setLayoutParams(paramsEditar);

            btnEditar.setOnClickListener(v ->
                    editarCompra(compraId)
            );

            tarjeta.addView(btnEditar);

            Button btnConfirmar = new Button(this);
            btnConfirmar.setText("Marcar como realizada");
            btnConfirmar.setAllCaps(false);

            btnConfirmar.setOnClickListener(v ->
                    confirmarCompra(compraId, descripcion)
            );

            tarjeta.addView(btnConfirmar);

            Button btnEliminar = new Button(this);
            btnEliminar.setText("Eliminar compra programada");
            btnEliminar.setAllCaps(false);

            btnEliminar.setOnClickListener(v ->
                    eliminarCompra(compraId, descripcion)
            );

            tarjeta.addView(btnEliminar);
        }

        contenedorCompras.addView(tarjeta);
    }

    // =====================================
    // EDITAR COMPRA PROGRAMADA
    // =====================================

    private void editarCompra(int compraId) {

        if (aplicacion.requiereAutenticacion()) {
            return;
        }

        Intent intent = new Intent(
                ComprasActivity.this,
                NuevaCompraActivity.class
        );

        intent.putExtra(
                NuevaCompraActivity.EXTRA_COMPRA_ID,
                compraId
        );

        startActivity(intent);
    }

    // =====================================
    // CONFIRMAR COMPRA PROGRAMADA
    // =====================================

    private void confirmarCompra(
            int compraId,
            String descripcion) {

        if (aplicacion.requiereAutenticacion()) {
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Confirmar compra")
                .setMessage(
                        "¿Confirmas que realizaste la compra \""
                                + descripcion
                                + "\"?\n\n"
                                + "Se descontará el monto de la cuenta "
                                + "asociada y se registrará un gasto."
                )
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Confirmar", (dialog, which) -> {

                    if (aplicacion.requiereAutenticacion()) {
                        return;
                    }

                    boolean resultado =
                            databaseHelper.confirmarCompraProgramada(
                                    compraId
                            );

                    if (resultado) {

                        Toast.makeText(
                                this,
                                "Compra realizada correctamente",
                                Toast.LENGTH_SHORT
                        ).show();

                    } else {

                        Toast.makeText(
                                this,
                                "No se pudo confirmar la compra. "
                                        + "Verifica el saldo disponible.",
                                Toast.LENGTH_LONG
                        ).show();
                    }

                    cargarCompras();
                })
                .show();
    }

    // =====================================
    // ELIMINAR COMPRA PROGRAMADA
    // =====================================

    private void eliminarCompra(
            int compraId,
            String descripcion) {

        if (aplicacion.requiereAutenticacion()) {
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Eliminar compra programada")
                .setMessage(
                        "¿Deseas eliminar la compra \""
                                + descripcion
                                + "\"?\n\n"
                                + "Esta acción no modifica el saldo "
                                + "de ninguna cuenta."
                )
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Eliminar", (dialog, which) -> {

                    if (aplicacion.requiereAutenticacion()) {
                        return;
                    }

                    int resultado =
                            databaseHelper.eliminarCompraProgramada(
                                    compraId
                            );

                    if (resultado > 0) {

                        Toast.makeText(
                                this,
                                "Compra programada eliminada",
                                Toast.LENGTH_SHORT
                        ).show();

                    } else {

                        Toast.makeText(
                                this,
                                "No se pudo eliminar la compra",
                                Toast.LENGTH_SHORT
                        ).show();
                    }

                    cargarCompras();
                })
                .show();
    }

    // =====================================
    // CREAR TEXTOS
    // =====================================

    private TextView crearTexto(
            String contenido,
            int tamano,
            String color,
            boolean negrita) {

        TextView texto = new TextView(this);

        texto.setText(contenido);
        texto.setTextSize(tamano);
        texto.setTextColor(Color.parseColor(color));

        if (negrita) {
            texto.setTypeface(null, Typeface.BOLD);
        }

        texto.setPadding(
                0,
                dp(3),
                0,
                dp(3)
        );

        return texto;
    }

    // =====================================
    // FORMATEAR MONTO
    // =====================================

    private String formatearMonto(long monto) {

        NumberFormat formato =
                NumberFormat.getCurrencyInstance(localeChile);

        formato.setMaximumFractionDigits(0);
        formato.setMinimumFractionDigits(0);

        return formato.format(monto);
    }

    // =====================================
    // FORMATEAR FECHA
    // =====================================

    private String formatearFecha(long fecha) {

        SimpleDateFormat formato =
                new SimpleDateFormat(
                        "dd/MM/yyyy",
                        localeChile
                );

        return formato.format(new Date(fecha));
    }

    // =====================================
    // CONVERTIR DP
    // =====================================

    private int dp(int valor) {

        return Math.round(
                valor * getResources()
                        .getDisplayMetrics()
                        .density
        );
    }

    // =====================================
    // CERRAR BASE DE DATOS
    // =====================================

    @Override
    protected void onDestroy() {

        if (databaseHelper != null) {
            databaseHelper.close();
        }

        super.onDestroy();
    }
}