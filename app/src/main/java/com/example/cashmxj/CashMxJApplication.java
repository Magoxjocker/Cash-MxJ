
package com.example.cashmxj;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

public class CashMxJApplication extends Application {

    // CONTROL DE ACTIVIDADES ABIERTAS
    private int actividadesIniciadas = 0;

    // CONTROL DE SEGURIDAD
    private boolean autenticacionRequerida = true;
    private boolean redireccionEnCurso = false;

    @Override
    public void onCreate() {
        super.onCreate();

        // CONTROL DEL CICLO DE VIDA
        // Y SEGURIDAD DE LA APLICACION
        registerActivityLifecycleCallbacks(
                new ActivityLifecycleCallbacks() {

                    @Override
                    public void onActivityCreated(
                            @NonNull Activity activity,
                            @Nullable Bundle savedInstanceState
                    ) {

                        // CONFIGURAR BARRAS DEL SISTEMA
                        configurarBarrasSistema(activity);

                        // EVITAR CAPTURAS Y GRABACIONES
                        // DE LAS PANTALLAS DE LA APP
                        activity.getWindow().addFlags(
                                WindowManager.LayoutParams.FLAG_SECURE
                        );
                    }

                    @Override
                    public void onActivityStarted(
                            @NonNull Activity activity
                    ) {

                        actividadesIniciadas++;

                        if (autenticacionRequerida) {
                            protegerActividad(activity);
                        }
                    }

                    @Override
                    public void onActivityResumed(
                            @NonNull Activity activity
                    ) {

                        if (autenticacionRequerida) {
                            protegerActividad(activity);
                        }
                    }

                    @Override
                    public void onActivityPaused(
                            @NonNull Activity activity
                    ) {
                        // SIN ACCIONES ADICIONALES
                    }

                    @Override
                    public void onActivityStopped(
                            @NonNull Activity activity
                    ) {

                        if (actividadesIniciadas > 0) {
                            actividadesIniciadas--;
                        }

                        // CUANDO LA APP QUEDA EN SEGUNDO PLANO
                        // SE REQUIERE AUTENTICACION AL REGRESAR
                        if (actividadesIniciadas == 0
                                && !activity.isChangingConfigurations()) {

                            autenticacionRequerida = true;
                            redireccionEnCurso = false;
                        }
                    }

                    @Override
                    public void onActivitySaveInstanceState(
                            @NonNull Activity activity,
                            @NonNull Bundle outState
                    ) {
                        // SIN ACCIONES ADICIONALES
                    }

                    @Override
                    public void onActivityDestroyed(
                            @NonNull Activity activity
                    ) {
                        // SIN ACCIONES ADICIONALES
                    }
                }
        );
    }

    // PROTEGER ACTIVIDADES NO AUTENTICADAS
    private void protegerActividad(Activity activity) {

        if (!autenticacionRequerida) {
            return;
        }

        // MAINACTIVITY CONTROLA EL ACCESO
        // MEDIANTE PIN O BIOMETRIA
        if (activity instanceof MainActivity) {
            return;
        }

        // EVITAR REDIRECCIONES DUPLICADAS
        if (redireccionEnCurso) {
            return;
        }

        redireccionEnCurso = true;

        Intent intent = new Intent(
                activity,
                MainActivity.class
        );

        intent.addFlags(
                Intent.FLAG_ACTIVITY_CLEAR_TOP
                        | Intent.FLAG_ACTIVITY_SINGLE_TOP
        );

        activity.startActivity(intent);
    }

    // CONSULTAR SI SE REQUIERE AUTENTICACION
    public boolean requiereAutenticacion() {

        return autenticacionRequerida;
    }

    // MARCAR AL USUARIO COMO AUTENTICADO
    public void marcarAutenticado() {

        autenticacionRequerida = false;
        redireccionEnCurso = false;
    }

    // BLOQUEAR LA APLICACION
    public void bloquearAplicacion() {

        autenticacionRequerida = true;
        redireccionEnCurso = false;
    }

    // CONFIGURACION GLOBAL DE BARRAS DEL SISTEMA
    private void configurarBarrasSistema(
            Activity activity
    ) {

        WindowCompat.setDecorFitsSystemWindows(
                activity.getWindow(),
                false
        );

        View contenido = activity.findViewById(
                android.R.id.content
        );

        if (contenido == null) {
            return;
        }

        final int paddingIzquierdo =
                contenido.getPaddingLeft();

        final int paddingSuperior =
                contenido.getPaddingTop();

        final int paddingDerecho =
                contenido.getPaddingRight();

        final int paddingInferior =
                contenido.getPaddingBottom();

        ViewCompat.setOnApplyWindowInsetsListener(
                contenido,
                (vista, insets) -> {

                    Insets barras = insets.getInsets(
                            WindowInsetsCompat.Type.systemBars()
                                    | WindowInsetsCompat.Type.displayCutout()
                    );

                    vista.setPadding(
                            paddingIzquierdo + barras.left,
                            paddingSuperior + barras.top,
                            paddingDerecho + barras.right,
                            paddingInferior + barras.bottom
                    );

                    return insets;
                }
        );

        ViewCompat.requestApplyInsets(
                contenido
        );
    }
}
