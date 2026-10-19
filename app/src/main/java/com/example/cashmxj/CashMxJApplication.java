
package com.example.cashmxj;

import android.app.Activity;
import android.app.Application;
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

    private int actividadesIniciadas = 0;
    private boolean autenticacionRequerida = true;

    @Override
    public void onCreate() {
        super.onCreate();

        registerActivityLifecycleCallbacks(
                new ActivityLifecycleCallbacks() {

                    @Override
                    public void onActivityCreated(
                            @NonNull Activity activity,
                            @Nullable Bundle savedInstanceState) {

                        configurarBarrasSistema(activity);

                        // Evita capturas de pantalla y vistas
                        // previas con información financiera.
                        activity.getWindow().addFlags(
                                WindowManager.LayoutParams.FLAG_SECURE
                        );
                    }

                    @Override
                    public void onActivityStarted(
                            @NonNull Activity activity) {

                        actividadesIniciadas++;
                    }

                    @Override
                    public void onActivityResumed(
                            @NonNull Activity activity) {
                    }

                    @Override
                    public void onActivityPaused(
                            @NonNull Activity activity) {
                    }

                    @Override
                    public void onActivityStopped(
                            @NonNull Activity activity) {

                        if (actividadesIniciadas > 0) {
                            actividadesIniciadas--;
                        }

                        // Cuando ninguna actividad está visible,
                        // la aplicación pasó a segundo plano.
                        if (actividadesIniciadas == 0
                                && !activity.isChangingConfigurations()) {

                            autenticacionRequerida = true;
                        }
                    }

                    @Override
                    public void onActivitySaveInstanceState(
                            @NonNull Activity activity,
                            @NonNull Bundle outState) {
                    }

                    @Override
                    public void onActivityDestroyed(
                            @NonNull Activity activity) {
                    }
                }
        );
    }

    public boolean requiereAutenticacion() {
        return autenticacionRequerida;
    }

    public void marcarAutenticado() {
        autenticacionRequerida = false;
    }

    public void bloquearAplicacion() {
        autenticacionRequerida = true;
    }

    private void configurarBarrasSistema(Activity activity) {

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

        final int paddingIzquierdo = contenido.getPaddingLeft();
        final int paddingSuperior = contenido.getPaddingTop();
        final int paddingDerecho = contenido.getPaddingRight();
        final int paddingInferior = contenido.getPaddingBottom();

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

        ViewCompat.requestApplyInsets(contenido);
    }
}
