package com.example.mispeliculas;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;
import androidx.test.runner.lifecycle.Stage;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.AfterClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.*;

/** Regresiones de lista y navegación usando las dependencias de prueba existentes. */
@RunWith(AndroidJUnit4.class)
public class NavegacionInstrumentedTest {
    private final Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
    private Activity principal;
    private Activity detalle;

    @BeforeClass
    public static void protegerBase() {
        RespaldoDbPruebas.guardar(InstrumentationRegistry.getInstrumentation().getTargetContext());
    }

    @AfterClass
    public static void restaurarBase() {
        RespaldoDbPruebas.restaurar(InstrumentationRegistry.getInstrumentation().getTargetContext());
    }

    @Before
    public void baseNuevaPorPrueba() {
        instrumentation.getTargetContext().deleteDatabase(PeliculasDbHelper.NOMBRE_BASE);
    }

    private Intent intent(Class<?> destino) {
        return new Intent(instrumentation.getTargetContext(), destino)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    }

    private MainActivity abrirPrincipal() {
        try (PeliculasDbHelper db = new PeliculasDbHelper(instrumentation.getTargetContext())) {
            for (Pelicula pelicula : db.listar()) db.eliminar(pelicula.getId());
        }
        return abrirPrincipalConDatos();
    }

    private MainActivity abrirPrincipalConDatos() {
        principal = instrumentation.startActivitySync(intent(MainActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK));
        instrumentation.waitForIdleSync();
        return (MainActivity) principal;
    }

    private void agregar(MainActivity activity, String nombre) {
        instrumentation.runOnMainSync(() -> {
            ((EditText) activity.findViewById(R.id.editPelicula)).setText(nombre);
            activity.findViewById(R.id.btnAgregar).performClick();
        });
    }

    private Button boton(MainActivity activity, int tarjeta, String texto) {
        LinearLayout lista = activity.findViewById(R.id.layoutPeliculas);
        LinearLayout fila = (LinearLayout) lista.getChildAt(tarjeta);
        LinearLayout datos = (LinearLayout) fila.getChildAt(1);
        for (int i = 0; i < datos.getChildCount(); i++) {
            View view = datos.getChildAt(i);
            if (view instanceof Button && ((Button) view).getText().toString().equals(texto)) {
                return (Button) view;
            }
        }
        throw new AssertionError("No se encontró el botón " + texto);
    }

    private void abrirDetalle(MainActivity activity) {
        Instrumentation.ActivityMonitor monitor = instrumentation.addMonitor(
                DetallePeliculaActivity.class.getName(), null, false);
        try {
            instrumentation.runOnMainSync(() -> boton(activity, 0, "Ver detalle").performClick());
            detalle = instrumentation.waitForMonitorWithTimeout(monitor, 5000);
            assertNotNull("No se abrió el detalle", detalle);
            instrumentation.waitForIdleSync();
        } finally {
            instrumentation.removeMonitor(monitor);
        }
    }

    private void comprobarPrincipal(MainActivity activity, int total, int vistas) {
        // finish() y Atrás retornan antes de que termine la transición de ciclo de vida.
        // Esperamos RESUMED de esta instancia, no solo que la cola principal esté libre.
        long limite = android.os.SystemClock.uptimeMillis() + 5000;
        boolean[] reanudada = {false};
        do {
            instrumentation.runOnMainSync(() -> reanudada[0] =
                    ActivityLifecycleMonitorRegistry.getInstance()
                            .getActivitiesInStage(Stage.RESUMED).contains(activity));
            if (reanudada[0]) break;
            android.os.SystemClock.sleep(50);
        } while (android.os.SystemClock.uptimeMillis() < limite);
        instrumentation.runOnMainSync(() -> {
            assertTrue("La misma MainActivity debe volver a RESUMED", ActivityLifecycleMonitorRegistry.getInstance()
                    .getActivitiesInStage(Stage.RESUMED).contains(activity));
            assertEquals(total, activity.getCantidadPeliculas());
            assertEquals(vistas, activity.getCantidadVistas());
            assertEquals(total, activity.layoutPeliculas.getChildCount());
            assertEquals(activity.getString(R.string.contador, total, vistas),
                    activity.txtContador.getText().toString());
        });
    }

