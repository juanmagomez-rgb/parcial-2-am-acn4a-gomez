# Fichas informativas locales

## Organización

Toda la información cinematográfica está en CatalogoPeliculas.java. Sus fichas son objetos Java inmutables con año, género y sinopsis; la identificación compartida del título mantiene las variantes previamente aceptadas para imagen y ficha.

Pelicula.java permanece independiente del catálogo: conserva ID, título, imagen y estado. DetallePeliculaActivity consulta la ficha con el título ya recibido; no se agregan extras ni datos de ficha a SQLite. El esquema, la columna de calificación y MainActivity no se modificaron al agregar la ficha.

El ScrollView existente contiene imagen, título, estado, año, género, Sinopsis y regreso. La sección informativa se oculta ante navegación inválida. Los títulos desconocidos muestran Descripción no disponible y ocultan año/género. No hay API, permisos de Internet ni descargas en la app.

## Contenido y fuentes verificadas

Las sinopsis son resúmenes propios en español basados en fuentes de los estudios; no se copian sus textos completos.

| Película | Datos de ficha | Fuentes |
|---|---|---|
| Interestelar | 2014; ciencia ficción, drama; Cooper participa en una expedición que busca un futuro para la humanidad | [Legendary](https://www.legendary.com/film/interstellar/) y [Paramount+](https://www.paramountplus.com/movies/video/pogPZ4svoLUp1fNjrr4osk0HOjN3r2b9/) |
| Shrek | 2001; animación, comedia, familiar; rescate de Fiona junto a Burro para recuperar el pantano | [Universal Pictures](https://www.universalpicturesathome.com/movies/shrek) |
| Spider-Man | 2002; acción, superhéroes; Peter Parker obtiene poderes y enfrenta al Duende Verde | [Sony Pictures](https://www.sonypictures.com/movies/spiderman) |

Todos los archivos originales de drawable se conservan exactamente. El estudiante indicó mantener spiderman.jpg: es una ilustración promocional con marca Disney+, no se presenta como póster oficial de la película de Sony de 2002. La ficha textual corresponde a esa película. No se descargaron, renombraron, reemplazaron ni eliminaron imágenes.

## Evidencia académica

- Consigna parcial 2 - aplicaciones moviles.pdf, p. 2: contenido real e imágenes.
- AC4-S0D44-clase-06.pdf, p. 16: clases, constructor y getters; pp. 10 y 17: datos pequeños y validación de extras.
- Transcripción 18/08/2026, 1:04:57–1:05:46: consultar los datos del objeto y representarlos en pantalla.

La organización como Ficha interna es una decisión propia aprobada, no un patrón exigido por el docente. La mejora no requiere que el material académico sea redistribuido.

## Validación y captura

Pruebas unitarias: datos de las tres fichas, variantes de títulos y ausencia de ficha para desconocidos o secuelas. Pruebas instrumentadas: recreación de los tres detalles, año/género/sinopsis/estado, scroll y regreso; mensaje alternativo para título libre y ocultamiento de ficha ante extras inválidos. También se mantuvieron las pruebas de navegación y persistencia SQLite. Ver [matriz](evidencia-y-validacion.md): 12 unitarias y 11 instrumentadas aprobadas.

[Captura real de Interestelar](fichas-evidencia/detalle.png). No es un mock. [Hashes SHA-256 de drawable](fichas-evidencia/drawable-sha256.json): los cinco archivos se mantuvieron idénticos. Las capturas de errores del sistema Android y sus volcados de diagnóstico se conservan solamente de forma local.
