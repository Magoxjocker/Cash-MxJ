
package com.example.cashmxj;

import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cashmxj.database.DatabaseHelper;

public class NuevoContactoActivity extends AppCompatActivity {

    private EditText etNombreContacto;
    private EditText etTelefonoContacto;
    private EditText etCorreoContacto;

    private Button btnGuardarContacto;

    private DatabaseHelper databaseHelper;

    private int contactoId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_nuevo_contacto);

        databaseHelper = new DatabaseHelper(this);

        etNombreContacto = findViewById(R.id.etNombreContacto);
        etTelefonoContacto = findViewById(R.id.etTelefonoContacto);
        etCorreoContacto = findViewById(R.id.etCorreoContacto);

        btnGuardarContacto = findViewById(R.id.btnGuardarContacto);

        TextView btnVolver = findViewById(R.id.btnVolverNuevoContacto);
        TextView tvTitulo = findViewById(R.id.tvTituloContacto);

        btnVolver.setOnClickListener(v -> finish());

        contactoId = getIntent().getIntExtra("contacto_id", -1);

        if (contactoId != -1) {

            tvTitulo.setText("Editar contacto");
            btnGuardarContacto.setText("Guardar cambios");

            cargarContacto();
        }

        btnGuardarContacto.setOnClickListener(v -> guardarContacto());
    }

    private void cargarContacto() {

        String[] contacto =
                databaseHelper.obtenerContactoPorId(contactoId);

        if (contacto == null) {

            Toast.makeText(
                    this,
                    "No se encontró el contacto",
                    Toast.LENGTH_SHORT
            ).show();

            finish();
            return;
        }

        etNombreContacto.setText(contacto[1]);
        etTelefonoContacto.setText(contacto[2]);
        etCorreoContacto.setText(contacto[3]);
    }

    private void guardarContacto() {

        String nombre = etNombreContacto.getText()
                .toString().trim();

        String telefono = etTelefonoContacto.getText()
                .toString().trim();

        String correo = etCorreoContacto.getText()
                .toString().trim();

        if (nombre.isEmpty()) {

            etNombreContacto.setError("Ingresa un nombre");
            etNombreContacto.requestFocus();
            return;
        }

        if (!correo.isEmpty() &&
                !Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {

            etCorreoContacto.setError(
                    "Ingresa un correo electrónico válido"
            );

            etCorreoContacto.requestFocus();
            return;
        }

        boolean guardado;

        if (contactoId == -1) {

            long resultado = databaseHelper.insertarContacto(
                    nombre,
                    telefono,
                    correo
            );

            guardado = resultado != -1;

        } else {

            int resultado = databaseHelper.actualizarContacto(
                    contactoId,
                    nombre,
                    telefono,
                    correo
            );

            guardado = resultado > 0;
        }

        if (guardado) {

            Toast.makeText(
                    this,
                    contactoId == -1
                            ? "Contacto guardado correctamente"
                            : "Contacto actualizado correctamente",
                    Toast.LENGTH_SHORT
            ).show();

            finish();

        } else {

            Toast.makeText(
                    this,
                    "No se pudo guardar el contacto",
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
