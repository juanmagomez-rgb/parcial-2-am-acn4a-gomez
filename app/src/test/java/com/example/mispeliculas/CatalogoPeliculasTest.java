package com.example.mispeliculas;

import org.junit.Test;
import static org.junit.Assert.*;

public class CatalogoPeliculasTest {
    @Test
    public void fichasContienenDatosVerificadosDeLasTresPeliculas() {
        String[] titulos = {"Interestelar", "Shrek", "Spider-Man"};
        int[] anios = {2014, 2001, 2002};
        String[] generos = {"Ciencia ficción, drama", "Animación, comedia, familiar", "Acción, superhéroes"};
        String[] referencias = {"Cooper", "Fiona", "Duende Verde"};
        for (int i = 0; i < titulos.length; i++) {
            CatalogoPeliculas.Ficha ficha = CatalogoPeliculas.fichaParaTitulo(titulos[i]);
            assertNotNull(ficha);
            assertEquals(anios[i], ficha.getAnio());
            assertEquals(generos[i], ficha.getGenero());
            assertTrue(ficha.getSinopsis().contains(referencias[i]));
        }
    }

    @Test
    public void variantesCompartenLaMismaFicha() {
        assertSame(CatalogoPeliculas.fichaParaTitulo("Interestelar"),
                CatalogoPeliculas.fichaParaTitulo(" INTERSTELLAR "));
        assertSame(CatalogoPeliculas.fichaParaTitulo("Shrek"),
                CatalogoPeliculas.fichaParaTitulo("sherk"));
        assertSame(CatalogoPeliculas.fichaParaTitulo("Spider-Man"),
                CatalogoPeliculas.fichaParaTitulo("El Hombre Araña"));
    }

    @Test
    public void noInventaFichaParaDesconocidosNiSecuelas() {
        for (String titulo : new String[]{"Shrek 2", "Spider-Man 2", "Manual", "", null}) {
            assertNull(CatalogoPeliculas.fichaParaTitulo(titulo));
        }
    }

    @Test
    public void reconoceShrekYVarianteAnterior() {
        for (String titulo : new String[]{"Shrek", " SHREK ", "sherk"}) {
            assertEquals(R.drawable.sherk, CatalogoPeliculas.imagenParaTitulo(titulo));
        }
    }

    @Test
    public void reconoceInterestelarEnAmbosIdiomas() {
        for (String titulo : new String[]{"Interestelar", " INTERSTELLAR "}) {
            assertEquals(R.drawable.interestelar, CatalogoPeliculas.imagenParaTitulo(titulo));
        }
    }

    @Test
    public void reconoceSeparadoresDeSpiderMan() {
        for (String titulo : new String[]{"spiderman", "Spider-Man", "Spider Man", "El Hombre Araña", "Hombre Araña"}) {
            assertEquals(R.drawable.spiderman, CatalogoPeliculas.imagenParaTitulo(titulo));
        }
    }

    @Test
    public void noAsociaSecuelasNiTitulosDesconocidos() {
        for (String titulo : new String[]{"Shrek 2", "Mi película", "", "   ", null}) {
            assertEquals(R.drawable.ic_launcher_foreground, CatalogoPeliculas.imagenParaTitulo(titulo));
        }
    }
}
