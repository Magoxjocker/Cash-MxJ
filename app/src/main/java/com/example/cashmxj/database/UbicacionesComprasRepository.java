package com.example.cashmxj.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.util.ArrayList;
import java.util.List;

public class UbicacionesComprasRepository {
    private final DatabaseHelper helper;
    public UbicacionesComprasRepository(DatabaseHelper helper) { this.helper = helper; }
    public static class CompraUbicada {
        public final int id;
        public final String descripcion, estado, cuenta;
        public final long monto;
        public final double latitud, longitud;
        public CompraUbicada(int id, String descripcion, String estado, String cuenta, long monto, double latitud, double longitud) {
            this.id=id; this.descripcion=descripcion; this.estado=estado; this.cuenta=cuenta; this.monto=monto; this.latitud=latitud; this.longitud=longitud;
        }
    }
    public boolean guardar(int compraId, double lat, double lon) {
        if (compraId <= 0 || !Double.isFinite(lat) || !Double.isFinite(lon) || Math.abs(lat)>90 || Math.abs(lon)>180) return false;
        ContentValues v = new ContentValues();
        v.put("compra_id", compraId); v.put("latitud",lat); v.put("longitud",lon);
        return helper.getWritableDatabase().insertWithOnConflict("ubicaciones_compras",null,v,SQLiteDatabase.CONFLICT_REPLACE) != -1;
    }
    public double[] obtener(int compraId) {
        try(Cursor c=helper.getReadableDatabase().rawQuery("SELECT latitud,longitud FROM ubicaciones_compras WHERE compra_id=?",new String[]{String.valueOf(compraId)})) {
            return c.moveToFirst() ? new double[]{c.getDouble(0),c.getDouble(1)} : null;
        }
    }
    public List<CompraUbicada> listar() {
        List<CompraUbicada> items=new ArrayList<>();
        String sql="SELECT c.id,c.descripcion,c.estado,cu.nombre,c.monto,u.latitud,u.longitud FROM compras c JOIN ubicaciones_compras u ON u.compra_id=c.id JOIN cuentas cu ON cu.id=c.cuenta_id";
        try(Cursor c=helper.getReadableDatabase().rawQuery(sql,null)) {
            while(c.moveToNext()) items.add(new CompraUbicada(c.getInt(0),c.getString(1),c.getString(2),c.getString(3),c.getLong(4),c.getDouble(5),c.getDouble(6)));
        }
        return items;
    }
}
