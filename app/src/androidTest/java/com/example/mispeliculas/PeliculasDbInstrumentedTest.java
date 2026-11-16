package com.example.mispeliculas;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.util.ArrayList;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class PeliculasDbInstrumentedTest {
    private final Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
    private static final String NOMBRE = "peliculas-pruebas-aisladas.db";

    @Before public void preparar() { context.deleteDatabase(NOMBRE); }
    @After public void limpiar() { context.deleteDatabase(NOMBRE); }

    @Test public void duplicadosTienenIdsUnicosYSeModificanPorId() {
        long segundo;
        try (PeliculasDbHelper db = new PeliculasDbHelper(context, NOMBRE, new ArrayList<>())) {
            Pelicula uno = db.agregar("Shrek");
            Pelicula dos = db.agregar("Shrek");
            segundo = dos.getId();
            assertTrue(uno.getId() > 0);
            assertTrue(dos.getId() > uno.getId());
            db.marcarVista(dos.getId());
            db.eliminar(uno.getId());
        }
        try (PeliculasDbHelper db = new PeliculasDbHelper(context, NOMBRE, null)) {
            ArrayList<Pelicula> lista = db.listar();
            assertEquals(1, lista.size());
            assertEquals(segundo, lista.get(0).getId());
            assertTrue(lista.get(0).isVista());
            db.eliminar(segundo);
            assertTrue(db.agregar("Otra").getId() > segundo);
        }
    }

    @Test public void calificacionReservadaAceptaNullYCeroACinco() {
        try (PeliculasDbHelper db = new PeliculasDbHelper(context, NOMBRE, null)) {
            db.listar();
            try (Cursor c = db.getReadableDatabase().rawQuery("SELECT calificacion FROM peliculas", null)) {
                assertTrue(c.moveToFirst());
                assertTrue(c.isNull(0));
            }
            db.getWritableDatabase().execSQL("UPDATE peliculas SET calificacion = 0");
            db.getWritableDatabase().execSQL("UPDATE peliculas SET calificacion = 5");
            for (String invalido : new String[]{"-1", "6", "2.5"}) {
                try {
                    db.getWritableDatabase().execSQL("UPDATE peliculas SET calificacion = " + invalido);
                    fail("Debe rechazar " + invalido);
                } catch (SQLiteConstraintException esperado) { /* Restricción de esquema comprobada. */ }
            }
            db.marcarVista(db.listar().get(0).getId());
            try (Cursor c = db.getReadableDatabase().rawQuery("SELECT calificacion FROM peliculas", null)) {
                assertTrue(c.moveToFirst());
                assertEquals(5, c.getInt(0));
            }
        }
    }

    @Test public void importaEstadoAnteriorUnaVezInclusoSiEstaVacio() {
        ArrayList<Pelicula> anteriores = new ArrayList<>();
        anteriores.add(new Pelicula("Mi película", R.drawable.ic_launcher_foreground, true));
        try (PeliculasDbHelper db = new PeliculasDbHelper(context, NOMBRE, anteriores)) {
            assertEquals(1, db.listar().size());
            assertTrue(db.listar().get(0).isVista());
            db.eliminar(db.listar().get(0).getId());
        }
        try (PeliculasDbHelper db = new PeliculasDbHelper(context, NOMBRE, anteriores)) {
            assertTrue(db.listar().isEmpty());
        }
        context.deleteDatabase(NOMBRE);
        try (PeliculasDbHelper db = new PeliculasDbHelper(context, NOMBRE, new ArrayList<>())) {
            assertTrue(db.listar().isEmpty());
        }
    }
}