    @After
    public void cerrar() {
        instrumentation.runOnMainSync(() -> {
            if (detalle != null) detalle.finish();
            if (principal != null) principal.finish();
        });
        instrumentation.waitForIdleSync();
    }

    @Test
    public void precargaReaperturaYEliminacionTotalPersisten() {
        MainActivity activity = abrirPrincipalConDatos();
        comprobarPrincipal(activity, 3, 1);
        comprobarTarjeta(activity, 0, "Interestelar", false);
        comprobarTarjeta(activity, 1, "Shrek", true);
        comprobarTarjeta(activity, 2, "Spider-Man", false);
        instrumentation.runOnMainSync(() -> {
            boton(activity, 0, "Marcar como vista").performClick();
            boton(activity, 1, "Eliminar").performClick();
            activity.finish();
        });
        instrumentation.waitForIdleSync();
        MainActivity reabierta = abrirPrincipalConDatos();
        comprobarPrincipal(reabierta, 2, 1);
        comprobarTarjeta(reabierta, 0, "Interestelar", true);
        comprobarTarjeta(reabierta, 1, "Spider-Man", false);
        abrirDetalle(reabierta);
        instrumentation.runOnMainSync(() -> detalle.findViewById(R.id.btnVolverLista).performClick());
        comprobarPrincipal(reabierta, 2, 1);
        instrumentation.runOnMainSync(() -> {
            boton(reabierta, 0, "Eliminar").performClick();
            boton(reabierta, 0, "Eliminar").performClick();
            reabierta.finish();
        });
        instrumentation.waitForIdleSync();
        MainActivity vacia = abrirPrincipalConDatos();
        comprobarPrincipal(vacia, 0, 0);
        agregar(vacia, "Manual después de vaciar");
        comprobarPrincipal(vacia, 1, 0);
        instrumentation.runOnMainSync(vacia::finish);
        instrumentation.waitForIdleSync();
        comprobarPrincipal(abrirPrincipalConDatos(), 1, 0);
    }

    @Test
    public void botonDetalleEsVisibleYTocableEnPantalla() {
        MainActivity activity = abrirPrincipal();
        onView(withId(R.id.editPelicula)).perform(replaceText("Interestelar"));
        onView(withId(R.id.btnAgregar)).perform(click());
        onView(withId(R.id.editPelicula)).perform(closeSoftKeyboard());
        // A diferencia de performClick(), Espresso exige visibilidad real y envía un toque.
        onView(withText(R.string.boton_ver_detalle)).perform(scrollTo())
                .check(matches(isDisplayed())).perform(click());
        onView(withId(R.id.txtDetalleTitulo)).check(matches(withText("Interestelar")));
        onView(withId(R.id.btnVolverLista)).perform(scrollTo(), click());
        comprobarPrincipal(activity, 1, 0);
    }

    @Test
    public void listaYRegresoConservanMismaInstanciaYContadores() {
        MainActivity activity = abrirPrincipal();
        agregar(activity, "   ");
        comprobarPrincipal(activity, 0, 0);
        agregar(activity, "Interestelar");
        agregar(activity, "Otra película");
        comprobarPrincipal(activity, 2, 0);
        abrirDetalle(activity);
        instrumentation.runOnMainSync(() -> {
            assertEquals("Interestelar", ((TextView) detalle.findViewById(
                    R.id.txtDetalleTitulo)).getText().toString());
            assertEquals(activity.getString(R.string.estado_pendiente),
                    ((TextView) detalle.findViewById(R.id.txtDetalleEstado)).getText().toString());
            detalle.findViewById(R.id.btnVolverLista).performClick();
        });
        comprobarPrincipal(activity, 2, 0);
        instrumentation.runOnMainSync(() -> boton(activity, 0, "Marcar como vista").performClick());
        abrirDetalle(activity);
        instrumentation.runOnMainSync(() -> assertEquals(activity.getString(R.string.estado_vista),
                ((TextView) detalle.findViewById(R.id.txtDetalleEstado)).getText().toString()));
        pressBack();
        comprobarPrincipal(activity, 2, 1);
        instrumentation.runOnMainSync(() -> {
            assertFalse(boton(activity, 0, "Vista").isEnabled());
            boton(activity, 1, "Eliminar").performClick();
        });
        comprobarPrincipal(activity, 1, 1);
        instrumentation.runOnMainSync(() -> boton(activity, 0, "Eliminar").performClick());
        comprobarPrincipal(activity, 0, 0);
        instrumentation.runOnMainSync(activity::recreate);
        comprobarPrincipal(esperarNuevaPrincipal(activity), 0, 0);
    }

