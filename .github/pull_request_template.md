## Summary
<!-- Formato completo: skill .claude/skills/nexum-commons-pr. Encabezados en inglés, contenido en español.
     2 a 4 bullets: qué cambia y por qué. -->
-

## Public API / contract
<!-- Cambios en clases públicas, propiedades nexum.commons.* o JSON de respuestas y errores. -->
- [ ] Sin cambios en la API pública ni en el JSON
- [ ] Cambio compatible (se añade algo): versión menor
- [ ] Cambio incompatible: versión mayor, explicado abajo y en el CHANGELOG

## Test plan
- [ ] `./mvnw verify` en verde (unitarios + `*IT` con Testcontainers) y check `verify` del CI en verde
- [ ] Tests nuevos o actualizados para el cambio
- [ ] Probado en un proyecto consumidor con `./mvnw install` (obligatorio si cambia la API pública o el JSON)

<!-- No hay entorno de dev desplegado: la evidencia es local y, si aplica, en un proyecto consumidor. -->

### Pruebas en local

<details>
<summary>Evidencia local — PostgreSQL con Testcontainers (expandir)</summary>

**Commit probado:** `<sha>`

<!-- Qué se ejecutó, salida relevante y tabla de resumen. -->

</details>

### Prueba en proyecto consumidor

<!-- "No aplica: sin cambios en la API pública ni en el JSON." o el bloque <details> con las respuestas antes y después. -->

## Key technical note
<!-- Qué no resuelve este cambio, decisiones no obvias y preguntas concretas para quien revisa. Omitir si no hay. -->

## Checklist
- [ ] `CHANGELOG.md` actualizado bajo `[Unreleased]`
- [ ] `README.md` actualizado si cambia el uso o una propiedad
- [ ] Rama propia (no `main`) y sin archivos locales (`handoff/`, `graphify-out/`, `target/`)

## SonarQube

**Quality Gate:** OK / FAILED. Instancia: `sonarqube:<versión>`, `<host>`.

#### New Code

| Métrica | Valor | Umbral |
|---|---|---|
| Coverage | X % | ≥ 80 % |
| Bugs | X | = 0 |
| Vulnerabilities | X | = 0 |
| Security review rating | X | = A |
| Duplicated lines | X % | ≤ 3 % |
| Code smells | X | ≤ 15 |
| Lines of new code | X | — |

#### Overall Code

| Métrica | Valor | Umbral |
|---|---|---|
| Coverage | X % | ≥ 80 % |
| Bugs | X | = 0 |
| Vulnerabilities | X | = 0 |
| Security review rating | X | = A |
| Duplicated lines | X % | ≤ 3 % |
| Code smells | X | ≤ 15 |
| Lines of code | X | — |

<!-- Si no se pudo ejecutar el análisis, indicarlo como pendiente; no rellenar la tabla con valores inventados.
     PR solo de documentación: reemplazar esta sección por "Excepción: solo documentación". -->
