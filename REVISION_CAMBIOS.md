# Cash MxJ — versión para revisión de código

## Implementado en esta entrega
- Se toma como base la versión previa con registro local de correo/contraseña y BiometricPrompt.
- SQLite versión 8: tabla `ubicaciones_compras` con relación a compras existentes, sin borrar las tablas previas.
- Nueva compra: ubicación obligatoria mediante GPS o pulsación prolongada en el mapa.
- Edición de compra programada: carga y actualiza su ubicación.
- Mapa: conserva lugares personalizados y muestra marcadores adicionales para compras realizadas y programadas. Al tocar un marcador se muestran datos de la compra.
- Al eliminar una compra, la relación de ubicación se elimina por clave foránea.

## Limitaciones y verificaciones pendientes
- Este paquete contiene código fuente, NO un APK compilado.
- No se pudo ejecutar Android Studio ni un emulador en este entorno; compilar y realizar pruebas en un teléfono antes de usar con datos importantes.
- La modernización visual completa del resto de pantallas NO está realizada en esta entrega.
- El registro local no equivale a autenticación en línea ni permite sincronización entre teléfonos.
- El aislamiento de los datos financieros por cada usuario aún NO está implementado. No usar varias cuentas de usuario con datos privados en el mismo dispositivo hasta completar esta separación.
- Las compras existentes anteriores a esta actualización no poseen coordenadas; no se inventan ubicaciones y no aparecen como marcadores hasta asignarlas.
- Revisar el caso de error de almacenamiento de ubicación tras registrar una compra: los datos financieros podrían quedar guardados sin su marcador; se recomienda integrar ambas escrituras en una única transacción.

## Pruebas recomendadas
1. Abrir en Android Studio y sincronizar Gradle.
2. Crear una cuenta financiera, registrar una compra realizada y elegir punto en mapa.
3. Registrar compra programada usando GPS y editar su ubicación.
4. Abrir mapa y comprobar que aparecen lugares personalizados y compras con distintos iconos.
5. Confirmar que una compra programada no descuenta saldo hasta ser realizada.
6. Comprobar el acceso por contraseña y huella en un dispositivo con sensor compatible.