    @Test
    public void recreacionYRotacionConservanDuplicadosOrdenYEstados() {
        MainActivity activity = abrirPrincipal();
        agregar(activity, "Shrek");
        agregar(activity, "Shrek");
        agregar(activity, "Título manual");
        instrumentation.runOnMainSync(() -> boton(activity, 1, "Marcar como vista").performClick());
        comprobarPrincipal(activity, 3, 1);

        instrumentation.runOnMainSync(activity::recreate);
        MainActivity recreada = esperarNuevaPrincipal(activity);
        comprobarPrincipal(recreada, 3, 1);
        comprobarTarjeta(recreada, 0, "Shrek", false);
        comprobarTarjeta(recreada, 1, "Shrek", true);
        comprobarTarjeta(recreada, 2, "Título manual", false);

        instrumentation.runOnMainSync(() -> recreada.setRequestedOrientation(
                android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE));
        MainActivity horizontal = esperarNuevaPrincipal(recreada);
        comprobarPrincipal(horizontal, 3, 1);
        comprobarTarjeta(horizontal, 1, "Shrek", true);
        instrumentation.runOnMainSync(() -> horizontal.setRequestedOrientation(
                android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT));
        MainActivity vertical = esperarNuevaPrincipal(horizontal);
        comprobarPrincipal(vertical, 3, 1);
        // Eliminar un duplicado pendiente no debe eliminar ni descontar el duplicado visto.
        instrumentation.runOnMainSync(() -> {
            // Alterar un control no debe alterar el estado ni los contadores del modelo.
            boton(vertical, 0, "Marcar como vista").setEnabled(false);
            assertEquals(1, vertical.getCantidadVistas());
            boton(vertical, 0, "Eliminar").performClick();
        });
        comprobarPrincipal(vertical, 2, 1);
        comprobarTarjeta(vertical, 0, "Shrek", true);
        abrirDetalle(vertical);
        instrumentation.runOnMainSync(() -> {
            assertEquals(vertical.getString(R.string.estado_vista),
                    ((TextView) detalle.findViewById(R.id.txtDetalleEstado)).getText().toString());
            detalle.findViewById(R.id.btnVolverLista).performClick();
        });
        comprobarPrincipal(vertical, 2, 1);
    }

    private MainActivity esperarNuevaPrincipal(MainActivity anterior) {
        MainActivity[] nueva = {null};
        long limite = android.os.SystemClock.uptimeMillis() + 10000;
        do {
            instrumentation.runOnMainSync(() -> {
                for (Activity activity : ActivityLifecycleMonitorRegistry.getInstance()
                        .getActivitiesInStage(Stage.RESUMED)) {
                    if (activity instanceof MainActivity && activity != anterior) {
                        nueva[0] = (MainActivity) activity;
                    }
                }
            });
            if (nueva[0] != null) break;
            android.os.SystemClock.sleep(50);
        } while (android.os.SystemClock.uptimeMillis() < limite);
        assertNotNull("La Activity debe recrearse y reanudarse", nueva[0]);
        principal = nueva[0];
        return nueva[0];
    }

