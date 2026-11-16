# SQLite, precarga e integridad de datos

## Decisión académica

Se utiliza SQLite nativo con SQLiteOpenHelper, enseñado en AC4-S0D44-clase-09.pdf, pp. 12–14. La transcripción del 06/10/2026, 1:14:04–1:15:55, desarrolla lectura con cursor y uso del helper desde la pantalla. No se agregaron Room ni dependencias.

Antes de modificar la persistencia se creó un ZIP local recuperable de 65 archivos del proyecto, verificado por hashes. Ese respaldo permanece fuera del repositorio; no es una copia de los datos privados del emulador ni se publica.

## Esquema actual

Base privada peliculas.db, versión 1; tabla peliculas:

| Campo | Definición |
|---|---|
| _id | INTEGER PRIMARY KEY AUTOINCREMENT |
| titulo | TEXT obligatorio, no vacío |
| vista | INTEGER obligatorio, restringido a 0 o 1 |
| calificacion | INTEGER opcional; NULL por defecto o entero entre 0 y 5 |

NULL distingue «sin calificar» de cero. Solo se reserva esa columna: no hay interfaz, API ni funcionalidad de estrellas. Los pósters se resuelven desde el catálogo; no se guardan bitmaps ni IDs de recursos en la base.

Pelicula conserva el ID de registro como atributo privado e inmutable. Los títulos duplicados reciben IDs distintos. UPDATE y DELETE usan `_id = ?`; no buscan por título. Listar ordena por ID para mantener el orden de alta.

## Inicialización y lista vacía

1. Sin base previa ni estado anterior recuperable, onCreate del helper crea la tabla y carga Interestelar pendiente, Shrek vista y Spider-Man pendiente: contador 3/1.
2. Con base existente, se cargan sus registros, aunque sean cero. Nunca se usa la condición lista vacía para reponer ejemplos.
3. Al eliminar todas y reabrir, la lista queda vacía; se puede volver a agregar manualmente.
4. Si una actualización desde la etapa temporal anterior entrega un Bundle válido y todavía no existe la base, se importa esa lista una vez, incluso si está vacía. Una base ya existente tiene precedencia y no se mezcla con el Bundle.

SQLite conserva las modificaciones entre sesiones. Borrar los datos de la aplicación elimina la base; Android también puede restaurar datos desde un respaldo del dispositivo, por lo que instalar un APK no implica por sí solo que la base sea nueva.

## Colección y vistas

MainActivity mantiene ArrayList<Pelicula> como representación en memoria. Alta, vista y eliminación se guardan primero; luego se actualizan objetos, tarjetas y contadores. Un error SQLite se informa sin simular que el cambio quedó guardado. Si falla la carga inicial, el alta queda deshabilitada para no ocultar una base inaccesible con nuevas entradas.

Recrear MainActivity vuelve a leer SQLite y dibuja una tarjeta por registro. Ya no se guarda una segunda lista completa en Bundle. El detalle mantiene Intent y extras y es de consulta. onDestroy cierra el helper, pero no se usa como evento de guardado.

El ejemplo de onUpgrade de clase 09 borra una tabla como demostración. Esta implementación no adopta ese borrado: una futura versión requiere una migración no destructiva explícita; las versiones no soportadas fallan de forma visible. No se probó una migración futura inexistente.

Las operaciones siguen la forma local y directa del ejemplo académico para listas pequeñas. Consultas o volúmenes grandes exigirían revisar el trabajo en segundo plano antes de ampliar el alcance.

## Validación

La suite vigente pasó 12 pruebas unitarias y 11 instrumentadas. Incluye precarga, reapertura, estados, eliminación parcial/total, alta posterior a vaciar, IDs de duplicados, recreación, rotación, navegación y restricciones de la columna reservada. Además se verificó una reapertura tras detener el proceso con 3 guardadas/1 vista. Ver la [matriz de validación](evidencia-y-validacion.md).

Las pruebas de almacenamiento usan una base con nombre exclusivo. Las de interfaz apartan la base original a archivos .antes-tests, trabajan sobre una base descartable y restauran la original. Ante una interrupción, revisar esos archivos antes de repetir; la suite no sobrescribe un respaldo pendiente.

[Captura real de la lista inicial](sqlite-evidencia/inicial.png). Los volcados XML y diagnósticos internos permanecen fuera de la entrega.
