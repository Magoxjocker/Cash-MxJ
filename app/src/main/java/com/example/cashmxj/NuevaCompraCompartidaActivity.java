
package com.example.cashmxj;

import android.os.Bundle;
import android.graphics.Color;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cashmxj.database.DatabaseHelper;
import com.example.cashmxj.database.ComprasCompartidasRepository;
import com.example.cashmxj.models.Cuenta;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class NuevaCompraCompartidaActivity extends AppCompatActivity {

    private EditText etDescripcionCompartida;
    private EditText etNombreProducto;
    private EditText etCantidadProducto;
    private EditText etPrecioProducto;

    private Spinner spCuentaCompartida;

    private LinearLayout contenedorProductos;
    private LinearLayout contenedorContactosCompartida;

    private TextView tvTotalCompartida;
    private TextView tvResumenReparto;

    private Button btnAgregarProducto;
    private Button btnGuardarCompartida;
    private Button btnVolverNuevaCompartida;

    private DatabaseHelper databaseHelper;
    private ComprasCompartidasRepository repository;

    private final List<Cuenta> cuentas = new ArrayList<>();

    private final List<ComprasCompartidasRepository.Item> items =
            new ArrayList<>();

    private final List<Integer> contactosIds = new ArrayList<>();
    private final List<String> contactosNombres = new ArrayList<>();
    private final List<CheckBox> checkBoxesContactos = new ArrayList<>();

    private final NumberFormat formatoMoneda =
            NumberFormat.getIntegerInstance(
                    new Locale("es", "CL")
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_nueva_compra_compartida
        );

        databaseHelper = new DatabaseHelper(this);

        repository = new ComprasCompartidasRepository(
                databaseHelper
        );

        inicializarVistas();

        // =====================================
        // BOTÓN VOLVER
        // =====================================

        btnVolverNuevaCompartida.setOnClickListener(
                v -> finish()
        );

        cargarCuentas();
        cargarContactos();

        btnAgregarProducto.setOnClickListener(
                v -> agregarProducto()
        );

        btnGuardarCompartida.setOnClickListener(
                v -> guardarCompraCompartida()
        );

        actualizarProductos();
        actualizarResumen();
    }

    // =====================================
    // INICIALIZAR VISTAS
    // =====================================

    private void inicializarVistas() {

        btnVolverNuevaCompartida = findViewById(
                R.id.btnVolverNuevaCompartida
        );

        etDescripcionCompartida = findViewById(
                R.id.etDescripcionCompartida
        );

        etNombreProducto = findViewById(
                R.id.etNombreProducto
        );

        etCantidadProducto = findViewById(
                R.id.etCantidadProducto
        );

        etPrecioProducto = findViewById(
                R.id.etPrecioProducto
        );

        spCuentaCompartida = findViewById(
                R.id.spCuentaCompartida
        );

        contenedorProductos = findViewById(
                R.id.contenedorProductos
        );

        contenedorContactosCompartida = findViewById(
                R.id.contenedorContactosCompartida
        );

        tvTotalCompartida = findViewById(
                R.id.tvTotalCompartida
        );

        tvResumenReparto = findViewById(
                R.id.tvResumenReparto
        );

        btnAgregarProducto = findViewById(
                R.id.btnAgregarProducto
        );

        btnGuardarCompartida = findViewById(
                R.id.btnGuardarCompartida
        );
    }

    // =====================================
    // CARGAR CUENTAS
    // =====================================

    private void cargarCuentas() {

        cuentas.clear();

        cuentas.addAll(
                databaseHelper.obtenerCuentas()
        );

        List<String> nombres = new ArrayList<>();

        for (Cuenta cuenta : cuentas) {

            nombres.add(
                    cuenta.getNombre() +
                            " - Saldo: $" +
                            formatoMoneda.format(cuenta.getSaldo())
            );
        }

        if (nombres.isEmpty()) {
            nombres.add("No existen cuentas registradas");
        }

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_item,
                        nombres
                );

        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spCuentaCompartida.setAdapter(adapter);
    }

    // =====================================
    // CARGAR CONTACTOS
    // =====================================

    private void cargarContactos() {

        contactosIds.clear();
        contactosNombres.clear();
        checkBoxesContactos.clear();

        contenedorContactosCompartida.removeAllViews();

        List<String[]> contactos =
                databaseHelper.obtenerContactos();

        if (contactos.isEmpty()) {

            TextView aviso = new TextView(this);

            aviso.setText(
                    "No existen contactos registrados. " +
                            "Agrega contactos desde el menú principal."
            );

            aviso.setTextColor(Color.DKGRAY);
            aviso.setTextSize(14);

            contenedorContactosCompartida.addView(aviso);

            return;
        }

        for (String[] contacto : contactos) {

            int contactoId = Integer.parseInt(
                    contacto[0]
            );

            String nombre = contacto[1];

            CheckBox checkBox = new CheckBox(this);

            checkBox.setText(nombre);
            checkBox.setTextColor(Color.BLACK);
            checkBox.setTextSize(16);

            checkBox.setOnCheckedChangeListener(
                    (buttonView, isChecked) ->
                            actualizarResumen()
            );

            contactosIds.add(contactoId);
            contactosNombres.add(nombre);
            checkBoxesContactos.add(checkBox);

            contenedorContactosCompartida.addView(
                    checkBox
            );
        }
    }

    // =====================================
    // AGREGAR PRODUCTO
    // =====================================

    private void agregarProducto() {

        String descripcion =
                etNombreProducto.getText()
                        .toString()
                        .trim();

        String cantidadTexto =
                etCantidadProducto.getText()
                        .toString()
                        .trim();

        String precioTexto =
                etPrecioProducto.getText()
                        .toString()
                        .trim();

        if (descripcion.isEmpty()) {

            etNombreProducto.setError(
                    "Ingresa el nombre del producto"
            );

            return;
        }

        if (cantidadTexto.isEmpty()) {

            etCantidadProducto.setError(
                    "Ingresa la cantidad"
            );

            return;
        }

        if (precioTexto.isEmpty()) {

            etPrecioProducto.setError(
                    "Ingresa el precio"
            );

            return;
        }

        try {

            int cantidad = Integer.parseInt(
                    cantidadTexto
            );

            long precio = Long.parseLong(
                    precioTexto
            );

            if (cantidad <= 0 || precio <= 0) {

                mostrarMensaje(
                        "La cantidad y el precio deben ser mayores a cero"
                );

                return;
            }

            Math.multiplyExact(
                    (long) cantidad,
                    precio
            );

            ComprasCompartidasRepository.Item item =
                    new ComprasCompartidasRepository.Item(
                            descripcion,
                            cantidad,
                            precio
                    );

            items.add(item);

            try {

                repository.calcularTotal(items);

            } catch (ArithmeticException e) {

                items.remove(items.size() - 1);

                mostrarMensaje(
                        "El total de la compra es demasiado grande"
                );

                return;
            }

            etNombreProducto.setText("");
            etCantidadProducto.setText("");
            etPrecioProducto.setText("");

            actualizarProductos();
            actualizarResumen();

        } catch (NumberFormatException |
                 ArithmeticException e) {

            mostrarMensaje(
                    "Ingresa una cantidad y un precio válidos"
            );
        }
    }

    // =====================================
    // MOSTRAR PRODUCTOS
    // =====================================

    private void actualizarProductos() {

        contenedorProductos.removeAllViews();

        if (items.isEmpty()) {

            TextView aviso = new TextView(this);

            aviso.setText(
                    "Todavía no has agregado productos."
            );

            aviso.setTextColor(Color.DKGRAY);

            contenedorProductos.addView(aviso);

            return;
        }

        for (int i = 0; i < items.size(); i++) {

            final int posicion = i;

            ComprasCompartidasRepository.Item item =
                    items.get(i);

            LinearLayout fila = new LinearLayout(this);

            fila.setOrientation(
                    LinearLayout.VERTICAL
            );

            fila.setPadding(12, 14, 12, 14);

            TextView detalle = new TextView(this);

            long subtotal = Math.multiplyExact(
                    (long) item.cantidad,
                    item.precioUnitario
            );

            detalle.setText(
                    item.descripcion +
                            "\nCantidad: " +
                            item.cantidad +
                            " × $" +
                            formatoMoneda.format(item.precioUnitario) +
                            "\nSubtotal: $" +
                            formatoMoneda.format(subtotal)
            );

            detalle.setTextColor(Color.BLACK);
            detalle.setTextSize(15);

            Button eliminar = new Button(this);

            eliminar.setText("Quitar producto");
            eliminar.setAllCaps(false);

            eliminar.setOnClickListener(v -> {

                items.remove(posicion);

                actualizarProductos();
                actualizarResumen();
            });

            fila.addView(detalle);
            fila.addView(eliminar);

            contenedorProductos.addView(fila);
        }
    }

    // =====================================
    // CONTACTOS SELECCIONADOS
    // =====================================

    private List<Integer> obtenerContactosSeleccionados() {

        List<Integer> seleccionados = new ArrayList<>();

        for (int i = 0;
             i < checkBoxesContactos.size();
             i++) {

            if (checkBoxesContactos.get(i).isChecked()) {

                seleccionados.add(
                        contactosIds.get(i)
                );
            }
        }

        return seleccionados;
    }

    // =====================================
    // ACTUALIZAR RESUMEN
    // =====================================

    private void actualizarResumen() {

        if (items.isEmpty()) {

            tvTotalCompartida.setText(
                    "Total: $0"
            );

            tvResumenReparto.setText(
                    "Agrega productos y selecciona " +
                            "los contactos para calcular el reparto."
            );

            return;
        }

        long total;

        try {

            total = repository.calcularTotal(items);

        } catch (ArithmeticException |
                 IllegalArgumentException e) {

            tvTotalCompartida.setText(
                    "Total inválido"
            );

            tvResumenReparto.setText(
                    "Revisa los valores de los productos."
            );

            return;
        }

        tvTotalCompartida.setText(
                "Total: $" +
                        formatoMoneda.format(total)
        );

        List<Integer> seleccionados =
                obtenerContactosSeleccionados();

        int cantidadPersonas =
                seleccionados.size() + 1;

        List<Long> distribucion =
                repository.calcularDistribucion(
                        total,
                        cantidadPersonas
                );

        StringBuilder resumen = new StringBuilder();

        resumen.append(
                "Distribución del gasto\n\n"
        );

        resumen.append(
                "Tú (pagador): $"
        );

        resumen.append(
                formatoMoneda.format(
                        distribucion.get(0)
                )
        );

        int indiceDistribucion = 1;

        for (int i = 0;
             i < checkBoxesContactos.size();
             i++) {

            if (checkBoxesContactos.get(i).isChecked()) {

                resumen.append("\n");

                resumen.append(
                        contactosNombres.get(i)
                );

                resumen.append(": $");

                resumen.append(
                        formatoMoneda.format(
                                distribucion.get(
                                        indiceDistribucion
                                )
                        )
                );

                indiceDistribucion++;
            }
        }

        resumen.append(
                "\n\nTotal: $"
        );

        resumen.append(
                formatoMoneda.format(total)
        );

        resumen.append(
                "\nEl monto total se descontará " +
                        "de la cuenta seleccionada."
        );

        tvResumenReparto.setText(
                resumen.toString()
        );
    }

    // =====================================
    // GUARDAR COMPRA COMPARTIDA
    // =====================================

    private void guardarCompraCompartida() {

        String descripcion =
                etDescripcionCompartida.getText()
                        .toString()
                        .trim();

        if (descripcion.isEmpty()) {

            etDescripcionCompartida.setError(
                    "Ingresa una descripción"
            );

            return;
        }

        if (cuentas.isEmpty()) {

            mostrarMensaje(
                    "Debes registrar una cuenta primero"
            );

            return;
        }

        if (items.isEmpty()) {

            mostrarMensaje(
                    "Agrega al menos un producto"
            );

            return;
        }

        List<Integer> seleccionados =
                obtenerContactosSeleccionados();

        if (seleccionados.isEmpty()) {

            mostrarMensaje(
                    "Selecciona al menos un contacto"
            );

            return;
        }

        int posicionCuenta =
                spCuentaCompartida.getSelectedItemPosition();

        if (posicionCuenta < 0 ||
                posicionCuenta >= cuentas.size()) {

            mostrarMensaje(
                    "Selecciona una cuenta válida"
            );

            return;
        }

        Cuenta cuenta = cuentas.get(
                posicionCuenta
        );

        long total;

        try {

            total = repository.calcularTotal(items);

        } catch (Exception e) {

            mostrarMensaje(
                    "El total de la compra es inválido"
            );

            return;
        }

        if (cuenta.getSaldo() < total) {

            mostrarMensaje(
                    "Saldo insuficiente en la cuenta seleccionada"
            );

            return;
        }

        // Evitar guardar dos veces
        // mientras se procesa la operación.
        btnGuardarCompartida.setEnabled(false);

        try {

            long compraId =
                    repository.registrarCompraCompartida(
                            descripcion,
                            System.currentTimeMillis(),
                            cuenta.getId(),
                            new ArrayList<>(items),
                            seleccionados
                    );

            if (compraId > 0) {

                mostrarMensaje(
                        "Compra compartida guardada correctamente"
                );

                setResult(RESULT_OK);
                finish();

            } else {

                mostrarMensaje(
                        "No se pudo registrar la compra. " +
                                "Verifica el saldo y los contactos."
                );
            }

        } catch (Exception e) {

            mostrarMensaje(
                    "Ocurrió un error al guardar la compra"
            );

        } finally {

            btnGuardarCompartida.setEnabled(true);
        }
    }

    // =====================================
    // MENSAJES
    // =====================================

    private void mostrarMensaje(String mensaje) {

        Toast.makeText(
                this,
                mensaje,
                Toast.LENGTH_LONG
        ).show();
    }

    @Override
    protected void onDestroy() {

        if (databaseHelper != null) {
            databaseHelper.close();
        }

        super.onDestroy();
    }
}
