package com.example.cashmxj;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import com.example.cashmxj.database.UsuariosRepository;

public class LoginActivity extends AppCompatActivity {
    private EditText correo, clave;
    private SharedPreferences preferencias;
    private UsuariosRepository usuarios;
    private BiometricPrompt huella;
    private static final int TIPO = BiometricManager.Authenticators.BIOMETRIC_STRONG;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_login);
        correo = findViewById(R.id.loginCorreo);
        clave = findViewById(R.id.loginClave);
        usuarios = new UsuariosRepository(this);
        preferencias = getSharedPreferences("cashmxj_acceso", MODE_PRIVATE);
        findViewById(R.id.btnIngresar).setOnClickListener(v -> ingresar());
        findViewById(R.id.btnCrearUsuario).setOnClickListener(v -> startActivity(new Intent(this, RegistroActivity.class)));
        findViewById(R.id.btnHuella).setOnClickListener(v -> accederHuella());
        huella = new BiometricPrompt(this, ContextCompat.getMainExecutor(this), new BiometricPrompt.AuthenticationCallback() {
            @Override public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                if (preferencias.getBoolean("huella_activa", false) && !preferencias.getString("correo_huella", "").isEmpty()) abrirInicio();
            }
            @Override public void onAuthenticationFailed() { Toast.makeText(LoginActivity.this, "Huella no reconocida", Toast.LENGTH_SHORT).show(); }
        });
    }
    @Override protected void onResume() {
        super.onResume();
        boolean disponible = preferencias.getBoolean("huella_activa", false)
                && !preferencias.getString("correo_huella", "").isEmpty()
                && BiometricManager.from(this).canAuthenticate(TIPO) == BiometricManager.BIOMETRIC_SUCCESS;
        findViewById(R.id.btnHuella).setVisibility(disponible ? View.VISIBLE : View.GONE);
    }
    private void ingresar() {
        String email = correo.getText().toString().trim();
        String password = clave.getText().toString();
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches() || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Ingresa un correo válido y contraseña", Toast.LENGTH_LONG).show(); return;
        }
        try {
            if (!usuarios.validar(email, password)) { Toast.makeText(this, "Correo o contraseña incorrectos", Toast.LENGTH_LONG).show(); return; }
            if (!preferencias.getBoolean("huella_preguntada_" + email.toLowerCase(java.util.Locale.ROOT), false)) {
                preguntarHuella(email);
            } else abrirInicio();
        } catch (Exception e) { Toast.makeText(this, "No se pudo validar el acceso", Toast.LENGTH_LONG).show(); }
    }
    private void preguntarHuella(String email) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Acceso rápido con huella")
                .setMessage("¿Deseas habilitar tu huella digital para ingresar más rápido a Cash MxJ en este teléfono?")
                .setPositiveButton("Habilitar", (d, w) -> {
                    preferencias.edit().putBoolean("huella_preguntada_" + email.toLowerCase(java.util.Locale.ROOT), true).apply();
                    if (BiometricManager.from(this).canAuthenticate(TIPO) != BiometricManager.BIOMETRIC_SUCCESS) {
                        Toast.makeText(this, "Configura una huella en los ajustes de Android", Toast.LENGTH_LONG).show();
                        abrirInicio(); return;
                    }
                    huella = new BiometricPrompt(this, ContextCompat.getMainExecutor(this), new BiometricPrompt.AuthenticationCallback() {
                        @Override public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult r) {
                            preferencias.edit().putString("correo_huella", email).putBoolean("huella_activa", true).apply();
                            abrirInicio();
                        }
                        @Override public void onAuthenticationError(int code, @NonNull CharSequence error) { abrirInicio(); }
                    });
                    mostrarHuella("Activa tu acceso con huella");
                })
                .setNegativeButton("Ahora no", (d, w) -> {
                    preferencias.edit().putBoolean("huella_preguntada_" + email.toLowerCase(java.util.Locale.ROOT), true).apply();
                    abrirInicio();
                }).setCancelable(false).show();
    }
    private void accederHuella() {
        if (!preferencias.getBoolean("huella_activa", false)) return;
        mostrarHuella("Ingresa con tu huella");
    }
    private void mostrarHuella(String titulo) {
        huella.authenticate(new BiometricPrompt.PromptInfo.Builder()
                .setTitle(titulo).setSubtitle("Cash MxJ · Acceso seguro")
                .setNegativeButtonText("Usar contraseña").setAllowedAuthenticators(TIPO).build());
    }
    private void abrirInicio() {
        ((CashMxJApplication)getApplication()).marcarAutenticado();
        startActivity(new Intent(this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
        finish();
    }
}
