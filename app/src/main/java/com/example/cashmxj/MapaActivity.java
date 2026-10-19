
package com.example.cashmxj;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.InputType;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.cashmxj.database.DatabaseHelper;
import com.example.cashmxj.database.LugaresRepository;

import org.maplibre.android.MapLibre;
import org.maplibre.android.annotations.Icon;
import org.maplibre.android.annotations.IconFactory;
import org.maplibre.android.annotations.Marker;
import org.maplibre.android.annotations.MarkerOptions;
import org.maplibre.android.camera.CameraPosition;
import org.maplibre.android.camera.CameraUpdateFactory;
import org.maplibre.android.geometry.LatLng;
import org.maplibre.android.maps.MapView;
import org.maplibre.android.maps.MapLibreMap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapaActivity extends AppCompatActivity {

    private static final int PERMISO_UBICACION = 101;

    private static final String ESTILO_MAPA =
            "https://tiles.openfreemap.org/styles/liberty";

    private final String[] emojis = {
            "📍", "🏠", "💼", "🛒",
            "🍔", "☕", "🚗", "❤️",
            "🏥", "🎓", "⭐", "🎉"
    };

    private MapView mapa;
    private MapLibreMap mapaLibre;

    private DatabaseHelper helper;
    private LugaresRepository repositorio;

    private TextView contador;
    private LinearLayout contenedor;

    private LocationManager locationManager;
    private Marker marcadorUbicacion;

    private final Map<Long, LugaresRepository.Lugar>
            lugaresPorMarcador = new HashMap<>();

    @Override
    protected void onCreate(Bundle estado) {
        super.onCreate(estado);

        MapLibre.getInstance(this);

        setContentView(R.layout.activity_mapa);

        helper = new DatabaseHelper(this);
        repositorio = new LugaresRepository(helper);

        locationManager =
                (LocationManager) getSystemService(LOCATION_SERVICE);

        mapa = findViewById(R.id.mapaLugares);
        contador = findViewById(R.id.tvCantidadLugares);
        contenedor = findViewById(R.id.contenedorLugares);

        findViewById(R.id.btnVolverMapa)
                .setOnClickListener(v -> finish());

        findViewById(R.id.btnMiUbicacion)
                .setOnClickListener(v -> localizarUsuario());

        mapa.onCreate(estado);

        actualizar();

        mapa.getMapAsync(map -> {
            mapaLibre = map;
            configurarMapa();
        });
    }

    private void configurarMapa() {

        CameraPosition posicionInicial =
                new CameraPosition.Builder()
                        .target(new LatLng(-33.4489, -70.6693))
                        .zoom(12.0)
                        .build();

        mapaLibre.setCameraPosition(posicionInicial);

        mapaLibre.setStyle(ESTILO_MAPA, style -> actualizar());

        mapaLibre.addOnMapLongClickListener(punto -> {
            mostrarFormulario(null, punto);
            return true;
        });

        mapaLibre.setOnMarkerClickListener(marcador -> {

            if (marcadorUbicacion != null
                    && marcador.getId() == marcadorUbicacion.getId()) {
                Toast.makeText(
                        this,
                        "Esta es tu ubicación",
                        Toast.LENGTH_SHORT
                ).show();
                return true;
            }

            LugaresRepository.Lugar lugar =
                    lugaresPorMarcador.get(marcador.getId());

            if (lugar != null) {
                mostrarAcciones(lugar);
            }

            return true;
        });
    }

    private boolean tienePermisoUbicacion() {

        return ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
                ||
                ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED;
    }

    private void localizarUsuario() {

        if (mapaLibre == null) {
            Toast.makeText(
                    this,
                    "El mapa todavía está cargando",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        if (!tienePermisoUbicacion()) {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    },
                    PERMISO_UBICACION
            );

            return;
        }

        obtenerUbicacion();
    }

    @SuppressLint("MissingPermission")
    private void obtenerUbicacion() {

        if (!tienePermisoUbicacion()) {
            return;
        }

        if (locationManager == null) {
            mostrarErrorUbicacion();
            return;
        }

        boolean gpsActivo = false;
        boolean redActiva = false;

        try {
            gpsActivo = locationManager.isProviderEnabled(
                    LocationManager.GPS_PROVIDER
            );
        } catch (Exception ignored) {
        }

        try {
            redActiva = locationManager.isProviderEnabled(
                    LocationManager.NETWORK_PROVIDER
            );
        } catch (Exception ignored) {
        }

        if (!gpsActivo && !redActiva) {
            Toast.makeText(
                    this,
                    "Activa la ubicación del teléfono",
                    Toast.LENGTH_LONG
            ).show();
            return;
        }

        String proveedor = gpsActivo
                ? LocationManager.GPS_PROVIDER
                : LocationManager.NETWORK_PROVIDER;

        Toast.makeText(
                this,
                "Buscando tu ubicación...",
                Toast.LENGTH_SHORT
        ).show();

        try {

            locationManager.getCurrentLocation(
                    proveedor,
                    null,
                    getMainExecutor(),
                    ubicacion -> {

                        if (isFinishing() || isDestroyed()) {
                            return;
                        }

                        if (ubicacion != null) {
                            mostrarUbicacion(ubicacion);
                        } else {
                            mostrarErrorUbicacion();
                        }
                    }
            );

        } catch (SecurityException e) {
            mostrarErrorUbicacion();
        } catch (Exception e) {
            mostrarErrorUbicacion();
        }
    }

    private void mostrarUbicacion(Location ubicacion) {

        if (mapaLibre == null) {
            return;
        }

        LatLng punto = new LatLng(
                ubicacion.getLatitude(),
                ubicacion.getLongitude()
        );

        if (marcadorUbicacion != null) {
            mapaLibre.removeMarker(marcadorUbicacion);
        }

        Icon icono = IconFactory.getInstance(this)
                .fromBitmap(crearPuntoAzul());

        marcadorUbicacion = mapaLibre.addMarker(
                new MarkerOptions()
                        .position(punto)
                        .title("Mi ubicación")
                        .icon(icono)
        );

        mapaLibre.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                        punto,
                        16.0
                ),
                1200
        );

        Toast.makeText(
                this,
                "Ubicación encontrada",
                Toast.LENGTH_SHORT
        ).show();
    }

    private Bitmap crearPuntoAzul() {

        int tamano = dp(38);

        Bitmap bitmap = Bitmap.createBitmap(
                tamano,
                tamano,
                Bitmap.Config.ARGB_8888
        );

        Canvas canvas = new Canvas(bitmap);

        Paint pintura = new Paint(Paint.ANTI_ALIAS_FLAG);

        float centro = tamano / 2f;

        pintura.setColor(android.graphics.Color.WHITE);
        canvas.drawCircle(
                centro,
                centro,
                tamano * 0.38f,
                pintura
        );

        pintura.setColor(
                android.graphics.Color.rgb(33, 150, 243)
        );

        canvas.drawCircle(
                centro,
                centro,
                tamano * 0.29f,
                pintura
        );

        return bitmap;
    }

    private void mostrarErrorUbicacion() {

        Toast.makeText(
                this,
                "No se pudo obtener tu ubicación. "
                        + "Comprueba el GPS e inténtalo nuevamente.",
                Toast.LENGTH_LONG
        ).show();
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults
    ) {

        super.onRequestPermissionsResult(
                requestCode,
                permissions,
                grantResults
        );

        if (requestCode == PERMISO_UBICACION) {

            if (tienePermisoUbicacion()) {
                obtenerUbicacion();
            } else {
                Toast.makeText(
                        this,
                        "Permiso de ubicación denegado",
                        Toast.LENGTH_LONG
                ).show();
            }
        }
    }

    private void actualizar() {

        List<LugaresRepository.Lugar> lugares =
                repositorio.listar();

        contador.setText(
                lugares.size() + " lugares guardados"
        );

        contenedor.removeAllViews();

        if (mapaLibre != null) {
            mapaLibre.clear();
            lugaresPorMarcador.clear();
            marcadorUbicacion = null;
        }

        for (LugaresRepository.Lugar lugar : lugares) {

            agregarFilaLugar(lugar);

            if (mapaLibre != null) {
                agregarMarcador(lugar);
            }
        }
    }

    private void agregarMarcador(
            LugaresRepository.Lugar lugar
    ) {

        Icon icono = IconFactory.getInstance(this)
                .fromBitmap(crearIcono(lugar.emoji));

        MarkerOptions opciones =
                new MarkerOptions()
                        .position(
                                new LatLng(
                                        lugar.latitud,
                                        lugar.longitud
                                )
                        )
                        .title(
                                lugar.emoji + " " + lugar.nombre
                        )
                        .snippet(
                                lugar.descripcion == null
                                        ? ""
                                        : lugar.descripcion
                        )
                        .icon(icono);

        Marker marcador = mapaLibre.addMarker(opciones);

        lugaresPorMarcador.put(
                marcador.getId(),
                lugar
        );
    }

    private Bitmap crearIcono(String emoji) {

        int tamano = dp(64);

        Bitmap bitmap = Bitmap.createBitmap(
                tamano,
                tamano,
                Bitmap.Config.ARGB_8888
        );

        Canvas canvas = new Canvas(bitmap);

        Paint pintura = new Paint(Paint.ANTI_ALIAS_FLAG);

        float centro = tamano / 2f;

        pintura.setColor(android.graphics.Color.WHITE);
        pintura.setStyle(Paint.Style.FILL);

        canvas.drawCircle(
                centro,
                centro,
                tamano * 0.43f,
                pintura
        );

        pintura.setColor(getColor(R.color.cash_primary));
        pintura.setStyle(Paint.Style.STROKE);
        pintura.setStrokeWidth(tamano * 0.055f);

        canvas.drawCircle(
                centro,
                centro,
                tamano * 0.43f,
                pintura
        );

        pintura.setStyle(Paint.Style.FILL);
        pintura.setTypeface(Typeface.DEFAULT);
        pintura.setTextAlign(Paint.Align.CENTER);
        pintura.setTextSize(tamano * 0.46f);

        String texto =
                emoji == null || emoji.isEmpty()
                        ? "📍"
                        : emoji;

        canvas.drawText(
                texto,
                centro,
                centro -
                        (pintura.ascent()
                                + pintura.descent()) / 2f,
                pintura
        );

        return bitmap;
    }

    private void agregarFilaLugar(
            LugaresRepository.Lugar lugar
    ) {

        TextView fila = new TextView(this);

        fila.setText(
                lugar.emoji
                        + "  "
                        + lugar.nombre
                        + "     ›"
        );

        fila.setTextSize(15);
        fila.setTextColor(
                getColor(R.color.cash_text_primary)
        );

        fila.setPadding(
                dp(8),
                dp(14),
                dp(8),
                dp(14)
        );

        fila.setOnClickListener(v -> {

            if (mapaLibre != null) {

                mapaLibre.animateCamera(
                        CameraUpdateFactory.newLatLngZoom(
                                new LatLng(
                                        lugar.latitud,
                                        lugar.longitud
                                ),
                                15.0
                        )
                );
            }

            mostrarAcciones(lugar);
        });

        contenedor.addView(fila);
    }

    private void mostrarAcciones(
            LugaresRepository.Lugar lugar
    ) {

        String descripcion =
                lugar.descripcion == null
                        || lugar.descripcion.trim().isEmpty()
                        ? "Sin descripción"
                        : lugar.descripcion;

        new AlertDialog.Builder(this)
                .setTitle(
                        lugar.emoji + " " + lugar.nombre
                )
                .setMessage(descripcion)
                .setPositiveButton(
                        "Editar",
                        (dialogo, opcion) ->
                                mostrarFormulario(
                                        lugar,
                                        new LatLng(
                                                lugar.latitud,
                                                lugar.longitud
                                        )
                                )
                )
                .setNeutralButton(
                        "Eliminar",
                        (dialogo, opcion) ->
                                confirmarEliminacion(lugar)
                )
                .setNegativeButton(
                        "Cerrar",
                        null
                )
                .show();
    }

    private void confirmarEliminacion(
            LugaresRepository.Lugar lugar
    ) {

        new AlertDialog.Builder(this)
                .setTitle("Eliminar lugar")
                .setMessage(
                        "¿Eliminar «"
                                + lugar.nombre
                                + "»? Esta acción no se puede deshacer."
                )
                .setNegativeButton(
                        "Cancelar",
                        null
                )
                .setPositiveButton(
                        "Eliminar",
                        (dialogo, opcion) -> {

                            boolean eliminado =
                                    repositorio.eliminar(lugar.id);

                            if (eliminado) {

                                actualizar();

                                Toast.makeText(
                                        this,
                                        "Lugar eliminado",
                                        Toast.LENGTH_SHORT
                                ).show();

                            } else {

                                Toast.makeText(
                                        this,
                                        "No se pudo eliminar el lugar",
                                        Toast.LENGTH_LONG
                                ).show();
                            }
                        }
                )
                .show();
    }

    private void mostrarFormulario(
            LugaresRepository.Lugar original,
            LatLng punto
    ) {

        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);

        panel.setPadding(
                dp(18),
                dp(8),
                dp(18),
                0
        );

        EditText nombre = new EditText(this);

        nombre.setHint("Nombre del lugar");
        nombre.setSingleLine(true);

        nombre.setInputType(
                InputType.TYPE_CLASS_TEXT
                        | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
        );

        EditText descripcion = new EditText(this);

        descripcion.setHint(
                "Descripción (opcional)"
        );

        descripcion.setInputType(
                InputType.TYPE_CLASS_TEXT
                        | InputType.TYPE_TEXT_FLAG_MULTI_LINE
                        | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
        );

        descripcion.setMinLines(2);

        Spinner selector = new Spinner(this);

        ArrayAdapter<String> adaptador =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_item,
                        emojis
                );

        adaptador.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        selector.setAdapter(adaptador);

        panel.addView(nombre);
        panel.addView(descripcion);
        panel.addView(selector);

        if (original != null) {

            nombre.setText(original.nombre);

            descripcion.setText(
                    original.descripcion == null
                            ? ""
                            : original.descripcion
            );

            for (int i = 0; i < emojis.length; i++) {

                if (emojis[i].equals(original.emoji)) {
                    selector.setSelection(i);
                    break;
                }
            }
        }

        AlertDialog dialogo =
                new AlertDialog.Builder(this)
                        .setTitle(
                                original == null
                                        ? "Nuevo lugar"
                                        : "Editar lugar"
                        )
                        .setView(panel)
                        .setNegativeButton(
                                "Cancelar",
                                null
                        )
                        .setPositiveButton(
                                "Guardar",
                                null
                        )
                        .create();

        dialogo.setOnShowListener(v -> {

            dialogo.getButton(
                    AlertDialog.BUTTON_POSITIVE
            ).setOnClickListener(boton -> {

                String titulo =
                        nombre.getText()
                                .toString()
                                .trim();

                if (titulo.isEmpty()) {
                    nombre.setError(
                            "Escribe un nombre"
                    );
                    nombre.requestFocus();
                    return;
                }

                String textoDescripcion =
                        descripcion.getText()
                                .toString()
                                .trim();

                String emojiSeleccionado =
                        emojis[
                                selector.getSelectedItemPosition()
                                ];

                LugaresRepository.Lugar lugar =
                        new LugaresRepository.Lugar(
                                original == null
                                        ? 0
                                        : original.id,
                                titulo,
                                textoDescripcion,
                                emojiSeleccionado,
                                punto.getLatitude(),
                                punto.getLongitude()
                        );

                boolean guardado =
                        repositorio.guardar(lugar);

                if (!guardado) {

                    Toast.makeText(
                            this,
                            "No se pudo guardar el lugar.",
                            Toast.LENGTH_LONG
                    ).show();

                    return;
                }

                dialogo.dismiss();

                actualizar();

                Toast.makeText(
                        this,
                        original == null
                                ? "Lugar agregado correctamente"
                                : "Lugar actualizado correctamente",
                        Toast.LENGTH_SHORT
                ).show();
            });
        });

        dialogo.show();
    }

    private int dp(int valor) {

        return Math.round(
                valor *
                        getResources()
                                .getDisplayMetrics()
                                .density
        );
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (mapa != null) {
            mapa.onStart();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mapa != null) {
            mapa.onResume();
        }
    }

    @Override
    protected void onPause() {

        if (mapa != null) {
            mapa.onPause();
        }

        super.onPause();
    }

    @Override
    protected void onStop() {

        if (mapa != null) {
            mapa.onStop();
        }

        super.onStop();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();

        if (mapa != null) {
            mapa.onLowMemory();
        }
    }

    @Override
    protected void onSaveInstanceState(
            @NonNull Bundle estado
    ) {
        super.onSaveInstanceState(estado);

        if (mapa != null) {
            mapa.onSaveInstanceState(estado);
        }
    }

    @Override
    protected void onDestroy() {

        if (mapa != null) {
            mapa.onDestroy();
        }

        if (helper != null) {
            helper.close();
        }

        super.onDestroy();
    }
}
