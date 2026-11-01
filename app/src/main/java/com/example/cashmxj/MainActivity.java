
package com.example.cashmxj;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import com.example.cashmxj.database.DatabaseHelper;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.concurrent.Executor;

public class MainActivity extends AppCompatActivity {

    private DatabaseHelper databaseHelper;
    private CashMxJApplication aplicacion;

    private TextView tvSaldoTotal;
    private View contenidoPrincipal;

    private boolean autenticacionEnCurso = false;

    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        aplicacion = (CashMxJApplication) getApplication();
        databaseHelper = new DatabaseHelper(this);

        contenidoPrincipal = findViewById(android.R.id.content);
        tvSaldoTotal = findViewById(R.id.tvSaldoTotal);

        View btnCuentas = findViewById(R.id.btnCuentas);
        View btnMovimientos = findViewById(R.id.btnMovimientos);
        View btnContactos = findViewById(R.id.btnContactos);
        View btnCompras = findViewById(R.id.btnCompras);

        // NUEVO: botón de compras compartidas
        View btnCompartidos = findViewById(R.id.btnCompartidos);

        // =====================================
        // CUENTAS
        // =====================================

        btnCuentas.setOnClickListener(v -> {
            if (aplicacion.requiereAutenticacion()) return;

            startActivity(
                    new Intent(this, CuentasActivity.class)
            );
        });

        // =====================================
        // MOVIMIENTOS
        // =====================================

        btnMovimientos.setOnClickListener(v -> {
            if (aplicacion.requiereAutenticacion()) return;

            startActivity(
                    new Intent(this, MovimientosActivity.class)
            );
        });

        // =====================================
        // CONTACTOS
        // =====================================

        btnContactos.setOnClickListener(v -> {
            if (aplicacion.requiereAutenticacion()) return;

            startActivity(
                    new Intent(this, ContactosActivity.class)
            );
        });

        // =====================================
        // COMPRAS
        // =====================================

        btnCompras.setOnClickListener(v -> {
            if (aplicacion.requiereAutenticacion()) return;

            startActivity(
                    new Intent(this, ComprasActivity.class)
            );
        });

        // =====================================
        // COMPRAS COMPARTIDAS
        // =====================================

        btnCompartidos.setOnClickListener(v -> {
            if (aplicacion.requiereAutenticacion()) return;

            startActivity(
                    new Intent(this, CompartidasActivity.class)
            );
        });

        // MODULO 4: MAPA DE LUGARES
        View btnMapa = findViewById(R.id.btnMapa);
        btnMapa.setOnClickListener(v -> {
            if (aplicacion.requiereAutenticacion()) return;
            startActivity(new Intent(this, MapaActivity.class));
        });

        // =====================================
        // AUTENTICACIÓN
        // =====================================


        if (aplicacion.requiereAutenticacion()) {
            contenidoPrincipal.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (aplicacion.requiereAutenticacion()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
        actualizarSaldoTotal();
    }

    private void actualizarSaldoTotal() {

        if (aplicacion.requiereAutenticacion()) return;

        long saldoTotal = databaseHelper.obtenerSaldoTotal();

        NumberFormat formato =
                NumberFormat.getCurrencyInstance(
                        new Locale("es", "CL")
                );

        formato.setMaximumFractionDigits(0);
        formato.setMinimumFractionDigits(0);

        tvSaldoTotal.setText(
                formato.format(saldoTotal)
        );
    }

    @Override
    protected void onDestroy() {

        if (databaseHelper != null) {
            databaseHelper.close();
        }

        super.onDestroy();
    }
}
