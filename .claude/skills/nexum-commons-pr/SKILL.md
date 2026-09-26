---
name: nexum-commons-pr
description: "Use when writing or updating a pull request description for nexum-spring-commons, or the message asking someone to review it. Triggers on 'escribe la descripción del PR', 'arma el PR', 'formato de PR', 'mensaje de revisión'. Enforces the fixed section format (English headings, Spanish content), local and consumer-project evidence (there is no deployed dev environment), the public API / contract section, and the SonarQube tables."
---

# Descripción de PR — nexum-spring-commons

Estándar de PR del proyecto. La plantilla vacía está en `.github/pull_request_template.md` y GitHub la carga al
abrir un PR; esta skill explica cómo llenarla.

**No hay entorno de desarrollo desplegado.** Es una librería: la evidencia es local (tests contra PostgreSQL real con
Testcontainers) y, cuando cambia la API pública o el JSON, una prueba en un **proyecto consumidor**. Esa prueba ocupa
el lugar que en un microservicio tiene "Pruebas en develop".

## 1. Formato fijo

Secciones en este orden, en Markdown listo para pegar:

```markdown
## Summary
- 2 a 4 bullets concretos: qué cambia y por qué.

## Public API / contract
- [x] Sin cambios en la API pública ni en el JSON
- [ ] Cambio compatible (se añade algo): versión menor
- [ ] Cambio incompatible: versión mayor, explicado en Key technical note y en el CHANGELOG

## Test plan
- [x] `./mvnw verify`: 29 unitarios y 11 `*IT` en verde
- [x] Tests nuevos: <cuáles y qué cubren>
- [ ] Prueba en proyecto consumidor (obligatoria si cambia la API pública o el JSON)

### Pruebas en local
<details> ... </details>

### Prueba en proyecto consumidor
<details> ... </details>   <!-- o: "No aplica: sin cambios en la API pública ni en el JSON." -->

## Key technical note
- Qué no resuelve el cambio, decisiones no obvias. Una pregunta concreta a quien revisa va al final.

## Checklist
- [x] `CHANGELOG.md` bajo `[Unreleased]`
- [x] `README.md` actualizado si cambia el uso o una propiedad
- [x] Rama propia contra `main`, sin archivos locales (`handoff/`, `graphify-out/`, `target/`)

## SonarQube
(tablas New Code y Overall Code, ver §4)
```

**La descripción es corta.** 2 a 4 bullets en Summary; notas técnicas solo si cambian la lectura del PR. No convertirla
en handoff ni en recorrido archivo por archivo.

## 2. Reglas de redacción

- **Encabezados en inglés, contenido en español.** No se traducen ni se renombran: `Summary`,
  `Public API / contract`, `Test plan`, `Key technical note`, `Checklist`, `SonarQube`.
- **"Ejecutar", no "correr"**, para comandos, tests, análisis o scripts. Nunca "corrida": es ejecución.
- Sin voseo, sin "usted", sin "che". No culpar a nadie; redactar en impersonal.
- **Nunca citar un archivo que no exista en el repositorio** o en otro repo accesible. Nada de rutas locales
  (`/Users/...`, `/tmp/...`, `handoff/`).
- **No mencionar los proyectos que originaron la librería.** Hablar de "proyectos con un patrón similar" o "un
  proyecto consumidor".
- La descripción sirve para cualquier revisor: no nombrar a una persona dentro de ella.

## 3. Evidencia

Cada bloque va **colapsado** con `<details>` y el `(expandir)` en el `<summary>`. El resumen (`X OK / 0 FAIL`) va
**fuera** del colapsable, en `Test plan`, para leerlo sin expandir.

### 3.1 Pruebas en local

```markdown
<details>
<summary>Evidencia local — PostgreSQL con Testcontainers (expandir)</summary>

**Commit probado:** `abc1234`

## Qué se ejecutó
`./mvnw verify`: unitarios, `*IT` contra PostgreSQL 16 real y cobertura JaCoCo.

## Salida relevante
```
[INFO] Tests run: 29, Failures: 0, Errors: 0, Skipped: 0
[INFO] Tests run: 11, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

## Resumen
| Paso | Resultado |
|---|---|
| Unitarios | 29 OK |
| Integración (`*IT`) | 11 OK |

