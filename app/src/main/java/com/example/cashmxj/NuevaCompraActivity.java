
package com.example.cashmxj;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cashmxj.database.DatabaseHelper;
import com.example.cashmxj.models.Cuenta;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class NuevaCompraActivity extends AppCompatActivity {

    public static final String EXTRA_COMPRA_ID = "compra_id";

    private DatabaseHelper databaseHelper;
    private CashMxJApplication aplicacion;

    private EditText etDescripcionCompra;
    private EditText etMontoCompra;

    private Spinner spCuentaCompra;
    private Spinner spContactoCompra;
    private Spinner spEstadoCompra;

    private Button btnFechaCompra;
    private Button btnGuardarCompra;

    private final List<Integer> idsCuentas = new ArrayList<>();
    private final List<Integer> idsContactos = new ArrayList<>();

    private final Calendar fechaSeleccionada = Calendar.getInstance();

    private int compraId = -1;
    private boolean guardando = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_nueva_compra);

        aplicacion = (CashMxJApplication) getApplication();
        databaseHelper = new DatabaseHelper(this);

        etDescripcionCompra = findViewById(R.id.etDescripcionCompra);
        etMontoCompra = findViewById(R.id.etMontoCompra);

        spCuentaCompra = findViewById(R.id.spCuentaCompra);
        spContactoCompra = findViewById(R.id.spContactoCompra);
        spEstadoCompra = findViewById(R.id.spEstadoCompra);

        btnFechaCompra = findViewById(R.id.btnFechaCompra);
        btnGuardarCompra = findViewById(R.id.btnGuardarCompra);

        compraId = getIntent().getIntExtra(EXTRA_COMPRA_ID, -1);

        cargarCuentas();
        cargarContactos();
        cargarEstados();

        actualizarTextoFecha();

        if (compraId > 0) {
            cargarCompraParaEditar();
        }

        btnFechaCompra.setOnClickListener(v -> seleccionarFecha());
        btnGuardarCompra.setOnClickListener(v -> guardarCompra());
    }

    private void cargarCuentas() {

        List<Cuenta> cuentas = databaseHelper.obtenerCuentas();
        List<String> nombres = new ArrayList<>();

        idsCuentas.clear();

        for (Cuenta cuenta : cuentas) {
            idsCuentas.add(cuenta.getId());
            nombres.add(cuenta.getNombre());
        }

        if (nombres.isEmpty()) {
            nombres.add("No hay cuentas disponibles");
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                nombres
        );

        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spCuentaCompra.setAdapter(adapter);
    }

    private void cargarContactos() {

        List<String[]> contactos = databaseHelper.obtenerContactos();
        List<String> nombres = new ArrayList<>();

        idsContactos.clear();

        idsContactos.add(null);
        nombres.add("Sin contacto asociado");

        for (String[] contacto : contactos) {
            idsContactos.add(Integer.parseInt(contacto[0]));
            nombres.add(contacto[1]);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                nombres
        );

        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spContactoCompra.setAdapter(adapter);
    }

    private void cargarEstados() {

        String[] estados = {
                "Realizada",
                "Programada"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                estados
        );

        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spEstadoCompra.setAdapter(adapter);
    }

    private void cargarCompraParaEditar() {

        String[] compra = databaseHelper.obtenerCompraPorId(compraId);

        if (compra == null ||
                !DatabaseHelper.COMPRA_PROGRAMADA.equals(compra[6])) {

            Toast.makeText(
                    this,
                    "Solo se pueden editar compras programadas",
                    Toast.LENGTH_LONG
            ).show();

            finish();
            return;
        }

        etDescripcionCompra.setText(compra[1]);
        etMontoCompra.setText(compra[2]);

        fechaSeleccionada.setTimeInMillis(
                Long.parseLong(compra[3])
        );

        actualizarTextoFecha();

        int cuentaId = Integer.parseInt(compra[4]);
        int posicionCuenta = idsCuentas.indexOf(cuentaId);

        if (posicionCuenta >= 0) {
            spCuentaCompra.setSelection(posicionCuenta);
        }

        Integer contactoId = compra[5].isEmpty()
                ? null
                : Integer.parseInt(compra[5]);

        int posicionContacto = idsContactos.indexOf(contactoId);

        if (posicionContacto >= 0) {
            spContactoCompra.setSelection(posicionContacto);
        }

        spEstadoCompra.setSelection(1);
        spEstadoCompra.setEnabled(false);

        btnGuardarCompra.setText("Guardar cambios");
        setTitle("Editar compra programada");
    }

    private void seleccionarFecha() {

        int anio = fechaSeleccionada.get(Calendar.YEAR);
        int mes = fechaSeleccionada.get(Calendar.MONTH);
        int dia = fechaSeleccionada.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, anioElegido, mesElegido, diaElegido) -> {

                    fechaSeleccionada.set(
                            Calendar.YEAR,
                            anioElegido
                    );

                    fechaSeleccionada.set(
                            Calendar.MONTH,
                            mesElegido
                    );

                    fechaSeleccionada.set(
                            Calendar.DAY_OF_MONTH,
                            diaElegido
                    );

                    actualizarTextoFecha();
                },
                anio,
                mes,
                dia
        );

        dialog.show();
    }

    private void actualizarTextoFecha() {

        SimpleDateFormat formato = new SimpleDateFormat(
                "dd/MM/yyyy",
                new Locale("es", "CL")
        );

        btnFechaCompra.setText(
                formato.format(fechaSeleccionada.getTime())
        );
    }

    private void guardarCompra() {

        if (guardando || aplicacion.requiereAutenticacion()) {
            return;
        }

        String descripcion = etDescripcionCompra.getText()
                .toString()
                .trim();

        String montoTexto = etMontoCompra.getText()
                .toString()
                .trim();

        if (descripcion.isEmpty()) {
            etDescripcionCompra.setError("Ingresa una descripción");
            etDescripcionCompra.requestFocus();
            return;
        }

        if (montoTexto.isEmpty()) {
            etMontoCompra.setError("Ingresa el monto");
            etMontoCompra.requestFocus();
            return;
        }

        long monto;

        try {
            monto = Long.parseLong(montoTexto);
        } catch (NumberFormatException e) {
            etMontoCompra.setError("Ingresa un monto válido");
            return;
        }

        if (monto <= 0) {
            etMontoCompra.setError("El monto debe ser mayor que cero");
            return;
        }

        if (idsCuentas.isEmpty()) {
            Toast.makeText(
                    this,
                    "Primero debes registrar una cuenta",
                    Toast.LENGTH_LONG
            ).show();
            return;
        }

        int posicionCuenta = spCuentaCompra.getSelectedItemPosition();

        if (posicionCuenta < 0 ||
                posicionCuenta >= idsCuentas.size()) {
            return;
        }

        int cuentaId = idsCuentas.get(posicionCuenta);

        int posicionContacto = spContactoCompra.getSelectedItemPosition();
        Integer contactoId = null;

        if (posicionContacto >= 0 &&
                posicionContacto < idsContactos.size()) {
            contactoId = idsContactos.get(posicionContacto);
        }

        String estado = spEstadoCompra.getSelectedItemPosition() == 1
                ? DatabaseHelper.COMPRA_PROGRAMADA
                : DatabaseHelper.COMPRA_REALIZADA;

        long fecha = fechaSeleccionada.getTimeInMillis();

        guardando = true;
        btnGuardarCompra.setEnabled(false);

        try {

            boolean exito;

            if (compraId > 0) {

                int resultado = databaseHelper.actualizarCompraProgramada(
                        compraId,
                        descripcion,
                        monto,
                        fecha,
                        cuentaId,
                        contactoId
                );

                exito = resultado == 1;

            } else {

                long resultado = databaseHelper.insertarCompra(
                        descripcion,
                        monto,
                        fecha,
                        cuentaId,
                        contactoId,
                        estado
                );

                exito = resultado > 0;
            }

            if (exito) {

                Toast.makeText(
                        this,
                        compraId > 0
                                ? "Compra actualizada correctamente"
                                : "Compra guardada correctamente",
                        Toast.LENGTH_SHORT
                ).show();

                finish();

            } else {

                Toast.makeText(
                        this,
                        "No se pudo guardar. Verifica los datos "
                                + "y el saldo disponible.",
                        Toast.LENGTH_LONG
                ).show();
            }

        } catch (Exception e) {

            Toast.makeText(
                    this,
                    "Ocurrió un error al guardar la compra",
                    Toast.LENGTH_LONG
            ).show();

        } finally {
            guardando = false;
            btnGuardarCompra.setEnabled(true);
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
