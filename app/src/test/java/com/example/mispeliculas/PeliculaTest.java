package com.example.mispeliculas;

import org.junit.Test;
import static org.junit.Assert.*;

public class PeliculaTest {
    @Test
    public void conservaDatosYNormalizaSoloBordesDelTitulo() {
        Pelicula pelicula = new Pelicula("  Mi película  ", 42, false);
        assertEquals("Mi película", pelicula.getTitulo());
        assertEquals(42, pelicula.getImagenResId());
        assertFalse(pelicula.isVista());
        pelicula.setVista(true);
        pelicula.setVista(true);
        assertTrue(pelicula.isVista());
        pelicula.setVista(false);
        assertFalse(pelicula.isVista());
    }

    @Test(expected = IllegalArgumentException.class)
    public void rechazaTituloVacio() {
        new Pelicula("   ", 0, false);
    }

    @Test(expected = IllegalArgumentException.class)
    public void rechazaTituloNulo() {
        new Pelicula(null, 0, false);
    }

    @Test
    public void duplicadosRepresentanEntradasIndependientes() {
        Pelicula primera = new Pelicula("Shrek", 1, false);
        Pelicula segunda = new Pelicula("Shrek", 1, false);
        primera.setVista(true);
        assertFalse(segunda.isVista());
        assertNotEquals(primera, segunda);
    }
}