</details>
```

Si el cambio corrige un defecto, indicar qué test falla sin la corrección (se comprobó revirtiéndola) y pasa con ella.

### 3.2 Prueba en proyecto consumidor

Obligatoria si cambia la API pública, una propiedad `nexum.commons.*` o el JSON. Procedimiento:

1. `./mvnw install -DskipTests` en la librería (versión `-SNAPSHOT` en `~/.m2`).
2. En un proyecto consumidor, usar esa versión y ejecutar su `./mvnw verify`.
3. Levantar el consumidor con la versión anterior y con la nueva, lanzar las mismas peticiones y comparar status y
   cuerpo (errores, listado paginado y sin paginar, `limit` grande, 401, 404, 429).

```markdown
<details>
<summary>Evidencia en proyecto consumidor — respuestas antes y después (expandir)</summary>

**Librería probada:** `0.1.0-SNAPSHOT` (commit `abc1234`)

## Qué se ejecutó
Descripción en prosa: qué proyecto (genérico), qué se migró, qué peticiones se compararon.

## Salida real
```
curl -s -X POST localhost:8080/api/v1/auth/register -H 'Content-Type: application/json' -d '{}'
< HTTP/1.1 400
{"code":"BAD_REQUEST_VALIDATION_ERROR","message":"Request validation failed", ...}
```

## Resumen
| Caso | Antes | Después |
|---|---|---|
| Validación | 400 | 400 |

</details>
```

- Tokens siempre como `Authorization: Bearer <REDACTED>`.
- Si no aplica, escribir en su lugar: `No aplica: sin cambios en la API pública ni en el JSON.`

### 3.3 Excepción: solo documentación

```markdown
> Excepción: PR de solo documentación; no requiere evidencia de pruebas ni tablas de SonarQube.
```

## 4. SonarQube en tablas

No se pegan capturas; se pegan las tablas con los valores reales y la instancia (`sonarqube:<versión>`, `<host>`).

```markdown
**Quality Gate:** OK. Instancia: `sonarqube:community`, `localhost:9000`.

#### New Code
| Métrica | Valor | Umbral |
|---|---|---|
| Coverage | XX % | ≥ 80 % ✅ |
| Bugs | X | = 0 ✅ |
| Vulnerabilities | X | = 0 ✅ |
| Security review rating | X | = A ✅ |
| Duplicated lines | X % | ≤ 3 % ✅ |
| Code smells | X | ≤ 15 ✅ |
| Lines of new code | X | — |

#### Overall Code
(misma tabla, con `Lines of code`)
```

- **New Code** mide lo que introduce este PR; **Overall** incluye lo anterior. Un New Code en rojo sin justificar no se
  aprueba. Una fila de Overall en rojo por deuda previa se anota junto al ❌ (`❌ (preexistente)`).
- Antes de pegar, confirmar que el periodo de *New Code* está anclado a `main`. Si reporta el 100 % del código como
  nuevo, es un falso positivo: corregir el periodo antes de pegar la tabla.
- Si no se pudo ejecutar el análisis, indicarlo como **pendiente** y no pedir revisión. Nunca inventar valores.
- Cómo obtener los valores: skill `nexum-commons-checks`.

## 5. Rama base

Siempre contra `main`, desde una rama propia (`feat/`, `fix/`, `docs/`, `chore/`). No hay ramas `release/*` ni
`develop` en este repositorio.

## 6. Mensaje pidiendo revisión

Siempre 3 líneas, sin bullets. No pedir revisión si falta la evidencia local, la prueba en consumidor (cuando aplica)
o las tablas de SonarQube.

```
Hola @<revisor>, comparto esta PR de nexum-spring-commons para revisión por favor.
[Verbo] <qué hace en una oración concreta: módulo, clase o propiedad tocada>[ — <impacto en la API: compatible / versión mayor>].
<URL del PR>
```

| Verbo | Cuándo |
|---|---|
| **Añade** | Funcionalidad nueva |
| **Corrige** | Defecto |
| **Actualiza** | Cambio sobre algo existente (documentación, dependencias, refactor) |

## 7. No mergear

Nunca mergear un PR con conversaciones abiertas o sin la aprobación de quien revisa. Tras el merge, si corresponde
una versión, seguir "Publicar una versión" en `CLAUDE.md`.
