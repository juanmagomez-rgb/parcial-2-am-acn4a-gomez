package com.example.mispeliculas;

import org.junit.Test;
import java.util.List;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.ResponseBody;
import retrofit2.Response;
import static org.junit.Assert.*;

public class TmdbTest {
    @Test public void consultaYConversionRespetanContrato() throws Exception {
        OkHttpClient cliente = new OkHttpClient.Builder().addInterceptor(cadena -> {
            assertEquals("https", cadena.request().url().scheme());
            assertEquals("/3/search/movie", cadena.request().url().encodedPath());
            assertEquals("Spider Man", cadena.request().url().queryParameter("query"));
            assertEquals("es-AR", cadena.request().url().queryParameter("language"));
            assertEquals("false", cadena.request().url().queryParameter("include_adult"));
            assertEquals("Bearer prueba", cadena.request().header("Authorization"));
            return new okhttp3.Response.Builder().request(cadena.request()).protocol(Protocol.HTTP_1_1)
                    .code(200).message("OK").body(ResponseBody.create(MediaType.get("application/json"),
                            "{\"results\":[{\"id\":557,\"title\":\"Spider-Man\",\"release_date\":\"2002-05-01\","
                            + "\"overview\":\"Sinopsis de prueba\",\"poster_path\":\"/poster.jpg\"},"
                            + "null,{\"id\":0,\"title\":\"Inválida\"}]}" )).build();
        }).build();
        Response<Tmdb.Respuesta> respuesta = Tmdb.servicio(cliente)
                .buscar("Bearer prueba", "Spider Man", "es-AR", false).execute();
        List<Pelicula> lista = respuesta.body().peliculas();
        assertEquals(1, lista.size());
        Pelicula pelicula = lista.get(0);
        assertEquals(557, pelicula.getIdTmdb());
        assertEquals("2002-05-01", pelicula.getEstreno());
        assertEquals("Sinopsis de prueba", pelicula.getSinopsis());
        assertEquals("/poster.jpg", pelicula.getPoster());
        assertFalse(pelicula.isVista());
    }

    @Test public void respuestasVaciasYCamposOpcionalesSonSeguros() {
        com.google.gson.Gson gson = new com.google.gson.Gson();
        assertTrue(gson.fromJson("{}", Tmdb.Respuesta.class).peliculas().isEmpty());
        assertTrue(gson.fromJson("{\"results\":[]}", Tmdb.Respuesta.class).peliculas().isEmpty());
        Pelicula pelicula = gson.fromJson("{\"results\":[{\"id\":1,\"title\":\"Título\"}]}",
                Tmdb.Respuesta.class).peliculas().get(0);
        assertEquals("", pelicula.getEstreno());
        assertEquals("", pelicula.getSinopsis());
        assertNull(Tmdb.urlPoster(pelicula.getPoster()));
    }

    @Test public void posterSoloUsaHostHttpsDeTmdb() {
        assertEquals("https://image.tmdb.org/t/p/w500/abc.jpg", Tmdb.urlPoster("/abc.jpg"));
        for (String ruta : new String[]{null, "", "https://otro.test/a.jpg", "//otro/a.jpg", "/../a.jpg"}) {
            assertNull(Tmdb.urlPoster(ruta));
        }
    }

    @Test public void erroresHttpYConexionNoSeConfundenConExito() throws Exception {
        for (int codigo : new int[]{401, 429, 500}) {
            OkHttpClient cliente = new OkHttpClient.Builder().addInterceptor(cadena ->
                    new okhttp3.Response.Builder().request(cadena.request()).protocol(Protocol.HTTP_1_1)
                            .code(codigo).message("Error de prueba")
                            .body(ResponseBody.create(MediaType.get("application/json"), "{}" )).build()).build();
            Response<Tmdb.Respuesta> respuesta = Tmdb.servicio(cliente)
                    .buscar("Bearer prueba", "Título", "es-AR", false).execute();
            assertFalse(respuesta.isSuccessful());
            assertEquals(codigo, respuesta.code());
            assertNull(respuesta.body());
            respuesta.errorBody().close();
        }
        OkHttpClient cliente = new OkHttpClient.Builder().addInterceptor(cadena -> {
            throw new java.io.IOException("Sin conexión simulada");
        }).build();
        try {
            Tmdb.servicio(cliente).buscar("Bearer prueba", "Título", "es-AR", false).execute();
            fail("Debe informar el fallo de conexión");
        } catch (java.io.IOException esperado) { /* No es una lista vacía ni una respuesta válida. */ }
    }
}
