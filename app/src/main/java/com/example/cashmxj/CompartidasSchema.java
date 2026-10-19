
package com.example.cashmxj.database;

import android.database.sqlite.SQLiteDatabase;

public final class CompartidasSchema {

    private CompartidasSchema() {
        // Clase utilitaria: no necesita instancias.
    }

    public static void crearTablas(SQLiteDatabase db) {

        // =========================================
        // CABECERA DE LA COMPRA COMPARTIDA
        // =========================================

        db.execSQL(
                "CREATE TABLE IF NOT EXISTS compras_compartidas (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "descripcion TEXT NOT NULL, " +
                        "fecha INTEGER NOT NULL, " +
                        "cuenta_id INTEGER NOT NULL, " +
                        "monto_total INTEGER NOT NULL " +
                        "CHECK (monto_total > 0), " +
                        "movimiento_id INTEGER NOT NULL UNIQUE, " +
                        "FOREIGN KEY (cuenta_id) " +
                        "REFERENCES cuentas(id) ON DELETE RESTRICT, " +
                        "FOREIGN KEY (movimiento_id) " +
                        "REFERENCES movimientos(id) ON DELETE RESTRICT" +
                        ")"
        );

        // =========================================
        // PRODUCTOS O SERVICIOS
        // =========================================

        db.execSQL(
                "CREATE TABLE IF NOT EXISTS compartidos_items (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "compra_id INTEGER NOT NULL, " +
                        "descripcion TEXT NOT NULL, " +
                        "cantidad INTEGER NOT NULL " +
                        "CHECK (cantidad > 0), " +
                        "precio_unitario INTEGER NOT NULL " +
                        "CHECK (precio_unitario > 0), " +
                        "FOREIGN KEY (compra_id) " +
                        "REFERENCES compras_compartidas(id) " +
                        "ON DELETE RESTRICT" +
                        ")"
        );

        // =========================================
        // PARTICIPANTES Y DEUDAS
        // =========================================

        db.execSQL(
                "CREATE TABLE IF NOT EXISTS compartidos_participantes (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "compra_id INTEGER NOT NULL, " +
                        "contacto_id INTEGER, " +
                        "es_pagador INTEGER NOT NULL DEFAULT 0 " +
                        "CHECK (es_pagador IN (0, 1)), " +
                        "monto_asignado INTEGER NOT NULL " +
                        "CHECK (monto_asignado >= 0), " +
                        "monto_pagado INTEGER NOT NULL DEFAULT 0 " +
                        "CHECK (monto_pagado >= 0 " +
                        "AND monto_pagado <= monto_asignado), " +
                        "CHECK ((es_pagador = 1 " +
                        "AND contacto_id IS NULL) OR " +
                        "(es_pagador = 0 " +
                        "AND contacto_id IS NOT NULL)), " +
                        "UNIQUE (compra_id, contacto_id), " +
                        "FOREIGN KEY (compra_id) " +
                        "REFERENCES compras_compartidas(id) " +
                        "ON DELETE RESTRICT, " +
                        "FOREIGN KEY (contacto_id) " +
                        "REFERENCES contactos(id) ON DELETE RESTRICT" +
                        ")"
        );

        // =========================================
        // ÍNDICES
        // =========================================

        db.execSQL(
                "CREATE INDEX IF NOT EXISTS " +
                        "idx_compartidas_fecha " +
                        "ON compras_compartidas(fecha)"
        );

        db.execSQL(
                "CREATE INDEX IF NOT EXISTS " +
                        "idx_compartidas_cuenta " +
                        "ON compras_compartidas(cuenta_id)"
        );

        db.execSQL(
                "CREATE INDEX IF NOT EXISTS " +
                        "idx_compartidos_items_compra " +
                        "ON compartidos_items(compra_id)"
        );

        db.execSQL(
                "CREATE INDEX IF NOT EXISTS " +
                        "idx_compartidos_participantes_compra " +
                        "ON compartidos_participantes(compra_id)"
        );

        db.execSQL(
                "CREATE INDEX IF NOT EXISTS " +
                        "idx_compartidos_participantes_contacto " +
                        "ON compartidos_participantes(contacto_id)"
        );
    }
}
