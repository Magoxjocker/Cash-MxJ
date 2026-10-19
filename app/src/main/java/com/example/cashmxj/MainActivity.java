
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
    private TextView tvSaldoTotal;

    private View contenidoPrincipal;

    private boolean autenticado = false;
    private boolean autenticacionEnCurso = false;

    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        databaseHelper = new DatabaseHelper(this);

        contenidoPrincipal = findViewById(android.R.id.content);
        contenidoPrincipal.setVisibility(View.INVISIBLE);

        View btnCuentas = findViewById(R.id.btnCuentas);
        View btnMovimientos = findViewById(R.id.btnMovimientos);
        View btnContactos = findViewById(R.id.btnContactos);

        tvSaldoTotal = findViewById(R.id.tvSaldoTotal);

        btnCuentas.setOnClickListener(v -> {
            if (!autenticado) return;

            Intent intent = new Intent(
                    MainActivity.this,
                    CuentasActivity.class
            );

            startActivity(intent);
        });

        btnMovimientos.setOnClickListener(v -> {
            if (!autenticado) return;

            Intent intent = new Intent(
                    MainActivity.this,
                    MovimientosActivity.class
            );

            startActivity(intent);
        });

        btnContactos.setOnClickListener(v -> {
            if (!autenticado) return;

            Intent intent = new Intent(
                    MainActivity.this,
                    ContactosActivity.class
            );

            startActivity(intent);
        });

        configurarAutenticacionBiometrica();
    }

    private void configurarAutenticacionBiometrica() {

        Executor executor = ContextCompat.getMainExecutor(this);

        biometricPrompt = new BiometricPrompt(
                this,
                executor,
                new BiometricPrompt.AuthenticationCallback() {

                    @Override
                    public void onAuthenticationSucceeded(
                            @NonNull BiometricPrompt.AuthenticationResult result) {

                        super.onAuthenticationSucceeded(result);

                        autenticacionEnCurso = false;
                        autenticado = true;

                        contenidoPrincipal.setVisibility(View.VISIBLE);

                        actualizarSaldoTotal();

                        Toast.makeText(
                                MainActivity.this,
                                "Acceso autorizado",
                                Toast.LENGTH_SHORT
                        ).show();
                    }

                    @Override
                    public void onAuthenticationError(
                            int errorCode,
                            @NonNull CharSequence errString) {

                        super.onAuthenticationError(errorCode, errString);

                        autenticacionEnCurso = false;

                        if (!autenticado) {
                            contenidoPrincipal.setVisibility(View.INVISIBLE);

                            Toast.makeText(
                                    MainActivity.this,
                                    "Autenticación cancelada o no disponible",
                                    Toast.LENGTH_SHORT
                            ).show();

                            finish();
                        }
                    }

                    @Override
                    public void onAuthenticationFailed() {

                        super.onAuthenticationFailed();

                        Toast.makeText(
                                MainActivity.this,
                                "Huella no reconocida. Intenta nuevamente.",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );

        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Cash MxJ")
                .setSubtitle("Confirma tu identidad para acceder")
                .setAllowedAuthenticators(
                        BiometricManager.Authenticators.BIOMETRIC_STRONG
                                | BiometricManager.Authenticators.DEVICE_CREDENTIAL
                )
                .build();
    }

    private void iniciarAutenticacion() {

        if (autenticado || autenticacionEnCurso) {
            return;
        }

        BiometricManager biometricManager =
                BiometricManager.from(this);

        int resultado = biometricManager.canAuthenticate(
                BiometricManager.Authenticators.BIOMETRIC_STRONG
                        | BiometricManager.Authenticators.DEVICE_CREDENTIAL
        );

        if (resultado == BiometricManager.BIOMETRIC_SUCCESS) {

            autenticacionEnCurso = true;
            biometricPrompt.authenticate(promptInfo);

        } else {

            contenidoPrincipal.setVisibility(View.INVISIBLE);

            Toast.makeText(
                    this,
                    "Configura una huella o bloqueo de pantalla seguro para acceder.",
                    Toast.LENGTH_LONG
            ).show();

            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (autenticado) {
            actualizarSaldoTotal();
        } else {
            contenidoPrincipal.setVisibility(View.INVISIBLE);
            iniciarAutenticacion();
        }
    }

    private void actualizarSaldoTotal() {

        if (!autenticado) {
            return;
        }

        long saldoTotal = databaseHelper.obtenerSaldoTotal();

        NumberFormat formato =
                NumberFormat.getCurrencyInstance(
                        new Locale("es", "CL")
                );

        formato.setMaximumFractionDigits(0);
        formato.setMinimumFractionDigits(0);

        tvSaldoTotal.setText(formato.format(saldoTotal));
    }

    @Override
    protected void onDestroy() {

        if (databaseHelper != null) {
            databaseHelper.close();
        }

        super.onDestroy();
    }
}
