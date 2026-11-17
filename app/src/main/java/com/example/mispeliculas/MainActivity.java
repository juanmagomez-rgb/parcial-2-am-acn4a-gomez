package com.example.mispeliculas;

import android.content.Intent;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
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
import androidx.appcompat.app.AlertDialog;
import com.bumptech.glide.Glide;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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
    private Call<Tmdb.Respuesta> consulta;
    private Button btnBuscar;
    private TextView txtBusqueda;
    private AlertDialog dialogo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editPelicula = findViewById(R.id.editPelicula);
        btnAgregar = findViewById(R.id.btnAgregar);
        layoutPeliculas = findViewById(R.id.layoutPeliculas);
        txtContador = findViewById(R.id.txtContador);
        btnBuscar = findViewById(R.id.btnBuscar);
        txtBusqueda = findViewById(R.id.txtBusqueda);
        btnBuscar.setOnClickListener(view -> buscarPeliculas());
        findViewById(R.id.txtCreditos).setOnClickListener(view -> mostrarCreditos());

        base = new PeliculasDbHelper(this, recuperarEstadoAnterior(savedInstanceState));
        try {
            peliculas.addAll(base.listar());
        } catch (SQLiteException error) {
            mostrarErrorGuardado();
            btnAgregar.setEnabled(false);
            btnBuscar.setEnabled(false);
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

    private void buscarPeliculas() {
        String titulo = editPelicula.getText().toString().trim();
        if (titulo.isEmpty()) { txtBusqueda.setText(R.string.mensaje_nombre_vacio); return; }
        if (BuildConfig.TMDB_TOKEN.isEmpty()) {
            txtBusqueda.setText(R.string.tmdb_sin_token);
            return;
        }
        btnBuscar.setEnabled(false);
        txtBusqueda.setText(R.string.tmdb_cargando);
        consulta = Tmdb.crear().buscar("Bearer " + BuildConfig.TMDB_TOKEN, titulo, "es-AR", false);
        consulta.enqueue(new Callback<Tmdb.Respuesta>() {
            @Override public void onResponse(Call<Tmdb.Respuesta> llamada, Response<Tmdb.Respuesta> respuesta) {
                if (isFinishing() || isDestroyed()) return;
                btnBuscar.setEnabled(true);
                if (!respuesta.isSuccessful()) {
                    int codigo = respuesta.code();
                    txtBusqueda.setText(codigo == 401 || codigo == 403 ? R.string.tmdb_token_invalido
                            : codigo == 429 ? R.string.tmdb_limite : R.string.tmdb_error);
                    return;
                }
                if (respuesta.body() == null) { txtBusqueda.setText(R.string.tmdb_error); return; }
                mostrarResultados(respuesta.body().peliculas());
            }
            @Override public void onFailure(Call<Tmdb.Respuesta> llamada, Throwable error) {
                if (llamada.isCanceled() || isFinishing() || isDestroyed()) return;
                btnBuscar.setEnabled(true);
                txtBusqueda.setText(error instanceof java.io.IOException
                        ? R.string.tmdb_sin_conexion : R.string.tmdb_error);
            }
        });
    }

    private void mostrarResultados(List<Pelicula> resultados) {
        if (resultados.isEmpty()) {
            txtBusqueda.setText(R.string.tmdb_sin_resultados);
            return;
        }
        txtBusqueda.setText(R.string.tmdb_seleccionar);

        View contenido = getLayoutInflater().inflate(R.layout.dialogo_resultados, null);
        ListView lista = contenido.findViewById(R.id.listaResultados);
        // Reserva espacio para el encabezado y Cancelar también en pantallas bajas.
        ViewGroup.LayoutParams medidas = lista.getLayoutParams();
        medidas.height = Math.min(getResources().getDimensionPixelSize(R.dimen.resultados_alto),
                (int) (getResources().getDisplayMetrics().heightPixels * 0.55f));
        lista.setLayoutParams(medidas);

        ArrayAdapter<Pelicula> adaptador = new ArrayAdapter<Pelicula>(
                this, R.layout.item_resultado, resultados) {
            @Override
            public View getView(int posicion, View vista, ViewGroup padre) {
                if (vista == null) {
                    vista = getLayoutInflater().inflate(R.layout.item_resultado, padre, false);
                }
                Pelicula pelicula = getItem(posicion);
                TextView titulo = vista.findViewById(R.id.txtResultadoTitulo);
                TextView anio = vista.findViewById(R.id.txtResultadoAnio);
                TextView sinopsis = vista.findViewById(R.id.txtResultadoSinopsis);
                ImageView poster = vista.findViewById(R.id.imgResultadoPoster);
                titulo.setText(pelicula.getTitulo());
                String estreno = pelicula.getEstreno();
                boolean tieneAnio = estreno.matches("[0-9]{4}-[0-9]{2}-[0-9]{2}");
                anio.setText(tieneAnio ? estreno.substring(0, 4) : "");
                anio.setVisibility(tieneAnio ? View.VISIBLE : View.GONE);
                sinopsis.setText(pelicula.getSinopsis().isEmpty()
                        ? getString(R.string.descripcion_no_disponible) : pelicula.getSinopsis());
                poster.setContentDescription(getString(R.string.descripcion_poster, pelicula.getTitulo()));
                Glide.with(MainActivity.this).load(Tmdb.urlPoster(pelicula.getPoster()))
                        .placeholder(R.drawable.ic_launcher_foreground)
                        .error(R.drawable.ic_launcher_foreground)
                        .fallback(R.drawable.ic_launcher_foreground).into(poster);
                return vista;
            }
        };
        lista.setAdapter(adaptador);

        dialogo = new AlertDialog.Builder(this, androidx.appcompat.R.style.Theme_AppCompat_Dialog_Alert)
                .setView(contenido)
                .setNegativeButton(R.string.boton_cancelar, null)
                .create();
        lista.setOnItemClickListener((padre, vista, posicion, id) -> {
            guardarPelicula(resultados.get(posicion));
            dialogo.dismiss();
        });
        dialogo.setOnShowListener(d -> {
            if (dialogo.getWindow() != null) {
                dialogo.getWindow().setBackgroundDrawableResource(R.color.fondo_app);
                dialogo.getWindow().setLayout(
                        Math.min(getResources().getDisplayMetrics().widthPixels
                                - getResources().getDimensionPixelSize(R.dimen.dialogo_margen),
                                getResources().getDimensionPixelSize(R.dimen.dialogo_ancho)),
                        ViewGroup.LayoutParams.WRAP_CONTENT);
            }
            dialogo.getButton(AlertDialog.BUTTON_NEGATIVE)
                    .setTextColor(getColor(R.color.rojo_principal));
        });
        dialogo.show();
    }

    private void mostrarCreditos() {
        dialogo = new AlertDialog.Builder(this, androidx.appcompat.R.style.Theme_AppCompat_Dialog_Alert)
                .setTitle(R.string.tmdb_creditos)
                .setView(getLayoutInflater().inflate(R.layout.dialogo_creditos, null))
                .setPositiveButton(R.string.boton_cerrar, null).create();
        dialogo.setOnShowListener(vista -> dialogo.getButton(AlertDialog.BUTTON_POSITIVE)
                .setTextColor(getColor(R.color.rojo_principal)));
        dialogo.show();
    }

    private void guardarPelicula(Pelicula seleccion) {
        try {
            Pelicula pelicula = base.agregar(seleccion);
            peliculas.add(pelicula);
            crearTarjeta(pelicula);
            actualizarContadores();
            txtBusqueda.setText(R.string.tmdb_agregada);
        } catch (SQLiteException error) { mostrarErrorGuardado(); }
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
        if (pelicula.getIdTmdb() > 0) {
            Glide.with(this).load(Tmdb.urlPoster(pelicula.getPoster()))
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .error(R.drawable.ic_launcher_foreground)
                    .fallback(R.drawable.ic_launcher_foreground).into(imagenPelicula);
            imagenPelicula.setContentDescription(getString(R.string.descripcion_poster, pelicula.getTitulo()));
        }

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
                if (pelicula.getIdTmdb() > 0) {
                    intent.putExtra("idTmdb", pelicula.getIdTmdb());
                    intent.putExtra("estreno", pelicula.getEstreno());
                    intent.putExtra("sinopsis", pelicula.getSinopsis());
                    intent.putExtra("poster", pelicula.getPoster());
                }
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
        if (consulta != null) consulta.cancel();
        if (dialogo != null) dialogo.dismiss();
        if (base != null) base.close();
        super.onDestroy();
    }
}
