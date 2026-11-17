package com.example.mispeliculas;

/** Datos de una entrada de la lista, independientes de los controles de pantalla. */
public class Pelicula {
    private final long id;
    private final String titulo;
    private final int imagenResId;
    private boolean vista;
    private final long idTmdb;
    private final String estreno;
    private final String sinopsis;
    private final String poster;

    public Pelicula(String titulo, int imagenResId, boolean vista) {
        this(0, titulo, imagenResId, vista);
    }

    public Pelicula(long id, String titulo, int imagenResId, boolean vista) {
        this(id, titulo, imagenResId, vista, 0, "", "", "");
    }

    public Pelicula(long id, String titulo, int imagenResId, boolean vista,
                    long idTmdb, String estreno, String sinopsis, String poster) {
        if (titulo == null || titulo.trim().isEmpty()) {
            throw new IllegalArgumentException("El título no puede estar vacío");
        }
        this.id = id;
        this.titulo = titulo.trim();
        this.imagenResId = imagenResId;
        this.vista = vista;
        this.idTmdb = Math.max(0, idTmdb);
        this.estreno = estreno == null ? "" : estreno.trim();
        this.sinopsis = sinopsis == null ? "" : sinopsis.trim();
        this.poster = poster == null ? "" : poster.trim();
    }

    public long getIdTmdb() { return idTmdb; }
    public String getEstreno() { return estreno; }
    public String getSinopsis() { return sinopsis; }
    public String getPoster() { return poster; }

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
