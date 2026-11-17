package com.example.mispeliculas;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;

/** Contrato REST y conversión de datos; no contiene credenciales reales. */
final class Tmdb {
    interface Servicio {
        @GET("search/movie")
        Call<Respuesta> buscar(@Header("Authorization") String token,
                              @Query("query") String titulo,
                              @Query("language") String idioma,
                              @Query("include_adult") boolean adultos);
    }

    static Servicio crear() {
        return servicio(new OkHttpClient.Builder().callTimeout(25, TimeUnit.SECONDS).build());
    }

    static Servicio servicio(OkHttpClient cliente) {
        return new Retrofit.Builder().baseUrl("https://api.themoviedb.org/3/")
                .client(cliente).addConverterFactory(GsonConverterFactory.create())
                .build().create(Servicio.class);
    }

    static String urlPoster(String ruta) {
        return ruta != null && ruta.matches("/[A-Za-z0-9._-]+\\.(jpg|png|webp)")
                ? "https://image.tmdb.org/t/p/w500" + ruta : null;
    }

    static class Respuesta {
        @SerializedName("results") List<Resultado> resultados;

        List<Pelicula> peliculas() {
            List<Pelicula> lista = new ArrayList<>();
            if (resultados != null) for (Resultado dato : resultados) {
                if (dato != null && dato.id > 0 && dato.titulo != null
                        && !dato.titulo.trim().isEmpty()) {
                    lista.add(new Pelicula(0, dato.titulo, R.drawable.ic_launcher_foreground,
                            false, dato.id, dato.estreno, dato.sinopsis, dato.poster));
                }
            }
            return lista;
        }
    }

    static class Resultado {
        long id;
        @SerializedName("title") String titulo;
        @SerializedName("release_date") String estreno;
        @SerializedName("overview") String sinopsis;
        @SerializedName("poster_path") String poster;
    }
}
