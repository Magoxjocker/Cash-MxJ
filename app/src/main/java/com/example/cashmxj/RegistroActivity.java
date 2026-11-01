package com.example.cashmxj;

import android.os.Bundle;
import android.util.Patterns;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.cashmxj.database.UsuariosRepository;

public class RegistroActivity extends AppCompatActivity {
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_registro);
        EditText correo = findViewById(R.id.registroCorreo);
        EditText clave = findViewById(R.id.registroClave);
        EditText repetir = findViewById(R.id.registroConfirmar);
        findViewById(R.id.btnRegistrar).setOnClickListener(v -> {
            String email = correo.getText().toString().trim();
            String pass = clave.getText().toString();
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) { correo.setError("Correo inválido"); return; }
            if (pass.length() < 8) { clave.setError("Mínimo 8 caracteres"); return; }
            if (!pass.equals(repetir.getText().toString())) { repetir.setError("Las contraseñas no coinciden"); return; }
            try {
                if (new UsuariosRepository(this).registrar(email, pass)) {
                    Toast.makeText(this, "Cuenta creada. Inicia sesión", Toast.LENGTH_LONG).show(); finish();
                } else correo.setError("Este correo ya está registrado");
            } catch (Exception e) { Toast.makeText(this, "No se pudo crear la cuenta", Toast.LENGTH_LONG).show(); }
        });
    }
}
