package com.example.mispeliculas;

import android.content.Intent;
import android.os.Bundle;
import android.database.sqlite.SQLiteException;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    EditText editPelicula;
    Button btnAgregar;
    LinearLayout layoutPeliculas;
    TextView txtContador;

    private static final String ESTADO_TITULOS = "peliculas.titulos";
    private static final String ESTADO_VISTAS = "peliculas.vistas";
    private PeliculasDbHelper base;
    private final ArrayList<Pelicula> peliculas = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editPelicula = findViewById(R.id.editPelicula);
        btnAgregar = findViewById(R.id.btnAgregar);
        layoutPeliculas = findViewById(R.id.layoutPeliculas);
        txtContador = findViewById(R.id.txtContador);

        base = new PeliculasDbHelper(this, recuperarEstadoAnterior(savedInstanceState));
        try {
            peliculas.addAll(base.listar());
        } catch (SQLiteException error) {
            mostrarErrorGuardado();
            btnAgregar.setEnabled(false);
        }
        mostrarPeliculas();

        btnAgregar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String nombrePelicula = editPelicula.getText().toString().trim();

                if (nombrePelicula.isEmpty()) {

                    Toast.makeText(
                            MainActivity.this,
                            R.string.mensaje_nombre_vacio,
                            Toast.LENGTH_SHORT
                    ).show();

                } else {

                    if (agregarPelicula(nombrePelicula)) {
                        editPelicula.setText("");
                    }
                }
            }
        });
    }

    private boolean agregarPelicula(String nombre) {
        final Pelicula pelicula;
        try {
            pelicula = base.agregar(nombre);
        } catch (SQLiteException error) {
            mostrarErrorGuardado();
            return false;
        }
        peliculas.add(pelicula);
        crearTarjeta(pelicula);
        actualizarContadores();
        return true;
    }

    private void mostrarPeliculas() {
        layoutPeliculas.removeAllViews();
        for (Pelicula pelicula : peliculas) {
            crearTarjeta(pelicula);
        }
        actualizarContadores();
    }

    private void crearTarjeta(Pelicula pelicula) {

        // Tarjeta principal de la película
        LinearLayout tarjetaPelicula = new LinearLayout(this);
        tarjetaPelicula.setOrientation(LinearLayout.HORIZONTAL);
        tarjetaPelicula.setPadding(20, 20, 20, 20);

        LinearLayout.LayoutParams parametrosTarjeta =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        parametrosTarjeta.setMargins(0, 10, 0, 10);
        tarjetaPelicula.setLayoutParams(parametrosTarjeta);

        // Imagen de la película
        ImageView imagenPelicula = new ImageView(this);

        imagenPelicula.setImageResource(pelicula.getImagenResId());
        imagenPelicula.setContentDescription(pelicula.getImagenResId() == R.drawable.ic_launcher_foreground
                ? getString(R.string.imagen_no_disponible)
                : getString(R.string.descripcion_poster, pelicula.getTitulo()));

        int anchoPoster = getResources().getDimensionPixelSize(R.dimen.poster_ancho);
        int altoPoster = getResources().getDimensionPixelSize(R.dimen.poster_alto);
        int margenPoster = getResources().getDimensionPixelSize(R.dimen.poster_margen);

        LinearLayout.LayoutParams parametrosImagen =
                new LinearLayout.LayoutParams(anchoPoster, altoPoster);

        parametrosImagen.setMargins(0, 0, margenPoster, 0);
        imagenPelicula.setLayoutParams(parametrosImagen);
        imagenPelicula.setScaleType(ImageView.ScaleType.CENTER_CROP);

        // Contenedor vertical para título, estado y botón
        LinearLayout datosPelicula = new LinearLayout(this);
        datosPelicula.setOrientation(LinearLayout.VERTICAL);

        LinearLayout.LayoutParams parametrosDatos =
                new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1
                );

        datosPelicula.setLayoutParams(parametrosDatos);

        // Título
        TextView tituloPelicula = new TextView(this);
        tituloPelicula.setText(pelicula.getTitulo());
        tituloPelicula.setTextSize(18);
        tituloPelicula.setTextColor(getColor(R.color.texto_principal));

        // Estado
        TextView estadoPelicula = new TextView(this);
        estadoPelicula.setText(pelicula.isVista() ? R.string.estado_vista : R.string.estado_pendiente);
        estadoPelicula.setTextSize(14);
        estadoPelicula.setTextColor(getColor(R.color.texto_secundario));

        // Botón
        Button btnVista = new Button(this);
        btnVista.setText(pelicula.isVista() ? R.string.boton_vista : R.string.boton_marcar_vista);
        btnVista.setEnabled(!pelicula.isVista());

        btnVista.setBackgroundTintList(
                getColorStateList(R.color.rojo_principal)
        );
        btnVista.setTextColor(getColor(R.color.white));

        btnVista.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                try {
                    base.marcarVista(pelicula.getId());
                } catch (SQLiteException error) {
                    mostrarErrorGuardado();
                    return;
                }
                pelicula.setVista(true);
                estadoPelicula.setText(R.string.estado_vista);

                btnVista.setText(R.string.boton_vista);
                btnVista.setEnabled(false);

                actualizarContadores();
            }
        });

        Button btnEliminar = new Button(this);
        btnEliminar.setText(R.string.boton_eliminar);

        btnEliminar.setBackgroundTintList(
                getColorStateList(R.color.superficie_boton)
        );
        btnEliminar.setTextColor(getColor(R.color.white));

        btnEliminar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                try {
                    base.eliminar(pelicula.getId());
                } catch (SQLiteException error) {
                    mostrarErrorGuardado();
                    return;
                }
                peliculas.remove(pelicula);
                layoutPeliculas.removeView(tarjetaPelicula);

                actualizarContadores();
            }
        });

        Button btnDetalle = new Button(this);
        btnDetalle.setText(R.string.boton_ver_detalle);
        btnDetalle.setBackgroundTintList(getColorStateList(R.color.superficie_boton));
        btnDetalle.setTextColor(getColor(R.color.white));
        btnDetalle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, DetallePeliculaActivity.class);
                intent.putExtra(DetallePeliculaActivity.EXTRA_TITULO, pelicula.getTitulo());
                intent.putExtra(DetallePeliculaActivity.EXTRA_VISTA, pelicula.isVista());
                intent.putExtra(DetallePeliculaActivity.EXTRA_IMAGEN, pelicula.getImagenResId());
                startActivity(intent);
            }
        });

        // Armamos la parte derecha
        datosPelicula.addView(tituloPelicula);
        datosPelicula.addView(estadoPelicula);
        datosPelicula.addView(btnVista);
        datosPelicula.addView(btnEliminar);
        datosPelicula.addView(btnDetalle);

        // Armamos la tarjeta completa
        tarjetaPelicula.addView(imagenPelicula);
        tarjetaPelicula.addView(datosPelicula);

        // Agregamos la tarjeta a "Mi lista"
        layoutPeliculas.addView(tarjetaPelicula);
    }

    private void actualizarContadores() {

        txtContador.setText(
                getString(
                        R.string.contador,
                        getCantidadPeliculas(),
                        getCantidadVistas()
                )
        );
    }

    int getCantidadPeliculas() {
        return peliculas.size();
    }

    int getCantidadVistas() {
        int vistas = 0;
        for (Pelicula pelicula : peliculas) {
            if (pelicula.isVista()) vistas++;
        }
        return vistas;
    }

    // Compatibilidad con un Bundle de etapa 3 al actualizar la app con una tarea abierta.
    // Solo se utiliza si SQLite debe crear la base por primera vez (incluso lista vacía).
    private ArrayList<Pelicula> recuperarEstadoAnterior(Bundle estado) {
        if (estado == null) return null;
        ArrayList<String> titulos = estado.getStringArrayList(ESTADO_TITULOS);
        boolean[] vistas = estado.getBooleanArray(ESTADO_VISTAS);
        if (titulos == null || vistas == null || titulos.size() != vistas.length) return null;
        ArrayList<Pelicula> anteriores = new ArrayList<>();
        for (int i = 0; i < titulos.size(); i++) {
            String titulo = titulos.get(i);
            if (titulo != null && !titulo.trim().isEmpty()) {
                anteriores.add(new Pelicula(titulo, CatalogoPeliculas.imagenParaTitulo(titulo), vistas[i]));
            }
        }
        return anteriores;
    }

    private void mostrarErrorGuardado() {
        Toast.makeText(this, R.string.error_guardado, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onDestroy() {
        if (base != null) base.close();
        super.onDestroy();
    }
}
