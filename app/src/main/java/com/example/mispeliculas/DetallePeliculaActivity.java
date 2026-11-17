package com.example.mispeliculas;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;

public class DetallePeliculaActivity extends AppCompatActivity {

    public static final String EXTRA_TITULO = "titulo";
    public static final String EXTRA_VISTA = "vista";
    public static final String EXTRA_IMAGEN = "imagenResId";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_pelicula);

        Button volver = findViewById(R.id.btnVolverLista);
        volver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Cerramos el detalle: no creamos otra pantalla principal.
                finish();
            }
        });

        TextView titulo = findViewById(R.id.txtDetalleTitulo);
        TextView estado = findViewById(R.id.txtDetalleEstado);
        ImageView imagen = findViewById(R.id.imgDetallePoster);
        TextView sinImagen = findViewById(R.id.txtSinImagen);

        Bundle extras = getIntent().getExtras();
        // Comprobamos también los tipos para no interpretar un dato inválido como false.
        Object nombre = extras == null ? null : extras.get(EXTRA_TITULO);
        Object vista = extras == null ? null : extras.get(EXTRA_VISTA);
        if (!(nombre instanceof String) || ((String) nombre).trim().isEmpty()
                || !(vista instanceof Boolean)) {
            titulo.setText(R.string.error_detalle);
            estado.setVisibility(View.GONE);
            imagen.setVisibility(View.GONE);
            return;
        }

        String nombrePelicula = ((String) nombre).trim();
        titulo.setText(nombrePelicula);
        estado.setText((Boolean) vista ? R.string.estado_vista : R.string.estado_pendiente);

        // La ficha es contenido del catálogo, no estado editable ni dato persistido de Pelicula.
        findViewById(R.id.layoutFicha).setVisibility(View.VISIBLE);
        TextView anio = findViewById(R.id.txtDetalleAnio);
        TextView genero = findViewById(R.id.txtDetalleGenero);
        TextView sinopsis = findViewById(R.id.txtDetalleSinopsis);
        Object idTmdb = extras.get("idTmdb");
        if (idTmdb instanceof Long && (Long) idTmdb > 0) {
            mostrarRemota(extras, anio, genero, sinopsis, imagen, nombrePelicula);
            return;
        }
        CatalogoPeliculas.Ficha ficha = CatalogoPeliculas.fichaParaTitulo(nombrePelicula);
        if (ficha == null) {
            anio.setVisibility(View.GONE);
            genero.setVisibility(View.GONE);
            sinopsis.setText(R.string.descripcion_no_disponible);
        } else {
            anio.setText(getString(R.string.detalle_anio, ficha.getAnio()));
            genero.setText(getString(R.string.detalle_genero, ficha.getGenero()));
            sinopsis.setText(ficha.getSinopsis());
        }

        Object recurso = extras.get(EXTRA_IMAGEN);
        int imagenResId = recurso instanceof Integer ? (Integer) recurso : 0;
        if (imagenResId == R.drawable.interestelar || imagenResId == R.drawable.sherk
                || imagenResId == R.drawable.spiderman) {
            imagen.setImageResource(imagenResId);
            imagen.setContentDescription(getString(R.string.descripcion_poster, nombrePelicula));
        } else {
            // No intentamos cargar un identificador arbitrario o de otro tipo de recurso.
            imagen.setImageResource(R.drawable.ic_launcher_foreground);
            imagen.setContentDescription(getString(R.string.imagen_no_disponible));
            sinImagen.setVisibility(View.VISIBLE);
        }
    }

    private void mostrarRemota(Bundle datos, TextView anio, TextView genero,
                               TextView sinopsis, ImageView imagen, String titulo) {
        genero.setVisibility(View.GONE);
        String estreno = texto(datos, "estreno");
        if (estreno.matches("[0-9]{4}-[0-9]{2}-[0-9]{2}")) {
            anio.setText(getString(R.string.detalle_anio, Integer.parseInt(estreno.substring(0, 4))));
        } else anio.setVisibility(View.GONE);
        String descripcion = texto(datos, "sinopsis");
        sinopsis.setText(descripcion.isEmpty() ? getString(R.string.descripcion_no_disponible) : descripcion);
        Glide.with(this).load(Tmdb.urlPoster(texto(datos, "poster")))
                .placeholder(R.drawable.ic_launcher_foreground)
                .error(R.drawable.ic_launcher_foreground)
                .fallback(R.drawable.ic_launcher_foreground).into(imagen);
        imagen.setContentDescription(getString(R.string.descripcion_poster, titulo));
    }

    private String texto(Bundle datos, String clave) {
        Object valor = datos.get(clave);
        return valor instanceof String ? ((String) valor).trim() : "";
    }
}
