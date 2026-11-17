package com.example.mispeliculas;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

/** SQLite conserva los datos y rutas remotas; los pósters locales se resuelven con el catálogo. */
public class PeliculasDbHelper extends SQLiteOpenHelper {
    static final String NOMBRE_BASE = "peliculas.db";
    private final ArrayList<Pelicula> datosAnteriores;

    public PeliculasDbHelper(Context context) {
        this(context, NOMBRE_BASE, null);
    }

    public PeliculasDbHelper(Context context, ArrayList<Pelicula> datosAnteriores) {
        this(context, NOMBRE_BASE, datosAnteriores);
    }

    PeliculasDbHelper(Context context, String nombreBase, ArrayList<Pelicula> datosAnteriores) {
        super(context.getApplicationContext(), nombreBase, null, 2);
        this.datosAnteriores = datosAnteriores;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE peliculas ("
                + "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "titulo TEXT NOT NULL CHECK(length(trim(titulo)) > 0), "
                + "vista INTEGER NOT NULL DEFAULT 0 CHECK(vista IN (0, 1)), "
                + "calificacion INTEGER DEFAULT NULL CHECK(calificacion IS NULL OR "
                + "(typeof(calificacion) = 'integer' AND calificacion BETWEEN 0 AND 5)), "
                + "tmdb_id INTEGER, estreno TEXT NOT NULL DEFAULT '', "
                + "sinopsis TEXT NOT NULL DEFAULT '', poster TEXT NOT NULL DEFAULT '')");
        // Solo se ejecuta al crear la base, nunca por encontrar la tabla vacía.
        if (datosAnteriores != null) {
            for (Pelicula pelicula : datosAnteriores) {
                insertar(db, pelicula.getTitulo(), pelicula.isVista());
            }
        } else {
            insertar(db, "Interestelar", false);
            insertar(db, "Shrek", true);
            insertar(db, "Spider-Man", false);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // SQLiteOpenHelper ejecuta la migración dentro de una transacción.
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE peliculas ADD COLUMN tmdb_id INTEGER");
            db.execSQL("ALTER TABLE peliculas ADD COLUMN estreno TEXT NOT NULL DEFAULT ''");
            db.execSQL("ALTER TABLE peliculas ADD COLUMN sinopsis TEXT NOT NULL DEFAULT ''");
            db.execSQL("ALTER TABLE peliculas ADD COLUMN poster TEXT NOT NULL DEFAULT ''");
        }
    }

    private long insertar(SQLiteDatabase db, String titulo, boolean vista) {
        ContentValues valores = new ContentValues();
        valores.put("titulo", titulo);
        valores.put("vista", vista ? 1 : 0);
        return db.insertOrThrow("peliculas", null, valores);
    }

    public Pelicula agregar(String titulo) {
        Pelicula nueva = new Pelicula(titulo, CatalogoPeliculas.imagenParaTitulo(titulo), false);
        long id = insertar(getWritableDatabase(), nueva.getTitulo(), nueva.isVista());
        return new Pelicula(id, nueva.getTitulo(), nueva.getImagenResId(), false);
    }

    public Pelicula agregar(Pelicula pelicula) {
        // Los títulos duplicados siguen permitidos: cada entrada tiene su propio _id.
        ContentValues valores = new ContentValues();
        valores.put("titulo", pelicula.getTitulo());
        valores.put("vista", pelicula.isVista() ? 1 : 0);
        if (pelicula.getIdTmdb() > 0) valores.put("tmdb_id", pelicula.getIdTmdb());
        valores.put("estreno", pelicula.getEstreno());
        valores.put("sinopsis", pelicula.getSinopsis());
        valores.put("poster", pelicula.getPoster());
        long id = getWritableDatabase().insertOrThrow("peliculas", null, valores);
        return new Pelicula(id, pelicula.getTitulo(), pelicula.getImagenResId(),
                pelicula.isVista(), pelicula.getIdTmdb(), pelicula.getEstreno(),
                pelicula.getSinopsis(), pelicula.getPoster());
    }

    public ArrayList<Pelicula> listar() {
        ArrayList<Pelicula> peliculas = new ArrayList<>();
        try (Cursor cursor = getReadableDatabase().query("peliculas",
                new String[]{"_id", "titulo", "vista", "tmdb_id", "estreno", "sinopsis", "poster"},
                null, null, null, null, "_id ASC")) {
            while (cursor.moveToNext()) {
                String titulo = cursor.getString(1);
                peliculas.add(new Pelicula(cursor.getLong(0), titulo,
                        cursor.getLong(3) > 0 ? R.drawable.ic_launcher_foreground
                                : CatalogoPeliculas.imagenParaTitulo(titulo), cursor.getInt(2) == 1,
                        cursor.getLong(3), cursor.getString(4), cursor.getString(5), cursor.getString(6)));
            }
        }
        return peliculas;
    }

    public void marcarVista(long id) {
        ContentValues valores = new ContentValues();
        valores.put("vista", 1);
        if (getWritableDatabase().update("peliculas", valores, "_id = ?",
                new String[]{String.valueOf(id)}) != 1) {
            throw new SQLiteException("La película ya no existe");
        }
    }

    public void eliminar(long id) {
        if (getWritableDatabase().delete("peliculas", "_id = ?",
                new String[]{String.valueOf(id)}) != 1) {
            throw new SQLiteException("La película ya no existe");
        }
    }
}
