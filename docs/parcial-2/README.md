# Documentación del segundo parcial

**Documento de la etapa local (16/11/2026), anterior a TMDB.** El informe definitivo queda pendiente de actualizar con búsqueda online, resultados personalizados y créditos; las capturas existentes corresponden a esa etapa local.

- [Informe de pantallas y flujo](pantallas-y-flujo.md)
- [Matriz de cumplimiento](evidencia-y-validacion.md)
- [SQLite y precarga](sqlite-y-precarga.md)
- [Fichas informativas y fuentes](fichas-informativas.md)

## Mocks iniciales

Son propuestas de diseño, anteriores a la ficha informativa; no son capturas ni prueban ejecución. El informe actual explica la evolución implementada.

- [Principal y detalle](mocks/pantallas.png)
- [Estados alternativos](mocks/estados.png)
- [Flujo de navegación](mocks/flujo.png)

El mensaje personalizado de lista vacía representado en el mock no está implementado: la lista vacía actual conserva formulario, contador cero y contenedor sin tarjetas.

## Capturas reales del emulador

- [Principal: tres películas y una vista](sqlite-evidencia/inicial.png)
- [Detalle: año, género, sinopsis y regreso](fichas-evidencia/detalle.png)
- [Hashes de drawable conservados](fichas-evidencia/drawable-sha256.json)

Las capturas corresponden a verificaciones previas documentadas de la misma implementación funcional. La última auditoría repitió compilación y las 11 pruebas instrumentadas sin modificar código.

Los diagnósticos de instalación, volcados XML, capturas de errores de Android y registros históricos de etapas permanecen locales y excluidos de Git. No forman parte de la entrega pública.
