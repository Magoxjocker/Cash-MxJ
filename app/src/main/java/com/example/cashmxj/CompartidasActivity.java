
package com.example.cashmxj;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.cashmxj.database.ComprasCompartidasRepository;
import com.example.cashmxj.database.DatabaseHelper;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CompartidasActivity extends AppCompatActivity {

    private LinearLayout contenedorHistorialCompartidas;
    private Button btnNuevaCompartida;
    private Button btnVolverCompartidas;

    private DatabaseHelper databaseHelper;
    private ComprasCompartidasRepository repository;
    private CashMxJApplication aplicacion;

    private final NumberFormat formatoMoneda =
            NumberFormat.getIntegerInstance(
                    new Locale("es", "CL")
            );

    private final DateFormat formatoFecha =
            DateFormat.getDateInstance(
                    DateFormat.MEDIUM,
                    new Locale("es", "CL")
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_compartidas);

        aplicacion = (CashMxJApplication) getApplication();

        contenedorHistorialCompartidas = findViewById(
                R.id.contenedorHistorialCompartidas
        );

        btnNuevaCompartida = findViewById(
                R.id.btnNuevaCompartida
        );

        btnVolverCompartidas = findViewById(
                R.id.btnVolverCompartidas
        );

        databaseHelper = new DatabaseHelper(this);

        repository = new ComprasCompartidasRepository(
                databaseHelper
        );

        // VOLVER AL MENU PRINCIPAL
        btnVolverCompartidas.setOnClickListener(v -> finish());

        // CREAR NUEVA COMPRA COMPARTIDA
        btnNuevaCompartida.setOnClickListener(v -> {

            if (aplicacion.requiereAutenticacion()) {
                return;
            }

            Intent intent = new Intent(
                    this,
                    NuevaCompraCompartidaActivity.class
            );

            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!aplicacion.requiereAutenticacion()) {
            cargarHistorial();
        }
    }

    // =====================================
    // CARGAR COMPRAS COMPARTIDAS
    // =====================================

    private void cargarHistorial() {

        contenedorHistorialCompartidas.removeAllViews();

        List<String[]> compras =
                repository.obtenerComprasCompartidas();

        if (compras.isEmpty()) {

            TextView mensaje = crearTexto(
                    "Todavía no existen compras compartidas.",
                    16,
                    Color.DKGRAY
            );

            contenedorHistorialCompartidas.addView(mensaje);
            return;
        }

        for (String[] compra : compras) {

            long compraId = Long.parseLong(compra[0]);
            String descripcion = compra[1];
            long fecha = Long.parseLong(compra[2]);
            long total = Long.parseLong(compra[3]);
            String cuenta = compra[4];
            String cantidadContactos = compra[5];
            long pendiente = Long.parseLong(compra[6]);

            LinearLayout tarjeta = new LinearLayout(this);
            tarjeta.setOrientation(LinearLayout.VERTICAL);
            tarjeta.setPadding(
                    dp(16),
                    dp(16),
                    dp(16),
                    dp(16)
            );
            tarjeta.setBackgroundColor(Color.WHITE);

            LinearLayout.LayoutParams parametros =
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );

            parametros.bottomMargin = dp(18);
            tarjeta.setLayoutParams(parametros);

            TextView titulo = crearTexto(
                    descripcion,
                    18,
                    Color.rgb(23, 32, 51)
            );

            titulo.setTypeface(null, Typeface.BOLD);
            tarjeta.addView(titulo);

            tarjeta.addView(crearTexto(
                    "Fecha: " + formatoFecha.format(new Date(fecha)),
                    14,
                    Color.DKGRAY
            ));

            tarjeta.addView(crearTexto(
                    "Cuenta: " + cuenta,
                    14,
                    Color.DKGRAY
            ));

            tarjeta.addView(crearTexto(
                    "Total: $" + formatoMoneda.format(total),
                    17,
                    Color.rgb(20, 108, 67)
            ));

            tarjeta.addView(crearTexto(
                    "Contactos: " + cantidadContactos,
                    14,
                    Color.DKGRAY
            ));

            if (pendiente > 0) {

                tarjeta.addView(crearTexto(
                        "Pendiente por cobrar: $" +
                                formatoMoneda.format(pendiente),
                        15,
                        Color.rgb(160, 80, 0)
                ));

            } else {

                tarjeta.addView(crearTexto(
                        "Sin cobros pendientes",
                        15,
                        Color.rgb(20, 108, 67)
                ));
            }

            Button btnDetalle = new Button(this);
            btnDetalle.setText("Ver detalle");
            btnDetalle.setAllCaps(false);

            LinearLayout detalle = new LinearLayout(this);
            detalle.setOrientation(LinearLayout.VERTICAL);
            detalle.setVisibility(View.GONE);

            btnDetalle.setOnClickListener(v -> {

                if (detalle.getVisibility() == View.VISIBLE) {

                    detalle.setVisibility(View.GONE);
                    btnDetalle.setText("Ver detalle");

                } else {

                    if (aplicacion.requiereAutenticacion()) {
                        return;
                    }

                    mostrarDetalle(compraId, detalle);
                    detalle.setVisibility(View.VISIBLE);
                    btnDetalle.setText("Ocultar detalle");
                }
            });

            tarjeta.addView(btnDetalle);
            tarjeta.addView(detalle);

            contenedorHistorialCompartidas.addView(tarjeta);
        }
    }

    // =====================================
    // MOSTRAR PRODUCTOS Y PARTICIPANTES
    // =====================================

    private void mostrarDetalle(
            long compraId,
            LinearLayout contenedor) {

        contenedor.removeAllViews();

        TextView tituloProductos = crearTexto(
                "Productos y servicios",
                16,
                Color.BLACK
        );

        tituloProductos.setTypeface(null, Typeface.BOLD);
        contenedor.addView(tituloProductos);

        List<ComprasCompartidasRepository.Item> items =
                repository.obtenerItems(compraId);

        for (ComprasCompartidasRepository.Item item : items) {

            long subtotal =
                    (long) item.cantidad * item.precioUnitario;

            contenedor.addView(crearTexto(
                    item.descripcion +
                            " — " +
                            item.cantidad +
                            " × $" +
                            formatoMoneda.format(item.precioUnitario) +
                            " = $" +
                            formatoMoneda.format(subtotal),
                    14,
                    Color.DKGRAY
            ));
        }

        TextView tituloParticipantes = crearTexto(
                "Distribución y deudas",
                16,
                Color.BLACK
        );

        tituloParticipantes.setTypeface(null, Typeface.BOLD);
        tituloParticipantes.setPadding(
                0,
                dp(16),
                0,
                dp(8)
        );

        contenedor.addView(tituloParticipantes);

        List<String[]> participantes =
                repository.obtenerParticipantes(compraId);

        for (String[] participante : participantes) {

            String nombre = participante[0];
            long asignado = Long.parseLong(participante[1]);
            long pagado = Long.parseLong(participante[2]);
            long pendiente = Long.parseLong(participante[3]);

            boolean esPagador = "1".equals(participante[4]);

            int contactoId = -1;

            if (!esPagador
                    && participante.length > 5
                    && !participante[5].isEmpty()) {

                contactoId = Integer.parseInt(participante[5]);
            }

            String estado;

            if (pendiente == 0) {
                estado = "PAGADO";
            } else if (pagado > 0) {
                estado = "ABONO PARCIAL";
            } else {
                estado = "PENDIENTE";
            }

            LinearLayout bloqueParticipante =
                    new LinearLayout(this);

            bloqueParticipante.setOrientation(
                    LinearLayout.VERTICAL
            );

            bloqueParticipante.setPadding(
                    0,
                    dp(12),
                    0,
                    dp(12)
            );

            TextView textoParticipante = crearTexto(
                    nombre +
                            "\nAsignado: $" +
                            formatoMoneda.format(asignado) +
                            " | Pagado: $" +
                            formatoMoneda.format(pagado) +
                            "\nPendiente: $" +
                            formatoMoneda.format(pendiente) +
                            "\nEstado: " + estado,
                    14,
                    pendiente == 0
                            ? Color.rgb(20, 108, 67)
                            : Color.DKGRAY
            );

            bloqueParticipante.addView(textoParticipante);

            // BOTONES SOLO PARA CONTACTOS CON DEUDA

            if (!esPagador
                    && contactoId > 0
                    && pendiente > 0) {

                final int idContacto = contactoId;
                final long deudaPendiente = pendiente;

                Button btnAbonar = new Button(this);
                btnAbonar.setText("Registrar abono");
                btnAbonar.setAllCaps(false);

                btnAbonar.setOnClickListener(v -> {

                    if (aplicacion.requiereAutenticacion()) {
                        return;
                    }

                    mostrarDialogoAbono(
                            compraId,
                            idContacto,
                            nombre,
                            deudaPendiente
                    );
                });

                bloqueParticipante.addView(btnAbonar);

                Button btnPagarTotalidad = new Button(this);
                btnPagarTotalidad.setText(
                        "Pagar totalidad ($" +
                                formatoMoneda.format(deudaPendiente) +
                                ")"
                );
                btnPagarTotalidad.setAllCaps(false);

                btnPagarTotalidad.setOnClickListener(v -> {

                    if (aplicacion.requiereAutenticacion()) {
                        return;
                    }

                    confirmarPagoCompleto(
                            compraId,
                            idContacto,
                            nombre,
                            deudaPendiente
                    );
                });

                bloqueParticipante.addView(btnPagarTotalidad);
            }

            contenedor.addView(bloqueParticipante);
        }
    }

    // =====================================
    // DIALOGO PARA REGISTRAR ABONO
    // =====================================

    private void mostrarDialogoAbono(
            long compraId,
            int contactoId,
            String nombre,
            long pendiente) {

        EditText etMonto = new EditText(this);

        etMonto.setHint("Monto del abono en pesos");
        etMonto.setInputType(
                InputType.TYPE_CLASS_NUMBER
        );

        etMonto.setSingleLine(true);

        LinearLayout contenedor = new LinearLayout(this);
        contenedor.setOrientation(LinearLayout.VERTICAL);
        contenedor.setPadding(
                dp(20),
                dp(8),
                dp(20),
                0
        );

        contenedor.addView(crearTexto(
                "Contacto: " + nombre +
                        "\nDeuda pendiente: $" +
                        formatoMoneda.format(pendiente),
                15,
                Color.DKGRAY
        ));

        contenedor.addView(etMonto);

        AlertDialog dialogo = new AlertDialog.Builder(this)
                .setTitle("Registrar abono")
                .setView(contenedor)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Registrar", null)
                .create();

        dialogo.setOnShowListener(dialog -> {

            Button botonRegistrar =
                    dialogo.getButton(
                            AlertDialog.BUTTON_POSITIVE
                    );

            botonRegistrar.setOnClickListener(v -> {

                if (aplicacion.requiereAutenticacion()) {
                    return;
                }

                String textoMonto =
                        etMonto.getText().toString().trim();

                if (textoMonto.isEmpty()) {
                    etMonto.setError("Ingresa un monto");
                    return;
                }

                long monto;

                try {
                    monto = Long.parseLong(textoMonto);
                } catch (NumberFormatException e) {
                    etMonto.setError("Monto inválido");
                    return;
                }

                if (monto <= 0) {
                    etMonto.setError(
                            "El monto debe ser mayor que cero"
                    );
                    return;
                }

                if (monto > pendiente) {
                    etMonto.setError(
                            "No puedes superar la deuda pendiente"
                    );
                    return;
                }

                boolean resultado = repository.registrarAbono(
                        compraId,
                        contactoId,
                        monto
                );

                if (resultado) {

                    Toast.makeText(
                            this,
                            "Abono registrado correctamente",
                            Toast.LENGTH_SHORT
                    ).show();

                    dialogo.dismiss();
                    cargarHistorial();

                } else {

                    Toast.makeText(
                            this,
                            "No se pudo registrar el abono. " +
                                    "Verifica que la deuda siga pendiente.",
                            Toast.LENGTH_LONG
                    ).show();

                    cargarHistorial();
                    dialogo.dismiss();
                }
            });
        });

        dialogo.show();
    }

    // =====================================
    // CONFIRMAR PAGO COMPLETO
    // =====================================

    private void confirmarPagoCompleto(
            long compraId,
            int contactoId,
            String nombre,
            long pendiente) {

        new AlertDialog.Builder(this)
                .setTitle("Confirmar pago completo")
                .setMessage(
                        "¿Confirmas que " + nombre +
                                " te pagó la totalidad de su deuda?\n\n" +
                                "Monto: $" +
                                formatoMoneda.format(pendiente) +
                                "\n\nEl dinero se agregará a la cuenta " +
                                "asociada a esta compra."
                )
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Confirmar pago", (dialog, which) -> {

                    if (aplicacion.requiereAutenticacion()) {
                        return;
                    }

                    boolean resultado = repository.registrarAbono(
                            compraId,
                            contactoId,
                            pendiente
                    );

                    if (resultado) {

                        Toast.makeText(
                                this,
                                "Deuda pagada completamente",
                                Toast.LENGTH_SHORT
                        ).show();

                    } else {

                        Toast.makeText(
                                this,
                                "No se pudo confirmar el pago. " +
                                        "Verifica el saldo pendiente.",
                                Toast.LENGTH_LONG
                        ).show();
                    }

                    cargarHistorial();
                })
                .show();
    }

    // =====================================
    // CREAR TEXTO
    // =====================================

    private TextView crearTexto(
            String contenido,
            int tamano,
            int color) {

        TextView texto = new TextView(this);

        texto.setText(contenido);
        texto.setTextSize(tamano);
        texto.setTextColor(color);
        texto.setPadding(
                0,
                dp(6),
                0,
                dp(6)
        );

        return texto;
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
