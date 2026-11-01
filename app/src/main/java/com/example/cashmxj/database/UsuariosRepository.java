package com.example.cashmxj.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Locale;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.MessageDigest;

public class UsuariosRepository {
    private final DatabaseHelper helper;
    public UsuariosRepository(Context context) { helper = new DatabaseHelper(context); }
    private String normalizar(String correo) { return correo.trim().toLowerCase(Locale.ROOT); }
    private byte[] derivar(String clave, byte[] sal) throws Exception {
        PBEKeySpec spec = new PBEKeySpec(clave.toCharArray(), sal, 120000, 256);
        try { return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded(); }
        finally { spec.clearPassword(); }
    }
    public boolean existe(String correo) {
        try (Cursor c = helper.getReadableDatabase().rawQuery("SELECT id FROM usuarios_app WHERE correo=?", new String[]{normalizar(correo)})) {
            return c.moveToFirst();
        }
    }
    public boolean registrar(String correo, String clave) throws Exception {
        if (existe(correo)) return false;
        byte[] sal = new byte[16]; new SecureRandom().nextBytes(sal);
        ContentValues v = new ContentValues();
        v.put("correo", normalizar(correo)); v.put("sal", sal); v.put("hash", derivar(clave, sal));
        return helper.getWritableDatabase().insert("usuarios_app", null, v) != -1;
    }
    public boolean validar(String correo, String clave) throws Exception {
        try (Cursor c = helper.getReadableDatabase().rawQuery("SELECT sal, hash FROM usuarios_app WHERE correo=?", new String[]{normalizar(correo)})) {
            return c.moveToFirst() && MessageDigest.isEqual(c.getBlob(1), derivar(clave, c.getBlob(0)));
        }
    }
}
