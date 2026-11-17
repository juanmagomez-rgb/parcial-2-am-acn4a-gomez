# Mis Películas — Segundo parcial

Aplicación Android desarrollada en **Java y XML** para Aplicaciones Móviles, comisión ACN4A.

Repositorio de esta entrega: [parcial-2-am-acn4a-gomez](https://github.com/juanmagomez-rgb/parcial-2-am-acn4a-gomez).

## Funcionalidades

- Agregar películas manualmente, listar, eliminar y marcar como vistas.
- Contadores calculados desde una colección de objetos `Pelicula`.
- Persistencia SQLite local con IDs únicos para distinguir títulos duplicados.
- Precarga, solo al crear una base nueva, de Interestelar pendiente, Shrek vista y Spider-Man pendiente: **3 guardadas y 1 vista**.
- Una lista vaciada por el usuario continúa vacía al reabrir; no se reponen ejemplos automáticamente.
- Detalle con imagen, estado, año, género y sinopsis del catálogo local. Títulos desconocidos muestran «Descripción no disponible».
- Navegación por Intent y extras; regreso mediante botón o Atrás sin crear otra MainActivity.
- Recreación y rotación sin duplicar registros ni contadores.

La base reserva una columna opcional de calificación entre 0 y 5. **No hay funcionalidad ni interfaz de estrellas.** No se incorporaron Firebase ni Room.

## Búsqueda opcional en TMDB

Escribir un título y pulsar **Buscar en TMDB** permite elegir una película de la primera página de resultados, en tarjetas oscuras con póster, título, año y sinopsis resumida. **Agregar película** conserva el agregado manual. Retrofit 2.11.0 y su conversor Gson consultan por HTTPS, de forma asíncrona, con idioma `es-AR`; Glide 4.16.0 carga pósters con caché, imagen de carga y alternativa ante errores. No se reemplazan los archivos de imágenes locales.

Para habilitar la búsqueda:

1. Copiar `tmdb.properties.example` a `tmdb.properties` en la raíz del proyecto.
2. En la configuración API de tu cuenta [TMDB](https://www.themoviedb.org/settings/api), obtener el **API Read Access Token** y escribir `tokenTmdb=TU_TOKEN`, sin comillas ni prefijo `Bearer`.
3. Sincronizar Gradle, recompilar e instalar el APK. Sin token, el resto de la aplicación funciona y la búsqueda informa que falta configurar la credencial.

`tmdb.properties` está excluido de Git. La credencial se incorpora al APK mediante BuildConfig: **puede extraerse de una aplicación cliente**; no debe considerarse completamente secreta. No compartir ese archivo ni publicar APK con una credencial personal sin revisar su uso.

SQLite versión 2 agrega columnas para ID de TMDB, fecha de estreno, sinopsis y ruta del póster mediante `ALTER TABLE`: conserva IDs locales, estados, eliminaciones y calificaciones previas, sin recargar ejemplos. Los duplicados siguen la regla existente: cada entrada tiene un ID local independiente. Los datos guardados permiten consultar el detalle sin Internet; las imágenes remotas dependen de la caché de Glide y muestran una alternativa si no están disponibles. La búsqueda requiere conexión y no se reanuda automáticamente al rotar; puede repetirse, sin agregar películas por sí sola. El detalle remoto muestra año y sinopsis; no inventa géneros ausentes en la búsqueda.

Fuentes de implementación: clase 13, pp. 4–5 y 10–11 (red, Retrofit, Glide y caché), y clase 09, pp. 12–14 (SQLiteOpenHelper). La migración conserva los registros, a diferencia del ejemplo destructivo del PDF. Son los extras de contenido e imágenes desde URL de la consigna, p. 3.

Datos e imágenes remotos: [TMDB](https://www.themoviedb.org). This product uses the TMDB API but is not endorsed or certified by TMDB. La pantalla principal permite abrir **Créditos · TMDB**, con el logo oficial, el aviso y el enlace a la fuente, según sus [requisitos de atribución](https://developer.themoviedb.org/docs/faq). Contratos consultados: [búsqueda](https://developer.themoviedb.org/reference/search-movie), [autenticación](https://developer.themoviedb.org/docs/authentication-application) e [imágenes](https://developer.themoviedb.org/docs/image-basics).

## Ejecutar y verificar

Abrir este proyecto existente en Android Studio y sincronizar Gradle. Módulo `app`, variante `debug`, applicationId `com.example.mispeliculas`.

La configuración versionada utiliza compile/target SDK 37, minSdk 24, Gradle Wrapper 9.5.0 y toolchain de Gradle Java 25; el código se compila con compatibilidad Java 11. Configurar la ubicación del SDK en `local.properties` del equipo, que no se versiona.

En Windows, desde la raíz:

```powershell
.\gradlew.bat assembleDebug testDebugUnitTest lintDebug
.\gradlew.bat connectedDebugAndroidTest
```

La segunda orden requiere un emulador o dispositivo Android disponible. Las pruebas de interfaz protegen la base previa, utilizan una base descartable y la restauran al finalizar. Si una ejecución se interrumpe y quedan archivos `.antes-tests`, revisar su recuperación antes de repetirla.

Gradle está configurado para conservar los APK y datos al finalizar (`android.injected.androidTest.leaveApksInstalledAfterRun=true`). Antes de probar sobre un dispositivo con datos importantes, conservar además un respaldo externo de su base; el respaldo interno de la suite no protege frente a una desinstalación.

La prueba online de Glide es optativa y se omite sin el argumento `tmdbOnline=true`. Para ejecutarla con Internet:

```powershell
.\gradlew.bat connectedDebugAndroidTest '-Pandroid.testInstrumentationRunnerArguments.class=com.example.mispeliculas.GlideInstrumentedTest' '-Pandroid.testInstrumentationRunnerArguments.tmdbOnline=true'
```

Para instalar la variante debug con el emulador conectado:

```powershell
.\gradlew.bat installDebug
```

Validación de la integración: **compilación, 16 pruebas unitarias y 16 instrumentadas aprobadas en Android 16/API 36**, incluyendo la prueba online optativa de Glide (carga HTTPS y recuperación desde caché). Lint sin errores y con 14 advertencias. Las pruebas de Retrofit usan respuestas controladas; las de SQLite verifican migración no destructiva, persistencia y estados. También se verificaron en el emulador la búsqueda autenticada de Shrek, su póster, el detalle y la persistencia tras cerrar y reabrir. La interfaz de resultados se verificó visualmente, incluido desplazamiento y Cancelar. En este cierre se ejecutó únicamente `assembleDebug`; las cifras de pruebas corresponden a la ejecución anterior a los últimos ajustes visuales. No se afirma validación en dispositivo físico ni conservación comprobada de la base anterior al primer intento: el ejecutor todavía desinstalaba el APK al terminar; luego se corrigió esa configuración. En la ejecución final, la app siguió instalada y la base coincidió byte por byte con su respaldo externo inmediatamente anterior.

## Documentación de la entrega

La documentación de abajo describe la etapa local previa a TMDB; queda pendiente actualizar el informe de pantallas y sus capturas con búsqueda, resultados personalizados y créditos. No hay un informe Word/PDF definitivo actualizado dentro del proyecto.

- [Pantallas, funcionalidades y flujo actual](docs/parcial-2/pantallas-y-flujo.md)
- [Matriz de cumplimiento y evidencia académica](docs/parcial-2/evidencia-y-validacion.md)
- [SQLite, precarga e integridad de datos](docs/parcial-2/sqlite-y-precarga.md)
- [Fichas, fuentes cinematográficas y validación](docs/parcial-2/fichas-informativas.md)
- [Índice de mocks y capturas](docs/parcial-2/README.md)

Los mocks son diseños iniciales; las capturas documentadas son imágenes reales del emulador. Los archivos originales de `drawable` se conservan exactamente. En particular, la ilustración actual de Spider-Man se mantiene por decisión del estudiante y no se presenta como póster oficial de la película de 2002.

El logo local `logo_tmdb.xml` conserva geometría y colores del [SVG oficial de TMDB](https://www.themoviedb.org/assets/v4/logos/v2/blue_short-8e7b30f73a4020692ccca9c88bafe5dcb6f8a62a4c6bc55cd9ba82bb2cd95f6c.svg), convertido a VectorDrawable para mostrarlo también sin conexión.
