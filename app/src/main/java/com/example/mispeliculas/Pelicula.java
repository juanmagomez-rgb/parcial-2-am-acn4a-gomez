package com.example.mispeliculas;

/** Datos de una entrada de la lista, independientes de los controles de pantalla. */
public class Pelicula {
    private final long id;
    private final String titulo;
    private final int imagenResId;
    private boolean vista;

    public Pelicula(String titulo, int imagenResId, boolean vista) {
        this(0, titulo, imagenResId, vista);
    }

    public Pelicula(long id, String titulo, int imagenResId, boolean vista) {
        if (titulo == null || titulo.trim().isEmpty()) {
            throw new IllegalArgumentException("El título no puede estar vacío");
        }
        this.id = id;
        this.titulo = titulo.trim();
        this.imagenResId = imagenResId;
        this.vista = vista;
    }

    public long getId() {
        return id;
    }

    public String getTitulo() {
        return titulo;
    }

    public int getImagenResId() {
        return imagenResId;
    }

    public boolean isVista() {
        return vista;
    }

    public void setVista(boolean vista) {
        this.vista = vista;
    }
}