    private void comprobarTarjeta(MainActivity activity, int indice, String titulo, boolean vista) {
        instrumentation.runOnMainSync(() -> {
            LinearLayout datos = (LinearLayout) ((LinearLayout) activity.layoutPeliculas
                    .getChildAt(indice)).getChildAt(1);
            assertEquals(titulo, ((TextView) datos.getChildAt(0)).getText().toString());
            assertEquals(activity.getString(vista ? R.string.estado_vista : R.string.estado_pendiente),
                    ((TextView) datos.getChildAt(1)).getText().toString());
            assertEquals(!vista, ((Button) datos.getChildAt(2)).isEnabled());
        });
    }

    @Test
    public void fichasSeMuestranSeRestauranYPermitenVolverConScroll() {
        String[] titulos = {"Interestelar", "Shrek", "Spider-Man"};
        int[] anios = {2014, 2001, 2002};
        String[] generos = {"Ciencia ficción, drama", "Animación, comedia, familiar", "Acción, superhéroes"};
        for (int i = 0; i < titulos.length; i++) {
            detalle = instrumentation.startActivitySync(intent(DetallePeliculaActivity.class)
                    .putExtra("titulo", titulos[i]).putExtra("vista", true));
            Activity anterior = detalle;
            instrumentation.runOnMainSync(anterior::recreate);
            Activity[] nueva = {null};
            long limite = android.os.SystemClock.uptimeMillis() + 10000;
            do {
                instrumentation.runOnMainSync(() -> {
                    for (Activity activity : ActivityLifecycleMonitorRegistry.getInstance()
                            .getActivitiesInStage(Stage.RESUMED)) {
                        if (activity instanceof DetallePeliculaActivity && activity != anterior) nueva[0] = activity;
                    }
                });
                if (nueva[0] != null) break;
                android.os.SystemClock.sleep(50);
            } while (android.os.SystemClock.uptimeMillis() < limite);
            assertNotNull("El detalle debe recrearse", nueva[0]);
            detalle = nueva[0];
            final int indice = i;
            instrumentation.runOnMainSync(() -> {
                assertEquals("Año de estreno: " + anios[indice],
                        ((TextView) detalle.findViewById(R.id.txtDetalleAnio)).getText().toString());
                assertEquals("Género: " + generos[indice],
                        ((TextView) detalle.findViewById(R.id.txtDetalleGenero)).getText().toString());
                String sinopsis = ((TextView) detalle.findViewById(R.id.txtDetalleSinopsis)).getText().toString();
                assertFalse(sinopsis.isEmpty());
                assertNotEquals(detalle.getString(R.string.descripcion_no_disponible), sinopsis);
                assertEquals(detalle.getString(R.string.estado_vista),
                        ((TextView) detalle.findViewById(R.id.txtDetalleEstado)).getText().toString());
            });
            onView(withId(R.id.txtDetalleSinopsis)).perform(scrollTo()).check(matches(isDisplayed()));
            onView(withId(R.id.btnVolverLista)).perform(scrollTo(), click());
            instrumentation.waitForIdleSync();
        }
    }

    @Test
    public void datosInvalidosMuestranErrorYPermitenVolver() {
        for (int caso = 0; caso < 4; caso++) {
            Intent intent = intent(DetallePeliculaActivity.class);
            if (caso == 1) intent.putExtra("titulo", "   ").putExtra("vista", false);
            if (caso == 2) intent.putExtra("titulo", "Interestelar");
            if (caso == 3) intent.putExtra("titulo", "Interestelar").putExtra("vista", "false");
            detalle = instrumentation.startActivitySync(intent);
            instrumentation.runOnMainSync(() -> {
                assertEquals(detalle.getString(R.string.error_detalle),
                        ((TextView) detalle.findViewById(R.id.txtDetalleTitulo)).getText().toString());
                assertEquals(View.GONE, detalle.findViewById(R.id.txtDetalleEstado).getVisibility());
                assertEquals(View.GONE, detalle.findViewById(R.id.imgDetallePoster).getVisibility());
                assertEquals(View.GONE, detalle.findViewById(R.id.layoutFicha).getVisibility());
                detalle.findViewById(R.id.btnVolverLista).performClick();
            });
            instrumentation.waitForIdleSync();
        }
    }

