package com.example.mispeliculas;

import java.util.Locale;

/** Contenido local verificado. Fuentes y alcance: docs/parcial-2/fichas-informativas.md. */
public final class CatalogoPeliculas {
    private static final Ficha INTERESTELAR = new Ficha(2014, "Ciencia ficción, drama",
            "Cooper, un expiloto, deja a su familia para participar en una expedición espacial "
                    + "que busca un futuro para la humanidad lejos de la Tierra.");
    private static final Ficha SHREK = new Ficha(2001, "Animación, comedia, familiar",
            "Para recuperar su pantano, el ogro Shrek acepta rescatar a la princesa Fiona. "
                    + "Acompañado por Burro, emprende una aventura que transforma su vida.");
    private static final Ficha SPIDER_MAN = new Ficha(2002, "Acción, superhéroes",
            "Tras la picadura de una araña, Peter Parker obtiene poderes extraordinarios. "
                    + "Decide usarlos para combatir el crimen y debe enfrentarse al Duende Verde.");

    public static final class Ficha {
        private final int anio;
        private final String genero;
        private final String sinopsis;

        private Ficha(int anio, String genero, String sinopsis) {
            this.anio = anio;
            this.genero = genero;
            this.sinopsis = sinopsis;
        }

        public int getAnio() { return anio; }
        public String getGenero() { return genero; }
        public String getSinopsis() { return sinopsis; }
    }

    private CatalogoPeliculas() {
    }

    public static int imagenParaTitulo(String titulo) {
        switch (identificar(titulo)) {
            case 1: return R.drawable.interestelar;
            case 2: return R.drawable.sherk;
            case 3: return R.drawable.spiderman;
            default: return R.drawable.ic_launcher_foreground;
        }
    }

    public static Ficha fichaParaTitulo(String titulo) {
        switch (identificar(titulo)) {
            case 1: return INTERESTELAR;
            case 2: return SHREK;
            case 3: return SPIDER_MAN;
            default: return null;
        }
    }

    private static int identificar(String titulo) {
        String clave = titulo == null ? "" : titulo.trim().toLowerCase(Locale.ROOT)
                .replaceAll("[\\s-]+", "");
        if (clave.equals("interestelar") || clave.equals("interstellar")) {
            return 1;
        }
        // Conservamos también el error de escritura aceptado por la versión anterior.
        if (clave.equals("shrek") || clave.equals("sherk")) {
            return 2;
        }
        if (clave.equals("spiderman") || clave.equals("elhombrearaña")
                || clave.equals("hombrearaña")) {
            return 3;
        }
        return 0;
    }
}
