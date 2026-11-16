# Mis Películas — Segundo parcial

Aplicación Android desarrollada en **Java y XML** para Aplicaciones Móviles, comisión ACN4A. Continúa el trabajo del primer parcial conservando sus cuatro commits originales.

Repositorio de esta entrega: [parcial-2-am-acn4a-gomez](https://github.com/juanmagomez-rgb/parcial-2-am-acn4a-gomez).
El [repositorio del primer parcial](https://github.com/juanmagomez-rgb/parcial-1-am-acn4a-gomez) permanece separado y sin cambios por esta publicación.

## Funcionalidades

- Agregar películas manualmente, listar, eliminar y marcar como vistas.
- Contadores calculados desde una colección de objetos `Pelicula`.
- Persistencia SQLite local con IDs únicos para distinguir títulos duplicados.
- Precarga, solo al crear una base nueva, de Interestelar pendiente, Shrek vista y Spider-Man pendiente: **3 guardadas y 1 vista**.
- Una lista vaciada por el usuario continúa vacía al reabrir; no se reponen ejemplos automáticamente.
- Detalle con imagen, estado, año, género y sinopsis del catálogo local. Títulos desconocidos muestran «Descripción no disponible».
- Navegación por Intent y extras; regreso mediante botón o Atrás sin crear otra MainActivity.
- Recreación y rotación sin duplicar registros ni contadores.

La base reserva una columna opcional de calificación entre 0 y 5. **No hay funcionalidad ni interfaz de estrellas.** No se incorporaron Firebase, API remota, Room ni dependencias nuevas.

## Ejecutar y verificar

Abrir este proyecto existente en Android Studio y sincronizar Gradle. Módulo `app`, variante `debug`, applicationId `com.example.mispeliculas`.

La configuración versionada utiliza compile/target SDK 37, minSdk 24, Gradle Wrapper 9.5.0 y toolchain de Gradle Java 25; el código se compila con compatibilidad Java 11. Configurar la ubicación del SDK en `local.properties` del equipo, que no se versiona.

En Windows, desde la raíz:

```powershell
.\gradlew.bat assembleDebug testDebugUnitTest lintDebug
.\gradlew.bat connectedDebugAndroidTest
```

La segunda orden requiere un emulador o dispositivo Android disponible. Las pruebas de interfaz protegen la base previa, utilizan una base descartable y la restauran al finalizar. Si una ejecución se interrumpe y quedan archivos `.antes-tests`, revisar su recuperación antes de repetirla.

Para instalar la variante debug con el emulador conectado:

```powershell
.\gradlew.bat installDebug
```

Validación del estado preparado para esta entrega: **compilación aprobada, 12 pruebas unitarias y 11 instrumentadas aprobadas en Android 16/API 36**. Lint terminó sin errores y con 14 advertencias. No se afirma validación en dispositivo físico.

## Documentación de la entrega

- [Pantallas, funcionalidades y flujo actual](docs/parcial-2/pantallas-y-flujo.md)
- [Matriz de cumplimiento y evidencia académica](docs/parcial-2/evidencia-y-validacion.md)
- [SQLite, precarga e integridad de datos](docs/parcial-2/sqlite-y-precarga.md)
- [Fichas, fuentes cinematográficas y validación](docs/parcial-2/fichas-informativas.md)
- [Índice de mocks y capturas](docs/parcial-2/README.md)

Los mocks son diseños iniciales; las capturas documentadas son imágenes reales del emulador. Los archivos originales de `drawable` se conservan exactamente. En particular, la ilustración actual de Spider-Man se mantiene por decisión del estudiante y no se presenta como póster oficial de la película de 2002.

## Condiciones de entrega por confirmar

Confirmar con el docente la excepción de trabajo individual y el nombre de entrega; verificar incorporación de `sergiomedinaio` y presentar el enlace por Campus en la fecha indicada allí. El código y sus pruebas no sustituyen esos requisitos administrativos.

Estos commits organizan trabajo terminado en la fecha de preparación; no simulan commits históricos del desarrollo. Los cuatro commits del primer parcial se conservan sin reescribirlos.
