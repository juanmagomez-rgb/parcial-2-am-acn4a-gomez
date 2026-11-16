# Matriz de cumplimiento y validación

Estado consolidado al preparar el segundo parcial. Se distingue código implementado de requisitos administrativos todavía no verificados.

## Fuentes académicas

El material de cátedra permanece fuera del repositorio. Se cita por archivo, página y clase; no se redistribuyen los PDF ni transcripciones.

- Consigna: parcial 2 - aplicaciones moviles.pdf, leído completo: p. 1 entrega/nombre; p. 2 mínimos; p. 3 extras.
- AC4-S0D44-clase-05.pdf, pp. 4–7: layouts y orientación.
- AC4-S0D44-clase-06.pdf, pp. 4–10 y 16–17: Activities, Intent, extras, objetos y validación.
- AC4-S0D44-clase-09.pdf, pp. 12–14: SQLite, SQLiteOpenHelper, identificadores y consultas.
- TRANSCRIPCIONES_COMPLETAS_APLICACIONES_MOVILES_2026.md: 18/08, 1:04:57–1:05:46, objetos frente a interfaz; 08/09, 42:24–42:57 y 54:31–55:10, navegación; 15/09, 7:28–8:45, Intent explícito; 06/10, 1:14:04–1:15:55, lectura de registros y helper.

El compendio revisado reúne 11 clases completas y notas parciales del 20/10. Esas notas no son transcripción literal y no se afirma cobertura total de todas las clases.

## Obligatorios

| Requisito de la consigna | Evidencia del proyecto | Estado |
|---|---|---|
| Varias pantallas y navegación, p. 2 | MainActivity, DetallePeliculaActivity, Manifest y extras | Implementado y probado |
| Informe por pantalla, funcionalidades, flujo y mocks, p. 2 | pantallas-y-flujo.md, mocks iniciales identificados y capturas reales | Documentado; el mock no equivale a implementación |
| ConstraintLayout y LinearLayout vertical/horizontal, p. 2 | Layouts y tarjetas Java | Implementado |
| Button y TextView, p. 2 | Ambas pantallas | Implementado |
| Contenido real con imágenes correspondientes, p. 2 | Catálogo de tres películas, imágenes locales y fuentes de fichas | Contenido textual verificado; excepción visual de Spider-Man conservada por instrucción del estudiante |
| Evento y comportamiento dinámico, p. 2 | Alta, vista, eliminación, contadores y detalle | Implementado y probado |
| Commits progresivos visibles en GitHub, p. 2 | Cuatro originales preservados; organización actual en commits nuevos | Los commits locales nuevos no equivalen todavía a publicación remota ni simulan historial pasado |
| Dos integrantes/aportes de ambos, pp. 1–2 | Historial de un autor | Excepción individual pendiente de confirmación para esta entrega |
| Nombre, acceso docente y Campus, p. 1 | Repositorio separado parcial-2-am-acn4a-gomez | Nombre individual previsto; verificar excepción, incorporación de sergiomedinaio y entrega por Campus |

El PDF no fija una cantidad numérica de pantallas: se implementaron dos pantallas con una función real de consulta. Ningún extra compensa un mínimo faltante.

La transcripción del 01/09, 1:41:14–1:41:44, y la del 08/09, 1:15–2:10, permiten trabajo individual. Son anteriores al segundo parcial; se registra la diferencia con el PDF sin asumir una confirmación específica de esta entrega. La fecha válida es la publicada por el docente en Campus.

## Extras y límites

Recursos strings/dimens/colors, interacciones, datos entre Activities y extras están implementados. SQLite y fichas son mejoras locales aprobadas. La base reserva calificacion con restricción 0–5, pero no hay interfaz ni funcionalidad de estrellas. Firebase, API e imágenes remotas no se implementaron. Se conservan exactamente las imágenes originales.

## Verificación realizada

- assembleDebug: aprobado.
- testDebugUnitTest: 12 pruebas aprobadas (7 catálogo, 4 modelo y 1 plantilla).
- connectedDebugAndroidTest: repetición completa en la auditoría de publicación, 11 aprobadas, 0 fallas/errores/omitidas, FinanzIA_Phone Android 16/API 36.
- lintDebug: sin errores y 14 advertencias.
- git diff --check: sin errores.

Cobertura: agregar/vacío/manual, marcar vista, eliminar pendientes/vistas y todas, contadores, duplicados por ID, precarga única, persistencia al reabrir, recreación y rotación, navegación y regreso, extras inválidos, imágenes alternativas, fichas conocidas/desconocidas y scroll. La columna reservada se prueba como restricción SQL, no como función de calificación.

Los reportes Gradle se generan localmente en app/build/reports y se excluyen de Git. No se probó dispositivo físico ni todos los tamaños de pantalla. Los mocks no sustituyen las pruebas y no se atribuyen a capturas reales.
