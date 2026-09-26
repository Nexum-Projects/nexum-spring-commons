---
name: nexum-commons-checks
description: "Use before considering a change to nexum-spring-commons done, before a commit, before opening a PR, or before a release. Triggers on 'ejecuta los tests', 'está listo?', 'valida el cambio', 'corre sonar', 'prepara el release'. Runs ./mvnw verify, SonarQube with Quality Gate via API, and graphify update, and reports PASS / FAIL / NOT RUN / BLOCKED per step."
---

# Validación — nexum-spring-commons

Versión para esta librería de la batería general `spring-boot-checks`. Ejecutar desde la raíz del repositorio.
**No afirmar PASS de un paso que no se ejecutó.**

## 1. Tests y cobertura

```bash
./mvnw verify
```

Unitarios (`*Test`) e integración (`*IT`, Testcontainers con PostgreSQL) más JaCoCo. Requiere Docker; sin Docker,
los `*IT` quedan BLOCKED: ejecutar al menos `./mvnw test` y decirlo. Ante un fallo, reportar solo la aserción o la
excepción relevante.

Los tests de contrato (`ResponseJsonTest`, `GlobalExceptionHandlerTest`) protegen el JSON público. Si fallan por un
cambio intencional, el cambio es **incompatible**: versión mayor y nota en el CHANGELOG.

## 2. SonarQube

Requiere `SONAR_HOST_URL` y `SONAR_TOKEN` exportados. Nunca imprimir ni guardar el token. Antes de reportar BLOCKED,
comprobar que es válido:

```bash
[ -n "$SONAR_TOKEN" ] && curl -s -u "$SONAR_TOKEN:" "$SONAR_HOST_URL/api/authentication/validate"
```

`{"valid":false}`: token vencido o revocado, o una sesión abierta antes de actualizarlo (reiniciar la sesión para
que tome el valor nuevo del perfil de la shell).

```bash
./scripts/sonar-scan.sh
```

El análisis se procesa en segundo plano: esperar a que termine la tarea antes de leer el gate.

```bash
curl -s -u "$SONAR_TOKEN:" "$SONAR_HOST_URL/api/ce/component?component=nexum-spring-commons" | jq -r '.current.status'
curl -s -u "$SONAR_TOKEN:" "$SONAR_HOST_URL/api/qualitygates/project_status?projectKey=nexum-spring-commons" | jq '.projectStatus'
```

Umbrales: cobertura ≥ 80 %, bugs = 0, vulnerabilidades = 0, security review A, duplicación ≤ 3 %,
code smells ≤ 15. Corregir lo que introduce el cambio; no arreglar deuda ajena al cambio.

## 3. Grafo

```bash
graphify update .
```

## 4. Antes de publicar una versión

Además de 1 a 3: instalar la librería (`./mvnw install -DskipTests`), probarla en un proyecto consumidor y comparar
sus respuestas HTTP con la versión anterior (errores, listados, 401, 404, 429). Ver "Migrar un proyecto existente"
en el README.

## Reporte

```
### Validation
- Tests (unit + IT): PASS — 29 unit, 11 IT
- Sonar: PASS — Quality Gate OK (cobertura 81,6 %, 0 issues)
- graphify: PASS
```

Los valores de Sonar van en la descripción del PR (tablas de la plantilla).
