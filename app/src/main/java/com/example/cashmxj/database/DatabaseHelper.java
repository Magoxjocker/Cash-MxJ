
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
    private static final int DATABASE_VERSION = 6;

    // CUENTAS
    private static final String TABLE_CUENTAS = "cuentas";

    private static final String COL_ID = "id";
    private static final String COL_NOMBRE = "nombre";
    private static final String COL_TIPO = "tipo";
    private static final String COL_SALDO = "saldo";
    private static final String COL_DESCRIPCION = "descripcion";

    // MOVIMIENTOS
    private static final String TABLE_MOVIMIENTOS = "movimientos";

    private static final String COL_CUENTA_ID = "cuenta_id";
    private static final String COL_MONTO = "monto";
    private static final String COL_CATEGORIA = "categoria";
    private static final String COL_FECHA = "fecha";

    // CONTACTOS
    private static final String TABLE_CONTACTOS = "contactos";

    private static final String COL_TELEFONO = "telefono";
    private static final String COL_CORREO = "correo";

    // COMPRAS
    private static final String TABLE_COMPRAS = "compras";

    private static final String COL_CONTACTO_ID = "contacto_id";
    private static final String COL_ESTADO = "estado";
    private static final String COL_MOVIMIENTO_ID = "movimiento_id";

    public static final String COMPRA_REALIZADA = "REALIZADA";
    public static final String COMPRA_PROGRAMADA = "PROGRAMADA";

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
        crearTablaCompras(db);

        CompartidasSchema.crearTablas(db);
        crearTablaLugares(db);
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

    private void crearTablaCompras(SQLiteDatabase db) {

        String sql =
                "CREATE TABLE IF NOT EXISTS " + TABLE_COMPRAS + " (" +
                        COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COL_DESCRIPCION + " TEXT NOT NULL, " +
                        COL_MONTO + " INTEGER NOT NULL CHECK (" +
                        COL_MONTO + " > 0), " +
                        COL_FECHA + " INTEGER NOT NULL, " +
                        COL_CUENTA_ID + " INTEGER NOT NULL, " +
                        COL_CONTACTO_ID + " INTEGER, " +
                        COL_ESTADO + " TEXT NOT NULL CHECK (" +
                        COL_ESTADO + " IN ('REALIZADA','PROGRAMADA')), " +
                        COL_MOVIMIENTO_ID + " INTEGER UNIQUE, " +
                        "CHECK ((" + COL_ESTADO + " = 'PROGRAMADA' " +
                        "AND " + COL_MOVIMIENTO_ID + " IS NULL) OR (" +
                        COL_ESTADO + " = 'REALIZADA' AND " +
                        COL_MOVIMIENTO_ID + " IS NOT NULL)), " +
                        "FOREIGN KEY (" + COL_CUENTA_ID + ") " +
                        "REFERENCES " + TABLE_CUENTAS + "(" + COL_ID + ") " +
                        "ON DELETE RESTRICT, " +
                        "FOREIGN KEY (" + COL_CONTACTO_ID + ") " +
                        "REFERENCES " + TABLE_CONTACTOS + "(" + COL_ID + ") " +
                        "ON DELETE SET NULL, " +
                        "FOREIGN KEY (" + COL_MOVIMIENTO_ID + ") " +
                        "REFERENCES " + TABLE_MOVIMIENTOS + "(" + COL_ID + ") " +
                        "ON DELETE RESTRICT" +
                        ")";

        db.execSQL(sql);

        db.execSQL(
                "CREATE INDEX IF NOT EXISTS idx_compras_fecha " +
                        "ON " + TABLE_COMPRAS +
                        "(" + COL_FECHA + ")"
        );

        db.execSQL(
                "CREATE INDEX IF NOT EXISTS idx_compras_cuenta " +
                        "ON " + TABLE_COMPRAS +
                        "(" + COL_CUENTA_ID + ")"
        );

        db.execSQL(
                "CREATE INDEX IF NOT EXISTS idx_compras_contacto " +
                        "ON " + TABLE_COMPRAS +
                        "(" + COL_CONTACTO_ID + ")"
        );
    }

    // MIGRACIONES SIN BORRAR DATOS
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

        if (oldVersion < 4) {
            crearTablaCompras(db);
        }

        if (oldVersion < 5) {
            CompartidasSchema.crearTablas(db);
        }

        if (oldVersion < 6) {
            crearTablaLugares(db);
        }
    }

    // MODULO 4 - LUGARES DEL MAPA (MAXIMO 4, CONTROLADO EN REPOSITORIO)
    private void crearTablaLugares(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS lugares_mapa (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "nombre TEXT NOT NULL, descripcion TEXT NOT NULL DEFAULT '', " +
                "emoji TEXT NOT NULL, latitud REAL NOT NULL, longitud REAL NOT NULL)");
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

        return db.insert(TABLE_CUENTAS, null, values);
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
                new String[]{String.valueOf(cuenta.getId())}
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

        // No eliminar cuentas vinculadas a compras.
        Cursor compras = db.rawQuery(
                "SELECT COUNT(*) FROM " + TABLE_COMPRAS +
                        " WHERE " + COL_CUENTA_ID + " = ?",
                new String[]{String.valueOf(id)}
        );

        try {
            if (compras.moveToFirst() && compras.getInt(0) > 0) {
                return -1;
            }
        } finally {
            compras.close();
        }

        return db.delete(
                TABLE_CUENTAS,
                COL_ID + " = ?",
                new String[]{String.valueOf(id)}
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

            if ("Gasto".equals(tipo) && nuevoSaldo < 0) {
                return -1;
            }

            ContentValues valuesMovimiento = new ContentValues();

            valuesMovimiento.put(
                    COL_CUENTA_ID,
                    movimiento.getCuentaId()
            );

            valuesMovimiento.put(COL_TIPO, movimiento.getTipo());
            valuesMovimiento.put(COL_MONTO, movimiento.getMonto());
            valuesMovimiento.put(
                    COL_CATEGORIA,
                    movimiento.getCategoria()
            );
            valuesMovimiento.put(
                    COL_DESCRIPCION,
                    movimiento.getDescripcion()
            );
            valuesMovimiento.put(COL_FECHA, movimiento.getFecha());

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
        values.put(
                COL_TELEFONO,
                telefono == null ? "" : telefono.trim()
        );
        values.put(
                COL_CORREO,
                correo == null ? "" : correo.trim()
        );

        return db.insert(TABLE_CONTACTOS, null, values);
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
        values.put(
                COL_TELEFONO,
                telefono == null ? "" : telefono.trim()
        );
        values.put(
                COL_CORREO,
                correo == null ? "" : correo.trim()
        );

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

    // =====================================
    // COMPRAS - INSERTAR
    // =====================================

    public long insertarCompra(
            String descripcion,
            long monto,
            long fecha,
            int cuentaId,
            Integer contactoId,
            String estado) {

        if (!datosCompraValidos(
                descripcion, monto, fecha, cuentaId, estado)) {
            return -1;
        }

        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();

        try {
            if (!existeCuenta(db, cuentaId)
                    || !existeContactoOpcional(db, contactoId)) {
                return -1;
            }

            Long movimientoId = null;

            if (COMPRA_REALIZADA.equals(estado)) {

                movimientoId = registrarGastoCompra(
                        db,
                        cuentaId,
                        monto,
                        descripcion.trim(),
                        fecha
                );

                if (movimientoId == null) {
                    return -1;
                }
            }

            ContentValues values = new ContentValues();

            values.put(COL_DESCRIPCION, descripcion.trim());
            values.put(COL_MONTO, monto);
            values.put(COL_FECHA, fecha);
            values.put(COL_CUENTA_ID, cuentaId);
            values.put(COL_ESTADO, estado);

            if (contactoId == null) {
                values.putNull(COL_CONTACTO_ID);
            } else {
                values.put(COL_CONTACTO_ID, contactoId);
            }

            if (movimientoId == null) {
                values.putNull(COL_MOVIMIENTO_ID);
            } else {
                values.put(COL_MOVIMIENTO_ID, movimientoId);
            }

            long compraId = db.insert(
                    TABLE_COMPRAS,
                    null,
                    values
            );

            if (compraId == -1) {
                return -1;
            }

            db.setTransactionSuccessful();
            return compraId;

        } finally {
            db.endTransaction();
        }
    }

    // =====================================
    // COMPRAS - OBTENER TODAS
    // =====================================

    // Cada registro contiene:
    // [0] id
    // [1] descripcion
    // [2] monto
    // [3] fecha (milisegundos)
    // [4] cuenta_id
    // [5] contacto_id (vacío si no existe)
    // [6] estado
    // [7] movimiento_id (vacío si no existe)
    // [8] nombre de la cuenta
    // [9] nombre del contacto

    public List<String[]> obtenerCompras() {

        List<String[]> compras = new ArrayList<>();

        SQLiteDatabase db = getReadableDatabase();

        String sql =
                "SELECT c.*, " +
                        "a." + COL_NOMBRE + " AS nombre_cuenta, " +
                        "t." + COL_NOMBRE + " AS nombre_contacto " +
                        "FROM " + TABLE_COMPRAS + " c " +
                        "INNER JOIN " + TABLE_CUENTAS + " a " +
                        "ON c." + COL_CUENTA_ID + " = a." + COL_ID + " " +
                        "LEFT JOIN " + TABLE_CONTACTOS + " t " +
                        "ON c." + COL_CONTACTO_ID + " = t." + COL_ID + " " +
                        "ORDER BY c." + COL_FECHA + " DESC, " +
                        "c." + COL_ID + " DESC";

        Cursor cursor = db.rawQuery(sql, null);

        try {
            while (cursor.moveToNext()) {
                compras.add(leerCompra(cursor));
            }
        } finally {
            cursor.close();
        }

        return compras;
    }

    // =====================================
    // COMPRAS - BUSCAR POR ID
    // =====================================

    public String[] obtenerCompraPorId(int id) {

        SQLiteDatabase db = getReadableDatabase();

        String sql =
                "SELECT c.*, " +
                        "a." + COL_NOMBRE + " AS nombre_cuenta, " +
                        "t." + COL_NOMBRE + " AS nombre_contacto " +
                        "FROM " + TABLE_COMPRAS + " c " +
                        "INNER JOIN " + TABLE_CUENTAS + " a " +
                        "ON c." + COL_CUENTA_ID + " = a." + COL_ID + " " +
                        "LEFT JOIN " + TABLE_CONTACTOS + " t " +
                        "ON c." + COL_CONTACTO_ID + " = t." + COL_ID + " " +
                        "WHERE c." + COL_ID + " = ?";

        Cursor cursor = db.rawQuery(
                sql,
                new String[]{String.valueOf(id)}
        );

        try {
            if (cursor.moveToFirst()) {
                return leerCompra(cursor);
            }
        } finally {
            cursor.close();
        }

        return null;
    }

    // =====================================
    // COMPRAS - ACTUALIZAR PROGRAMADA
    // =====================================

    public int actualizarCompraProgramada(
            int id,
            String descripcion,
            long monto,
            long fecha,
            int cuentaId,
            Integer contactoId) {

        if (!datosCompraValidos(
                descripcion,
                monto,
                fecha,
                cuentaId,
                COMPRA_PROGRAMADA)) {
            return -1;
        }

        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();

        try {
            if (!existeCuenta(db, cuentaId)
                    || !existeContactoOpcional(db, contactoId)) {
                return -1;
            }

            ContentValues values = new ContentValues();

            values.put(COL_DESCRIPCION, descripcion.trim());
            values.put(COL_MONTO, monto);
            values.put(COL_FECHA, fecha);
            values.put(COL_CUENTA_ID, cuentaId);

            if (contactoId == null) {
                values.putNull(COL_CONTACTO_ID);
            } else {
                values.put(COL_CONTACTO_ID, contactoId);
            }

            int resultado = db.update(
                    TABLE_COMPRAS,
                    values,
                    COL_ID + " = ? AND " + COL_ESTADO + " = ?",
                    new String[]{
                            String.valueOf(id),
                            COMPRA_PROGRAMADA
                    }
            );

            db.setTransactionSuccessful();
            return resultado;

        } finally {
            db.endTransaction();
        }
    }

    // =====================================
    // COMPRAS - CONFIRMAR PROGRAMADA
    // =====================================

    public boolean confirmarCompraProgramada(int compraId) {

        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();

        try {
            String descripcion;
            long monto;
            int cuentaId;

            Cursor cursor = db.query(
                    TABLE_COMPRAS,
                    new String[]{
                            COL_DESCRIPCION,
                            COL_MONTO,
                            COL_CUENTA_ID
                    },
                    COL_ID + " = ? AND " + COL_ESTADO + " = ?",
                    new String[]{
                            String.valueOf(compraId),
                            COMPRA_PROGRAMADA
                    },
                    null,
                    null,
                    null
            );

            try {
                if (!cursor.moveToFirst()) {
                    return false;
                }

                descripcion = cursor.getString(
                        cursor.getColumnIndexOrThrow(COL_DESCRIPCION)
                );

                monto = cursor.getLong(
                        cursor.getColumnIndexOrThrow(COL_MONTO)
                );

                cuentaId = cursor.getInt(
                        cursor.getColumnIndexOrThrow(COL_CUENTA_ID)
                );

            } finally {
                cursor.close();
            }

            // Fecha efectiva de pago.
            long fechaRealizacion = System.currentTimeMillis();

            Long movimientoId = registrarGastoCompra(
                    db,
                    cuentaId,
                    monto,
                    descripcion,
                    fechaRealizacion
            );

            if (movimientoId == null) {
                return false;
            }

            ContentValues values = new ContentValues();

            values.put(COL_ESTADO, COMPRA_REALIZADA);
            values.put(COL_MOVIMIENTO_ID, movimientoId);
            values.put(COL_FECHA, fechaRealizacion);

            int actualizadas = db.update(
                    TABLE_COMPRAS,
                    values,
                    COL_ID + " = ? AND " + COL_ESTADO + " = ?",
                    new String[]{
                            String.valueOf(compraId),
                            COMPRA_PROGRAMADA
                    }
            );

            if (actualizadas != 1) {
                return false;
            }

            db.setTransactionSuccessful();
            return true;

        } finally {
            db.endTransaction();
        }
    }

    // =====================================
    // COMPRAS - ELIMINAR PROGRAMADA
    // =====================================

    public int eliminarCompraProgramada(int id) {

        SQLiteDatabase db = getWritableDatabase();

        return db.delete(
                TABLE_COMPRAS,
                COL_ID + " = ? AND " + COL_ESTADO + " = ?",
                new String[]{
                        String.valueOf(id),
                        COMPRA_PROGRAMADA
                }
        );
    }

    // =====================================
    // COMPRAS - VALIDACIONES
    // =====================================

    private boolean datosCompraValidos(
            String descripcion,
            long monto,
            long fecha,
            int cuentaId,
            String estado) {

        return descripcion != null
                && !descripcion.trim().isEmpty()
                && monto > 0
                && fecha > 0
                && cuentaId > 0
                && (COMPRA_REALIZADA.equals(estado)
                || COMPRA_PROGRAMADA.equals(estado));
    }

    private boolean existeCuenta(
            SQLiteDatabase db,
            int cuentaId) {

        Cursor cursor = db.query(
                TABLE_CUENTAS,
                new String[]{COL_ID},
                COL_ID + " = ?",
                new String[]{String.valueOf(cuentaId)},
                null,
                null,
                null
        );

        try {
            return cursor.moveToFirst();
        } finally {
            cursor.close();
        }
    }

    private boolean existeContactoOpcional(
            SQLiteDatabase db,
            Integer contactoId) {

        if (contactoId == null) {
            return true;
        }

        Cursor cursor = db.query(
                TABLE_CONTACTOS,
                new String[]{COL_ID},
                COL_ID + " = ?",
                new String[]{String.valueOf(contactoId)},
                null,
                null,
                null
        );

        try {
            return cursor.moveToFirst();
        } finally {
            cursor.close();
        }
    }

    // =====================================
    // COMPRAS - REGISTRAR GASTO
    // Se llama dentro de una transacción.
    // =====================================

    private Long registrarGastoCompra(
            SQLiteDatabase db,
            int cuentaId,
            long monto,
            String descripcion,
            long fecha) {

        long saldoActual;

        Cursor cursor = db.query(
                TABLE_CUENTAS,
                new String[]{COL_SALDO},
                COL_ID + " = ?",
                new String[]{String.valueOf(cuentaId)},
                null,
                null,
                null
        );

        try {
            if (!cursor.moveToFirst()) {
                return null;
            }

            saldoActual = cursor.getLong(
                    cursor.getColumnIndexOrThrow(COL_SALDO)
            );
        } finally {
            cursor.close();
        }

        long nuevoSaldo;

        try {
            nuevoSaldo = Math.subtractExact(saldoActual, monto);
        } catch (ArithmeticException e) {
            return null;
        }

        if (nuevoSaldo < 0) {
            return null;
        }

        ContentValues movimiento = new ContentValues();

        movimiento.put(COL_CUENTA_ID, cuentaId);
        movimiento.put(COL_TIPO, "Gasto");
        movimiento.put(COL_MONTO, monto);
        movimiento.put(COL_CATEGORIA, "Compras");
        movimiento.put(COL_DESCRIPCION, descripcion);
        movimiento.put(COL_FECHA, fecha);

        long movimientoId = db.insert(
                TABLE_MOVIMIENTOS,
                null,
                movimiento
        );

        if (movimientoId == -1) {
            return null;
        }

        ContentValues cuenta = new ContentValues();
        cuenta.put(COL_SALDO, nuevoSaldo);

        int actualizadas = db.update(
                TABLE_CUENTAS,
                cuenta,
                COL_ID + " = ?",
                new String[]{String.valueOf(cuentaId)}
        );

        if (actualizadas != 1) {
            return null;
        }

        return movimientoId;
    }

    // =====================================
    // COMPRAS - CONVERTIR CURSOR
    // =====================================

    private String[] leerCompra(Cursor cursor) {

        int indiceContacto = cursor.getColumnIndexOrThrow(
                COL_CONTACTO_ID
        );

        int indiceMovimiento = cursor.getColumnIndexOrThrow(
                COL_MOVIMIENTO_ID
        );

        int indiceNombreContacto = cursor.getColumnIndexOrThrow(
                "nombre_contacto"
        );

        return new String[]{
                String.valueOf(cursor.getLong(
                        cursor.getColumnIndexOrThrow(COL_ID)
                )),
                cursor.getString(
                        cursor.getColumnIndexOrThrow(COL_DESCRIPCION)
                ),
                String.valueOf(cursor.getLong(
                        cursor.getColumnIndexOrThrow(COL_MONTO)
                )),
                String.valueOf(cursor.getLong(
                        cursor.getColumnIndexOrThrow(COL_FECHA)
                )),
                String.valueOf(cursor.getInt(
                        cursor.getColumnIndexOrThrow(COL_CUENTA_ID)
                )),
                cursor.isNull(indiceContacto)
                        ? ""
                        : String.valueOf(cursor.getInt(indiceContacto)),
                cursor.getString(
                        cursor.getColumnIndexOrThrow(COL_ESTADO)
                ),
                cursor.isNull(indiceMovimiento)
                        ? ""
                        : String.valueOf(cursor.getLong(indiceMovimiento)),
                cursor.getString(
                        cursor.getColumnIndexOrThrow("nombre_cuenta")
                ),
                cursor.isNull(indiceNombreContacto)
                        ? ""
                        : cursor.getString(indiceNombreContacto)
        };
    }
}
