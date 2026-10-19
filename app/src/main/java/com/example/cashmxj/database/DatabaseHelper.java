
package com.example.cashmxj.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.cashmxj.models.Cuenta;
import com.example.cashmxj.models.Movimiento;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "cashmxj.db";
    private static final int DATABASE_VERSION = 3;

    // TABLA CUENTAS
    private static final String TABLE_CUENTAS = "cuentas";

    private static final String COL_ID = "id";
    private static final String COL_NOMBRE = "nombre";
    private static final String COL_TIPO = "tipo";
    private static final String COL_SALDO = "saldo";
    private static final String COL_DESCRIPCION = "descripcion";

    // TABLA MOVIMIENTOS
    private static final String TABLE_MOVIMIENTOS = "movimientos";

    private static final String COL_CUENTA_ID = "cuenta_id";
    private static final String COL_MONTO = "monto";
    private static final String COL_CATEGORIA = "categoria";
    private static final String COL_FECHA = "fecha";

    // TABLA CONTACTOS
    private static final String TABLE_CONTACTOS = "contactos";

    private static final String COL_TELEFONO = "telefono";
    private static final String COL_CORREO = "correo";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        crearTablaCuentas(db);
        crearTablaMovimientos(db);
        crearTablaContactos(db);
    }

    private void crearTablaCuentas(SQLiteDatabase db) {

        String sql =
                "CREATE TABLE IF NOT EXISTS " + TABLE_CUENTAS + " (" +
                        COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COL_NOMBRE + " TEXT NOT NULL, " +
                        COL_TIPO + " TEXT NOT NULL, " +
                        COL_SALDO + " INTEGER NOT NULL, " +
                        COL_DESCRIPCION + " TEXT" +
                        ")";

        db.execSQL(sql);
    }

    private void crearTablaMovimientos(SQLiteDatabase db) {

        String sql =
                "CREATE TABLE IF NOT EXISTS " + TABLE_MOVIMIENTOS + " (" +
                        COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COL_CUENTA_ID + " INTEGER NOT NULL, " +
                        COL_TIPO + " TEXT NOT NULL " +
                        "CHECK (" + COL_TIPO +
                        " IN ('Ingreso', 'Gasto')), " +
                        COL_MONTO + " INTEGER NOT NULL " +
                        "CHECK (" + COL_MONTO + " > 0), " +
                        COL_CATEGORIA + " TEXT NOT NULL, " +
                        COL_DESCRIPCION + " TEXT, " +
                        COL_FECHA + " INTEGER NOT NULL, " +
                        "FOREIGN KEY (" + COL_CUENTA_ID + ") " +
                        "REFERENCES " + TABLE_CUENTAS + "(" + COL_ID + ") " +
                        "ON DELETE RESTRICT" +
                        ")";

        db.execSQL(sql);

        db.execSQL(
                "CREATE INDEX IF NOT EXISTS idx_movimientos_cuenta " +
                        "ON " + TABLE_MOVIMIENTOS +
                        "(" + COL_CUENTA_ID + ")"
        );
    }

    private void crearTablaContactos(SQLiteDatabase db) {

        String sql =
                "CREATE TABLE IF NOT EXISTS " + TABLE_CONTACTOS + " (" +
                        COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COL_NOMBRE + " TEXT NOT NULL, " +
                        COL_TELEFONO + " TEXT, " +
                        COL_CORREO + " TEXT" +
                        ")";

        db.execSQL(sql);
    }

    // MIGRACION SIN BORRAR DATOS
    @Override
    public void onUpgrade(
            SQLiteDatabase db,
            int oldVersion,
            int newVersion) {

        if (oldVersion < 2) {
            crearTablaMovimientos(db);
        }

        if (oldVersion < 3) {
            crearTablaContactos(db);
        }
    }

    // =====================================
    // CUENTAS - INSERTAR
    // =====================================

    public long insertarCuenta(Cuenta cuenta) {

        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COL_NOMBRE, cuenta.getNombre());
        values.put(COL_TIPO, cuenta.getTipo());
        values.put(COL_SALDO, cuenta.getSaldo());
        values.put(COL_DESCRIPCION, cuenta.getDescripcion());

        return db.insert(
                TABLE_CUENTAS,
                null,
                values
        );
    }

    // =====================================
    // CUENTAS - OBTENER TODAS
    // =====================================

    public List<Cuenta> obtenerCuentas() {

        List<Cuenta> cuentas = new ArrayList<>();

        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.query(
                TABLE_CUENTAS,
                null,
                null,
                null,
                null,
                null,
                COL_ID + " DESC"
        );

        try {

            while (cursor.moveToNext()) {

                Cuenta cuenta = new Cuenta(
                        cursor.getInt(
                                cursor.getColumnIndexOrThrow(COL_ID)
                        ),
                        cursor.getString(
                                cursor.getColumnIndexOrThrow(COL_NOMBRE)
                        ),
                        cursor.getString(
                                cursor.getColumnIndexOrThrow(COL_TIPO)
                        ),
                        cursor.getLong(
                                cursor.getColumnIndexOrThrow(COL_SALDO)
                        ),
                        cursor.getString(
                                cursor.getColumnIndexOrThrow(COL_DESCRIPCION)
                        )
                );

                cuentas.add(cuenta);
            }

        } finally {
            cursor.close();
        }

        return cuentas;
    }

    // =====================================
    // CUENTAS - BUSCAR POR ID
    // =====================================

    public Cuenta obtenerCuentaPorId(int id) {

        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.query(
                TABLE_CUENTAS,
                null,
                COL_ID + " = ?",
                new String[]{String.valueOf(id)},
                null,
                null,
                null
        );

        try {

            if (cursor.moveToFirst()) {

                return new Cuenta(
                        cursor.getInt(
                                cursor.getColumnIndexOrThrow(COL_ID)
                        ),
                        cursor.getString(
                                cursor.getColumnIndexOrThrow(COL_NOMBRE)
                        ),
                        cursor.getString(
                                cursor.getColumnIndexOrThrow(COL_TIPO)
                        ),
                        cursor.getLong(
                                cursor.getColumnIndexOrThrow(COL_SALDO)
                        ),
                        cursor.getString(
                                cursor.getColumnIndexOrThrow(COL_DESCRIPCION)
                        )
                );
            }

        } finally {
            cursor.close();
        }

        return null;
    }

    // =====================================
    // CUENTAS - ACTUALIZAR
    // =====================================

    public int actualizarCuenta(Cuenta cuenta) {

        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COL_NOMBRE, cuenta.getNombre());
        values.put(COL_TIPO, cuenta.getTipo());
        values.put(COL_SALDO, cuenta.getSaldo());
        values.put(COL_DESCRIPCION, cuenta.getDescripcion());

        return db.update(
                TABLE_CUENTAS,
                values,
                COL_ID + " = ?",
                new String[]{
                        String.valueOf(cuenta.getId())
                }
        );
    }

    // =====================================
    // CUENTAS - ELIMINAR
    // =====================================

    public int eliminarCuenta(int id) {

        SQLiteDatabase db = getWritableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM " + TABLE_MOVIMIENTOS +
                        " WHERE " + COL_CUENTA_ID + " = ?",
                new String[]{String.valueOf(id)}
        );

        try {

            if (cursor.moveToFirst() && cursor.getInt(0) > 0) {
                return -1;
            }

        } finally {
            cursor.close();
        }

        return db.delete(
                TABLE_CUENTAS,
                COL_ID + " = ?",
                new String[]{
                        String.valueOf(id)
                }
        );
    }

    // =====================================
    // CUENTAS - SALDO TOTAL
    // =====================================

    public long obtenerSaldoTotal() {

        long total = 0;

        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT SUM(" + COL_SALDO + ") FROM " + TABLE_CUENTAS,
                null
        );

        try {

            if (cursor.moveToFirst() && !cursor.isNull(0)) {
                total = cursor.getLong(0);
            }

        } finally {
            cursor.close();
        }

        return total;
    }

    // =====================================
    // MOVIMIENTOS - INSERTAR Y ACTUALIZAR SALDO
    // =====================================

    public long insertarMovimiento(Movimiento movimiento) {

        if (movimiento == null || movimiento.getMonto() <= 0) {
            return -1;
        }

        String tipo = movimiento.getTipo();

        if (!"Ingreso".equals(tipo) && !"Gasto".equals(tipo)) {
            return -1;
        }

        if (movimiento.getCategoria() == null ||
                movimiento.getCategoria().trim().isEmpty()) {
            return -1;
        }

        SQLiteDatabase db = getWritableDatabase();

        db.beginTransaction();

        try {

            long saldoActual;

            Cursor cursor = db.query(
                    TABLE_CUENTAS,
                    new String[]{COL_SALDO},
                    COL_ID + " = ?",
                    new String[]{
                            String.valueOf(movimiento.getCuentaId())
                    },
                    null,
                    null,
                    null
            );

            try {

                if (!cursor.moveToFirst()) {
                    return -1;
                }

                saldoActual = cursor.getLong(
                        cursor.getColumnIndexOrThrow(COL_SALDO)
                );

            } finally {
                cursor.close();
            }

            long nuevoSaldo;

            try {

                if ("Ingreso".equals(tipo)) {

                    nuevoSaldo = Math.addExact(
                            saldoActual,
                            movimiento.getMonto()
                    );

                } else {

                    nuevoSaldo = Math.subtractExact(
                            saldoActual,
                            movimiento.getMonto()
                    );
                }

            } catch (ArithmeticException e) {
                return -1;
            }

            // No permitir gastos superiores al saldo disponible.
            if ("Gasto".equals(tipo) && nuevoSaldo < 0) {
                return -1;
            }

            ContentValues valuesMovimiento = new ContentValues();

            valuesMovimiento.put(
                    COL_CUENTA_ID,
                    movimiento.getCuentaId()
            );

            valuesMovimiento.put(
                    COL_TIPO,
                    movimiento.getTipo()
            );

            valuesMovimiento.put(
                    COL_MONTO,
                    movimiento.getMonto()
            );

            valuesMovimiento.put(
                    COL_CATEGORIA,
                    movimiento.getCategoria()
            );

            valuesMovimiento.put(
                    COL_DESCRIPCION,
                    movimiento.getDescripcion()
            );

            valuesMovimiento.put(
                    COL_FECHA,
                    movimiento.getFecha()
            );

            long resultado = db.insert(
                    TABLE_MOVIMIENTOS,
                    null,
                    valuesMovimiento
            );

            if (resultado == -1) {
                return -1;
            }

            ContentValues valuesCuenta = new ContentValues();

            valuesCuenta.put(COL_SALDO, nuevoSaldo);

            int cuentasActualizadas = db.update(
                    TABLE_CUENTAS,
                    valuesCuenta,
                    COL_ID + " = ?",
                    new String[]{
                            String.valueOf(movimiento.getCuentaId())
                    }
            );

            if (cuentasActualizadas != 1) {
                return -1;
            }

            db.setTransactionSuccessful();

            return resultado;

        } finally {
            db.endTransaction();
        }
    }

    // =====================================
    // MOVIMIENTOS - OBTENER TODOS
    // =====================================

    public List<Movimiento> obtenerMovimientos() {

        List<Movimiento> movimientos = new ArrayList<>();

        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.query(
                TABLE_MOVIMIENTOS,
                null,
                null,
                null,
                null,
                null,
                COL_FECHA + " DESC, " + COL_ID + " DESC"
        );

        try {

            while (cursor.moveToNext()) {

                Movimiento movimiento = new Movimiento(
                        cursor.getInt(
                                cursor.getColumnIndexOrThrow(COL_ID)
                        ),
                        cursor.getInt(
                                cursor.getColumnIndexOrThrow(COL_CUENTA_ID)
                        ),
                        cursor.getString(
                                cursor.getColumnIndexOrThrow(COL_TIPO)
                        ),
                        cursor.getLong(
                                cursor.getColumnIndexOrThrow(COL_MONTO)
                        ),
                        cursor.getString(
                                cursor.getColumnIndexOrThrow(COL_CATEGORIA)
                        ),
                        cursor.getString(
                                cursor.getColumnIndexOrThrow(COL_DESCRIPCION)
                        ),
                        cursor.getLong(
                                cursor.getColumnIndexOrThrow(COL_FECHA)
                        )
                );

                movimientos.add(movimiento);
            }

        } finally {
            cursor.close();
        }

        return movimientos;
    }

    // =====================================
    // CONTACTOS - INSERTAR
    // =====================================

    public long insertarContacto(
            String nombre,
            String telefono,
            String correo) {

        if (nombre == null || nombre.trim().isEmpty()) {
            return -1;
        }

        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COL_NOMBRE, nombre.trim());
        values.put(COL_TELEFONO, telefono == null ? "" : telefono.trim());
        values.put(COL_CORREO, correo == null ? "" : correo.trim());

        return db.insert(
                TABLE_CONTACTOS,
                null,
                values
        );
    }

    // =====================================
    // CONTACTOS - OBTENER TODOS
    // =====================================

    public List<String[]> obtenerContactos() {

        List<String[]> contactos = new ArrayList<>();

        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.query(
                TABLE_CONTACTOS,
                null,
                null,
                null,
                null,
                null,
                COL_NOMBRE + " COLLATE NOCASE ASC"
        );

        try {

            while (cursor.moveToNext()) {

                String[] contacto = new String[]{
                        String.valueOf(
                                cursor.getInt(
                                        cursor.getColumnIndexOrThrow(COL_ID)
                                )
                        ),
                        cursor.getString(
                                cursor.getColumnIndexOrThrow(COL_NOMBRE)
                        ),
                        cursor.getString(
                                cursor.getColumnIndexOrThrow(COL_TELEFONO)
                        ),
                        cursor.getString(
                                cursor.getColumnIndexOrThrow(COL_CORREO)
                        )
                };

                contactos.add(contacto);
            }

        } finally {
            cursor.close();
        }

        return contactos;
    }

    // =====================================
    // CONTACTOS - BUSCAR POR ID
    // =====================================

    public String[] obtenerContactoPorId(int id) {

        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.query(
                TABLE_CONTACTOS,
                null,
                COL_ID + " = ?",
                new String[]{String.valueOf(id)},
                null,
                null,
                null
        );

        try {

            if (cursor.moveToFirst()) {

                return new String[]{
                        String.valueOf(
                                cursor.getInt(
                                        cursor.getColumnIndexOrThrow(COL_ID)
                                )
                        ),
                        cursor.getString(
                                cursor.getColumnIndexOrThrow(COL_NOMBRE)
                        ),
                        cursor.getString(
                                cursor.getColumnIndexOrThrow(COL_TELEFONO)
                        ),
                        cursor.getString(
                                cursor.getColumnIndexOrThrow(COL_CORREO)
                        )
                };
            }

        } finally {
            cursor.close();
        }

        return null;
    }

    // =====================================
    // CONTACTOS - ACTUALIZAR
    // =====================================

    public int actualizarContacto(
            int id,
            String nombre,
            String telefono,
            String correo) {

        if (nombre == null || nombre.trim().isEmpty()) {
            return -1;
        }

        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COL_NOMBRE, nombre.trim());
        values.put(COL_TELEFONO, telefono == null ? "" : telefono.trim());
        values.put(COL_CORREO, correo == null ? "" : correo.trim());

        return db.update(
                TABLE_CONTACTOS,
                values,
                COL_ID + " = ?",
                new String[]{String.valueOf(id)}
        );
    }

    // =====================================
    // CONTACTOS - ELIMINAR
    // =====================================

    public int eliminarContacto(int id) {

        SQLiteDatabase db = getWritableDatabase();

        return db.delete(
                TABLE_CONTACTOS,
                COL_ID + " = ?",
                new String[]{String.valueOf(id)}
        );
    }
}
