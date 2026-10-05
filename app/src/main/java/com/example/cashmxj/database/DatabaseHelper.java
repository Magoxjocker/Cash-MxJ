package com.example.cashmxj.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.cashmxj.models.Cuenta;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "cashmxj.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_CUENTAS = "cuentas";

    private static final String COL_ID = "id";
    private static final String COL_NOMBRE = "nombre";
    private static final String COL_TIPO = "tipo";
    private static final String COL_SALDO = "saldo";
    private static final String COL_DESCRIPCION = "descripcion";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String crearTablaCuentas =
                "CREATE TABLE " + TABLE_CUENTAS + " (" +
                        COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COL_NOMBRE + " TEXT NOT NULL, " +
                        COL_TIPO + " TEXT NOT NULL, " +
                        COL_SALDO + " INTEGER NOT NULL, " +
                        COL_DESCRIPCION + " TEXT" +
                        ")";

        db.execSQL(crearTablaCuentas);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CUENTAS);
        onCreate(db);
    }

    // CREATE
    public long insertarCuenta(Cuenta cuenta) {

        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COL_NOMBRE, cuenta.getNombre());
        values.put(COL_TIPO, cuenta.getTipo());
        values.put(COL_SALDO, cuenta.getSaldo());
        values.put(COL_DESCRIPCION, cuenta.getDescripcion());

        return db.insert(TABLE_CUENTAS, null, values);
    }

    // READ
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

        if (cursor.moveToFirst()) {

            do {

                Cuenta cuenta = new Cuenta(
                        cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_NOMBRE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_TIPO)),
                        cursor.getLong(cursor.getColumnIndexOrThrow(COL_SALDO)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_DESCRIPCION))
                );

                cuentas.add(cuenta);

            } while (cursor.moveToNext());
        }

        cursor.close();

        return cuentas;
    }

    public long obtenerSaldoTotal() {

        long total = 0;

        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT SUM(" + COL_SALDO + ") FROM " + TABLE_CUENTAS,
                null
        );

        if (cursor.moveToFirst() && !cursor.isNull(0)) {
            total = cursor.getLong(0);
        }

        cursor.close();

        return total;
    }
}