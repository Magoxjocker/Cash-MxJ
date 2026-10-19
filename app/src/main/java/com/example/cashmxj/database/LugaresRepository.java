
package com.example.cashmxj.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import java.util.ArrayList;
import java.util.List;

public class LugaresRepository {

    private static final String TABLA = "lugares_mapa";

    private final DatabaseHelper helper;

    public LugaresRepository(DatabaseHelper helper) {
        this.helper = helper;
    }

    // MODELO DE DATOS DE CADA MARCADOR
    public static class Lugar {

        public long id;
        public String nombre;
        public String descripcion;
        public String emoji;
        public double latitud;
        public double longitud;

        public Lugar(
                long id,
                String nombre,
                String descripcion,
                String emoji,
                double latitud,
                double longitud
        ) {
            this.id = id;
            this.nombre = nombre;
            this.descripcion = descripcion;
            this.emoji = emoji;
            this.latitud = latitud;
            this.longitud = longitud;
        }
    }

    // CREAR TABLA SI TODAVIA NO EXISTE
    private void asegurarTabla(SQLiteDatabase db) {

        db.execSQL(
                "CREATE TABLE IF NOT EXISTS " + TABLA + " (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "nombre TEXT NOT NULL, " +
                        "descripcion TEXT NOT NULL DEFAULT '', " +
                        "emoji TEXT NOT NULL DEFAULT '📍', " +
                        "latitud REAL NOT NULL, " +
                        "longitud REAL NOT NULL" +
                        ")"
        );
    }

    // CONSULTAR TODOS LOS LUGARES
    public List<Lugar> listar() {

        List<Lugar> lugares = new ArrayList<>();

        try {
            SQLiteDatabase db = helper.getWritableDatabase();

            asegurarTabla(db);

            try (Cursor cursor = db.query(
                    TABLA,
                    new String[]{
                            "id",
                            "nombre",
                            "descripcion",
                            "emoji",
                            "latitud",
                            "longitud"
                    },
                    null,
                    null,
                    null,
                    null,
                    "id DESC"
            )) {

                while (cursor.moveToNext()) {

                    Lugar lugar = new Lugar(
                            cursor.getLong(0),
                            cursor.getString(1),
                            cursor.getString(2),
                            cursor.getString(3),
                            cursor.getDouble(4),
                            cursor.getDouble(5)
                    );

                    lugares.add(lugar);
                }
            }

        } catch (SQLiteException e) {
            android.util.Log.e(
                    "LugaresRepository",
                    "Error al consultar lugares",
                    e
            );
        }

        return lugares;
    }

    // GUARDAR NUEVO LUGAR O ACTUALIZAR EXISTENTE
    public boolean guardar(Lugar lugar) {

        if (lugar == null ||
                lugar.nombre == null ||
                lugar.nombre.trim().isEmpty()) {
            return false;
        }

        if (!Double.isFinite(lugar.latitud) ||
                !Double.isFinite(lugar.longitud) ||
                lugar.latitud < -90 ||
                lugar.latitud > 90 ||
                lugar.longitud < -180 ||
                lugar.longitud > 180) {
            return false;
        }

        try {
            SQLiteDatabase db = helper.getWritableDatabase();

            asegurarTabla(db);

            ContentValues valores = new ContentValues();

            valores.put("nombre", lugar.nombre.trim());

            valores.put(
                    "descripcion",
                    lugar.descripcion == null
                            ? ""
                            : lugar.descripcion.trim()
            );

            valores.put(
                    "emoji",
                    lugar.emoji == null ||
                            lugar.emoji.isEmpty()
                            ? "📍"
                            : lugar.emoji
            );

            valores.put("latitud", lugar.latitud);
            valores.put("longitud", lugar.longitud);

            // ACTUALIZAR MARCADOR EXISTENTE
            if (lugar.id > 0) {

                int actualizados = db.update(
                        TABLA,
                        valores,
                        "id = ?",
                        new String[]{
                                String.valueOf(lugar.id)
                        }
                );

                return actualizados == 1;
            }

            // INSERTAR NUEVO MARCADOR
            // SIN LIMITE FIJO DE CANTIDAD
            long nuevoId = db.insert(
                    TABLA,
                    null,
                    valores
            );

            if (nuevoId == -1) {
                return false;
            }

            lugar.id = nuevoId;

            return true;

        } catch (SQLiteException e) {

            android.util.Log.e(
                    "LugaresRepository",
                    "Error al guardar lugar",
                    e
            );

            return false;
        }
    }

    // ELIMINAR UN MARCADOR POR SU ID
    public boolean eliminar(long id) {

        if (id <= 0) {
            return false;
        }

        try {
            SQLiteDatabase db = helper.getWritableDatabase();

            asegurarTabla(db);

            int eliminados = db.delete(
                    TABLA,
                    "id = ?",
                    new String[]{
                            String.valueOf(id)
                    }
            );

            return eliminados == 1;

        } catch (SQLiteException e) {

            android.util.Log.e(
                    "LugaresRepository",
                    "Error al eliminar lugar",
                    e
            );

            return false;
        }
    }
}
