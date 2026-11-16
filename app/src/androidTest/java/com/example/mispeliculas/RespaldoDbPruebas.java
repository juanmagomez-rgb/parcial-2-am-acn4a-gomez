package com.example.mispeliculas;

import android.content.Context;
import java.io.File;

/** Aparta la base previa de la app mientras la suite usa una base descartable. */
final class RespaldoDbPruebas {
    private static final String[] SUFIJOS = {"", "-journal", "-wal", "-shm"};
    private static boolean guardado;

    static void guardar(Context context) {
        guardado = false;
        for (String sufijo : SUFIJOS) {
            File original = context.getDatabasePath(PeliculasDbHelper.NOMBRE_BASE + sufijo);
            File respaldo = new File(original.getPath() + ".antes-tests");
            if (respaldo.exists()) throw new AssertionError("Hay un respaldo pendiente: " + respaldo);
        }
        for (String sufijo : SUFIJOS) {
            File original = context.getDatabasePath(PeliculasDbHelper.NOMBRE_BASE + sufijo);
            if (original.exists() && !original.renameTo(new File(original.getPath() + ".antes-tests"))) {
                throw new AssertionError("No se pudo proteger " + original);
            }
        }
        guardado = true;
    }

    static void restaurar(Context context) {
        // JUnit también llama @AfterClass si falla @BeforeClass: no borrar en ese caso.
        if (!guardado) return;
        context.deleteDatabase(PeliculasDbHelper.NOMBRE_BASE);
        for (String sufijo : SUFIJOS) {
            File original = context.getDatabasePath(PeliculasDbHelper.NOMBRE_BASE + sufijo);
            File respaldo = new File(original.getPath() + ".antes-tests");
            if (respaldo.exists() && !respaldo.renameTo(original)) {
                throw new AssertionError("Restaurar manualmente " + respaldo);
            }
        }
        guardado = false;
    }
}
