
package com.example.cashmxj;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.cashmxj.database.DatabaseHelper;

import java.util.List;

public class ContactosActivity extends AppCompatActivity {

    private DatabaseHelper databaseHelper;
    private LinearLayout contenedorContactos;
    private TextView tvSinContactos;
    private Button btnNuevoContacto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_contactos);

        databaseHelper = new DatabaseHelper(this);

        View btnVolver = findViewById(R.id.btnVolverContactos);

        btnNuevoContacto = findViewById(R.id.btnNuevoContacto);
        contenedorContactos = findViewById(R.id.contenedorContactos);
        tvSinContactos = findViewById(R.id.tvSinContactos);

        btnVolver.setOnClickListener(v -> finish());

        btnNuevoContacto.setOnClickListener(v -> {
            Intent intent = new Intent(
                    ContactosActivity.this,
                    NuevoContactoActivity.class
            );

            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarContactos();
    }

    private void cargarContactos() {

        List<String[]> contactos = databaseHelper.obtenerContactos();

        contenedorContactos.removeAllViews();

        tvSinContactos.setVisibility(
                contactos.isEmpty() ? View.VISIBLE : View.GONE
        );

        for (String[] contacto : contactos) {

            int id = Integer.parseInt(contacto[0]);
            String nombre = contacto[1];
            String telefono = contacto[2];
            String correo = contacto[3];

            StringBuilder informacion = new StringBuilder(nombre);

            if (telefono != null && !telefono.isEmpty()) {
                informacion.append("\n").append(telefono);
            }

            if (correo != null && !correo.isEmpty()) {
                informacion.append("\n").append(correo);
            }

            TextView vistaContacto = new TextView(this);

            vistaContacto.setText(informacion.toString());
            vistaContacto.setTextSize(16);

            vistaContacto.setTextColor(
                    getColor(R.color.cash_text_primary)
            );

            vistaContacto.setBackgroundResource(
                    R.drawable.bg_card
            );

            int horizontal = dp(16);
            int vertical = dp(12);

            vistaContacto.setPadding(
                    horizontal,
                    vertical,
                    horizontal,
                    vertical
            );

            LinearLayout.LayoutParams parametros =
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );

            parametros.setMargins(0, 0, 0, dp(12));

            vistaContacto.setLayoutParams(parametros);

            vistaContacto.setOnClickListener(v -> {
                mostrarOpcionesContacto(id, nombre);
            });

            contenedorContactos.addView(vistaContacto);
        }
    }

    private void mostrarOpcionesContacto(int id, String nombre) {

        String[] opciones = {
                "Editar contacto",
                "Eliminar contacto"
        };

        new AlertDialog.Builder(this)
                .setTitle(nombre)
                .setItems(opciones, (dialog, opcion) -> {

                    if (opcion == 0) {

                        Intent intent = new Intent(
                                this,
                                NuevoContactoActivity.class
                        );

                        intent.putExtra("contacto_id", id);

                        startActivity(intent);

                    } else {

                        confirmarEliminarContacto(id, nombre);
                    }
                })
                .show();
    }

    private void confirmarEliminarContacto(int id, String nombre) {

        new AlertDialog.Builder(this)
                .setTitle("Eliminar contacto")
                .setMessage(
                        "¿Deseas eliminar a " + nombre + "?"
                )
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Eliminar", (dialog, which) -> {

                    int resultado = databaseHelper.eliminarContacto(id);

                    if (resultado > 0) {

                        Toast.makeText(
                                this,
                                "Contacto eliminado",
                                Toast.LENGTH_SHORT
                        ).show();

                        cargarContactos();

                    } else {

                        Toast.makeText(
                                this,
                                "No se pudo eliminar el contacto",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                })
                .show();
    }

    private int dp(int valor) {
        return Math.round(
                valor * getResources().getDisplayMetrics().density
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
