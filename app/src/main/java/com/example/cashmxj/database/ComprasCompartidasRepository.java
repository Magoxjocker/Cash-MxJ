
package com.example.cashmxj.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ComprasCompartidasRepository {

    private final DatabaseHelper databaseHelper;

    public static class Item {
        public final String descripcion;
        public final int cantidad;
        public final long precioUnitario;

        public Item(String descripcion, int cantidad, long precioUnitario) {
            this.descripcion = descripcion;
            this.cantidad = cantidad;
            this.precioUnitario = precioUnitario;
        }
    }

    public static class Participante {
        public final int contactoId;
        public final long montoAsignado;
        public final long montoPagado;

        public Participante(int contactoId, long montoAsignado, long montoPagado) {
            this.contactoId = contactoId;
            this.montoAsignado = montoAsignado;
            this.montoPagado = montoPagado;
        }

        public long obtenerDeudaPendiente() {
            return montoAsignado - montoPagado;
        }
    }

    public ComprasCompartidasRepository(DatabaseHelper databaseHelper) {
        if (databaseHelper == null) {
            throw new IllegalArgumentException(
                    "DatabaseHelper es obligatorio"
            );
        }
        this.databaseHelper = databaseHelper;
    }

    // =====================================
    // CALCULAR TOTAL DE PRODUCTOS
    // =====================================

    public long calcularTotal(List<Item> items) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException(
                    "Debes agregar al menos un producto"
            );
        }

        long total = 0;

        for (Item item : items) {
            if (item == null
                    || item.descripcion == null
                    || item.descripcion.trim().isEmpty()
                    || item.cantidad <= 0
                    || item.precioUnitario <= 0) {
                throw new IllegalArgumentException(
                        "Los productos contienen datos inválidos"
                );
            }

            long subtotal = Math.multiplyExact(
                    (long) item.cantidad,
                    item.precioUnitario
            );
            total = Math.addExact(total, subtotal);
        }

        return total;
    }

    // =====================================
    // DISTRIBUCION EQUITATIVA
    // =====================================

    public List<Long> calcularDistribucion(
            long montoTotal,
            int numeroParticipantes) {

        if (montoTotal <= 0 || numeroParticipantes <= 0) {
            throw new IllegalArgumentException(
                    "Monto o participantes inválidos"
            );
        }

        List<Long> distribucion = new ArrayList<>();
        long cuotaBase = montoTotal / numeroParticipantes;
        long resto = montoTotal % numeroParticipantes;

        for (int i = 0; i < numeroParticipantes; i++) {
            long cuota = cuotaBase;
            if (i < resto) {
                cuota++;
            }
            distribucion.add(cuota);
        }

        return distribucion;
    }

    // =====================================
    // REGISTRAR COMPRA COMPARTIDA
    // =====================================

    public long registrarCompraCompartida(
            String descripcion,
            long fecha,
            int cuentaId,
            List<Item> items,
            List<Integer> contactosIds) {

        if (descripcion == null
                || descripcion.trim().isEmpty()
                || fecha <= 0
                || cuentaId <= 0
                || contactosIds == null
                || contactosIds.isEmpty()) {
            return -1;
        }

        final long montoTotal;

        try {
            montoTotal = calcularTotal(items);
        } catch (IllegalArgumentException | ArithmeticException e) {
            return -1;
        }

        Set<Integer> contactosUnicos = new HashSet<>();

        for (Integer contactoId : contactosIds) {
            if (contactoId == null
                    || contactoId <= 0
                    || !contactosUnicos.add(contactoId)) {
                return -1;
            }
        }

        int numeroParticipantes = contactosIds.size() + 1;

        List<Long> distribucion = calcularDistribucion(
                montoTotal,
                numeroParticipantes
        );

        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        db.beginTransaction();

        try {
            Long saldoActual = obtenerSaldoCuenta(db, cuentaId);

            if (saldoActual == null || saldoActual < montoTotal) {
                return -1;
            }

            for (Integer contactoId : contactosIds) {
                if (!existeContacto(db, contactoId)) {
                    return -1;
                }
            }

            long nuevoSaldo = Math.subtractExact(
                    saldoActual,
                    montoTotal
            );

            ContentValues movimiento = new ContentValues();
            movimiento.put("cuenta_id", cuentaId);
            movimiento.put("tipo", "Gasto");
            movimiento.put("monto", montoTotal);
            movimiento.put("categoria", "Compras compartidas");
            movimiento.put("descripcion", descripcion.trim());
            movimiento.put("fecha", fecha);

            long movimientoId = db.insert(
                    "movimientos",
                    null,
                    movimiento
            );

            if (movimientoId == -1) {
                return -1;
            }

            ContentValues cuenta = new ContentValues();
            cuenta.put("saldo", nuevoSaldo);

            int cuentasActualizadas = db.update(
                    "cuentas",
                    cuenta,
                    "id = ?",
                    new String[]{String.valueOf(cuentaId)}
            );

            if (cuentasActualizadas != 1) {
                return -1;
            }

            ContentValues compra = new ContentValues();
            compra.put("descripcion", descripcion.trim());
            compra.put("fecha", fecha);
            compra.put("cuenta_id", cuentaId);
            compra.put("monto_total", montoTotal);
            compra.put("movimiento_id", movimientoId);

            long compraId = db.insert(
                    "compras_compartidas",
                    null,
                    compra
            );

            if (compraId == -1) {
                return -1;
            }

            for (Item item : items) {
                ContentValues producto = new ContentValues();
                producto.put("compra_id", compraId);
                producto.put("descripcion", item.descripcion.trim());
                producto.put("cantidad", item.cantidad);
                producto.put("precio_unitario", item.precioUnitario);

                long productoId = db.insert(
                        "compartidos_items",
                        null,
                        producto
                );

                if (productoId == -1) {
                    return -1;
                }
            }

            ContentValues pagador = new ContentValues();
            pagador.put("compra_id", compraId);
            pagador.putNull("contacto_id");
            pagador.put("es_pagador", 1);
            pagador.put("monto_asignado", distribucion.get(0));
            pagador.put("monto_pagado", distribucion.get(0));

            long pagadorId = db.insert(
                    "compartidos_participantes",
                    null,
                    pagador
            );

            if (pagadorId == -1) {
                return -1;
            }

            for (int i = 0; i < contactosIds.size(); i++) {
                ContentValues participante = new ContentValues();
                participante.put("compra_id", compraId);
                participante.put("contacto_id", contactosIds.get(i));
                participante.put("es_pagador", 0);
                participante.put(
                        "monto_asignado",
                        distribucion.get(i + 1)
                );
                participante.put("monto_pagado", 0);

                long participanteId = db.insert(
                        "compartidos_participantes",
                        null,
                        participante
                );

                if (participanteId == -1) {
                    return -1;
                }
            }

            db.setTransactionSuccessful();
            return compraId;

        } catch (Exception e) {
            return -1;
        } finally {
            db.endTransaction();
        }
    }

    // =====================================
    // HISTORIAL DE COMPRAS
    // =====================================

    public List<String[]> obtenerComprasCompartidas() {
        List<String[]> resultado = new ArrayList<>();

        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        String sql =
                "SELECT c.id, c.descripcion, c.fecha, " +
                        "c.monto_total, a.nombre, " +
                        "(SELECT COUNT(*) " +
                        "FROM compartidos_participantes p " +
                        "WHERE p.compra_id = c.id " +
                        "AND p.es_pagador = 0), " +
                        "(SELECT COALESCE(SUM(" +
                        "p.monto_asignado - p.monto_pagado), 0) " +
                        "FROM compartidos_participantes p " +
                        "WHERE p.compra_id = c.id " +
                        "AND p.es_pagador = 0) " +
                        "FROM compras_compartidas c " +
                        "INNER JOIN cuentas a " +
                        "ON a.id = c.cuenta_id " +
                        "ORDER BY c.fecha DESC, c.id DESC";

        try (Cursor cursor = db.rawQuery(sql, null)) {
            while (cursor.moveToNext()) {
                resultado.add(new String[]{
                        String.valueOf(cursor.getLong(0)),
                        cursor.getString(1),
                        String.valueOf(cursor.getLong(2)),
                        String.valueOf(cursor.getLong(3)),
                        cursor.getString(4),
                        String.valueOf(cursor.getInt(5)),
                        String.valueOf(cursor.getLong(6))
                });
            }
        }

        return resultado;
    }

    // =====================================
    // PRODUCTOS DE UNA COMPRA
    // =====================================

    public List<Item> obtenerItems(long compraId) {
        List<Item> items = new ArrayList<>();

        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        try (Cursor cursor = db.rawQuery(
                "SELECT descripcion, cantidad, precio_unitario " +
                        "FROM compartidos_items " +
                        "WHERE compra_id = ? ORDER BY id ASC",
                new String[]{String.valueOf(compraId)}
        )) {
            while (cursor.moveToNext()) {
                items.add(new Item(
                        cursor.getString(0),
                        cursor.getInt(1),
                        cursor.getLong(2)
                ));
            }
        }

        return items;
    }

    // =====================================
    // PARTICIPANTES Y DEUDAS
    // =====================================

    // [0] Nombre
    // [1] Monto asignado
    // [2] Monto pagado
    // [3] Deuda pendiente
    // [4] Es pagador
    // [5] ID contacto (nuevo)

    public List<String[]> obtenerParticipantes(long compraId) {
        List<String[]> participantes = new ArrayList<>();

        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        String sql =
                "SELECT CASE WHEN p.es_pagador = 1 " +
                        "THEN 'Tú (pagador)' " +
                        "ELSE COALESCE(t.nombre, 'Contacto') END, " +
                        "p.monto_asignado, p.monto_pagado, " +
                        "(p.monto_asignado - p.monto_pagado), " +
                        "p.es_pagador, p.contacto_id " +
                        "FROM compartidos_participantes p " +
                        "LEFT JOIN contactos t " +
                        "ON t.id = p.contacto_id " +
                        "WHERE p.compra_id = ? " +
                        "ORDER BY p.es_pagador DESC, p.id ASC";

        try (Cursor cursor = db.rawQuery(
                sql,
                new String[]{String.valueOf(compraId)}
        )) {
            while (cursor.moveToNext()) {
                participantes.add(new String[]{
                        cursor.getString(0),
                        String.valueOf(cursor.getLong(1)),
                        String.valueOf(cursor.getLong(2)),
                        String.valueOf(cursor.getLong(3)),
                        String.valueOf(cursor.getInt(4)),
                        cursor.isNull(5)
                                ? ""
                                : String.valueOf(cursor.getInt(5))
                });
            }
        }

        return participantes;
    }

    // =====================================
    // REGISTRAR ABONO O PAGO COMPLETO
    // =====================================

    public boolean registrarAbono(
            long compraId,
            int contactoId,
            long montoAbono) {

        if (compraId <= 0 || contactoId <= 0 || montoAbono <= 0) {
            return false;
        }

        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        db.beginTransaction();

        try {
            long participanteId;
            long montoAsignado;
            long montoPagado;
            long cuentaId;
            String descripcionCompra;
            String nombreContacto;

            // Buscar al participante y la cuenta de la compra.
            String sql =
                    "SELECT p.id, p.monto_asignado, p.monto_pagado, " +
                            "c.cuenta_id, c.descripcion, " +
                            "COALESCE(t.nombre, 'Contacto') " +
                            "FROM compartidos_participantes p " +
                            "INNER JOIN compras_compartidas c " +
                            "ON c.id = p.compra_id " +
                            "INNER JOIN contactos t " +
                            "ON t.id = p.contacto_id " +
                            "WHERE p.compra_id = ? " +
                            "AND p.contacto_id = ? " +
                            "AND p.es_pagador = 0";

            try (Cursor cursor = db.rawQuery(
                    sql,
                    new String[]{
                            String.valueOf(compraId),
                            String.valueOf(contactoId)
                    }
            )) {
                // No se permiten participantes duplicados.
                if (!cursor.moveToFirst()) {
                    return false;
                }

                participanteId = cursor.getLong(0);
                montoAsignado = cursor.getLong(1);
                montoPagado = cursor.getLong(2);
                cuentaId = cursor.getLong(3);
                descripcionCompra = cursor.getString(4);
                nombreContacto = cursor.getString(5);

                if (cursor.moveToNext()) {
                    return false;
                }
            }

            if (montoAsignado <= 0
                    || montoPagado < 0
                    || montoPagado > montoAsignado) {
                return false;
            }

            long pendiente = Math.subtractExact(
                    montoAsignado,
                    montoPagado
            );

            if (montoAbono > pendiente) {
                return false;
            }

            Long saldoActual = obtenerSaldoCuenta(
                    db,
                    (int) cuentaId
            );

            if (saldoActual == null) {
                return false;
            }

            long nuevoSaldo = Math.addExact(
                    saldoActual,
                    montoAbono
            );

            long nuevoMontoPagado = Math.addExact(
                    montoPagado,
                    montoAbono
            );

            // 1. Registrar el ingreso.
            ContentValues movimiento = new ContentValues();
            movimiento.put("cuenta_id", cuentaId);
            movimiento.put("tipo", "Ingreso");
            movimiento.put("monto", montoAbono);
            movimiento.put("categoria", "Cobro compra compartida");
            movimiento.put(
                    "descripcion",
                    "Abono de " + nombreContacto +
                            " - " + descripcionCompra
            );
            movimiento.put(
                    "fecha",
                    System.currentTimeMillis()
            );

            long movimientoId = db.insert(
                    "movimientos",
                    null,
                    movimiento
            );

            if (movimientoId == -1) {
                return false;
            }

            // 2. Aumentar el saldo de la cuenta.
            ContentValues valoresCuenta = new ContentValues();
            valoresCuenta.put("saldo", nuevoSaldo);

            int cuentaActualizada = db.update(
                    "cuentas",
                    valoresCuenta,
                    "id = ? AND saldo = ?",
                    new String[]{
                            String.valueOf(cuentaId),
                            String.valueOf(saldoActual)
                    }
            );

            if (cuentaActualizada != 1) {
                return false;
            }

            // 3. Actualizar la deuda del contacto.
            ContentValues valoresParticipante = new ContentValues();
            valoresParticipante.put(
                    "monto_pagado",
                    nuevoMontoPagado
            );

            int participanteActualizado = db.update(
                    "compartidos_participantes",
                    valoresParticipante,
                    "id = ? AND monto_pagado = ? " +
                            "AND es_pagador = 0",
                    new String[]{
                            String.valueOf(participanteId),
                            String.valueOf(montoPagado)
                    }
            );

            if (participanteActualizado != 1) {
                return false;
            }

            // Las tres operaciones se confirman juntas.
            db.setTransactionSuccessful();
            return true;

        } catch (Exception e) {
            return false;
        } finally {
            db.endTransaction();
        }
    }

    // =====================================
    // VALIDAR CUENTA
    // =====================================

    private Long obtenerSaldoCuenta(
            SQLiteDatabase db,
            int cuentaId) {

        try (Cursor cursor = db.rawQuery(
                "SELECT saldo FROM cuentas WHERE id = ?",
                new String[]{String.valueOf(cuentaId)}
        )) {
            if (cursor.moveToFirst()) {
                return cursor.getLong(0);
            }
        }

        return null;
    }

    // =====================================
    // VALIDAR CONTACTO
    // =====================================

    private boolean existeContacto(
            SQLiteDatabase db,
            int contactoId) {

        try (Cursor cursor = db.rawQuery(
                "SELECT id FROM contactos WHERE id = ?",
                new String[]{String.valueOf(contactoId)}
        )) {
            return cursor.moveToFirst();
        }
    }
}