    @Test public void fichaRemotaGuardadaConservaExtrasYEstadoAlRecrear() {
        abrirPrincipal();
        instrumentation.runOnMainSync(() -> principal.finish());
        instrumentation.waitForIdleSync();
        // Datos controlados: no se presenta esta prueba como una consulta real a TMDB.
        try (PeliculasDbHelper db = new PeliculasDbHelper(instrumentation.getTargetContext())) {
            Pelicula pelicula = db.agregar(new Pelicula(0, "Shrek", R.drawable.ic_launcher_foreground,
                    false, 999, "2020-01-02", "Sinopsis remota guardada", ""));
            db.marcarVista(pelicula.getId());
        }
        MainActivity activity = abrirPrincipalConDatos();
        comprobarPrincipal(activity, 1, 1);
        instrumentation.runOnMainSync(activity::recreate);
        MainActivity recreada = esperarNuevaPrincipal(activity);
        comprobarPrincipal(recreada, 1, 1);
        abrirDetalle(recreada);
        instrumentation.runOnMainSync(() -> {
            assertEquals("Año de estreno: 2020", ((TextView) detalle.findViewById(R.id.txtDetalleAnio)).getText().toString());
            assertEquals("Sinopsis remota guardada", ((TextView) detalle.findViewById(R.id.txtDetalleSinopsis)).getText().toString());
            assertEquals(View.GONE, detalle.findViewById(R.id.txtDetalleGenero).getVisibility());
            assertEquals(999, detalle.getIntent().getLongExtra("idTmdb", 0));
            detalle.findViewById(R.id.btnVolverLista).performClick();
        });
        comprobarPrincipal(recreada, 1, 1);
        instrumentation.runOnMainSync(() -> boton(recreada, 0, "Eliminar").performClick());
        comprobarPrincipal(recreada, 0, 0);
    }

    @Test public void busquedaVaciaYTokenAusentePermitenAgregarManual() {
        MainActivity activity = abrirPrincipal();
        instrumentation.runOnMainSync(() -> {
            activity.findViewById(R.id.btnBuscar).performClick();
            assertEquals(activity.getString(R.string.mensaje_nombre_vacio),
                    ((TextView) activity.findViewById(R.id.txtBusqueda)).getText().toString());
            if (BuildConfig.TMDB_TOKEN.isEmpty()) {
                activity.editPelicula.setText("Shrek");
                activity.findViewById(R.id.btnBuscar).performClick();
                assertEquals(activity.getString(R.string.tmdb_sin_token),
                        ((TextView) activity.findViewById(R.id.txtBusqueda)).getText().toString());
            }
        });
        agregar(activity, "Mi película manual");
        comprobarPrincipal(activity, 1, 0);
    }

    @Test
    public void imagenAusenteDesconocidaOTipoIncorrectoUsaAlternativa() {
        for (int caso = 0; caso < 3; caso++) {
            Intent intent = intent(DetallePeliculaActivity.class)
                    .putExtra("titulo", "Título libre").putExtra("vista", false);
            if (caso == 1) intent.putExtra("imagenResId", -123);
            if (caso == 2) intent.putExtra("imagenResId", "imagen");
            detalle = instrumentation.startActivitySync(intent);
            instrumentation.runOnMainSync(() -> {
                assertEquals(View.VISIBLE, detalle.findViewById(R.id.txtSinImagen).getVisibility());
                assertEquals(detalle.getString(R.string.descripcion_no_disponible),
                        ((TextView) detalle.findViewById(R.id.txtDetalleSinopsis)).getText().toString());
                assertEquals(View.GONE, detalle.findViewById(R.id.txtDetalleAnio).getVisibility());
                assertEquals(View.GONE, detalle.findViewById(R.id.txtDetalleGenero).getVisibility());
                assertEquals("Título libre", ((TextView) detalle.findViewById(
                        R.id.txtDetalleTitulo)).getText().toString());
                detalle.finish();
            });
            instrumentation.waitForIdleSync();
        }
    }
}
