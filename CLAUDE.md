# CLAUDE.md

Guía para Claude Code (y cualquier agente) en este repositorio. Al empezar una sesión: skill
`/inicio-nexum-commons` (o [`INICIO.md`](INICIO.md)). Antes de dar un cambio por terminado: `/nexum-commons-checks`.
Descripción de un PR: `/nexum-commons-pr`. Cierre de sesión: `/nexum-commons-handoff`.
Ramas, hooks, commits y estándar de PR: [`CONTRIBUTING.md`](CONTRIBUTING.md).

## Qué es

Librería Spring Boot 4 / Java 17 con respuestas, paginación, manejo de errores, búsqueda y rate limit para APIs
REST. Se distribuye por JitPack (`com.github.Nexum-Projects:nexum-spring-commons:<tag>`). Paquete base
`com.nexum.commons`.

| Paquete | Contenido |
|---|---|
| `error` | `ErrorCode` (interfaz: `name()` + `httpStatus()`), `CommonErrorCode`, `BusinessException`, `NotFoundException`, `ErrorDTO`, `GlobalExceptionHandler` |
| `response` | `PageDTO`, `ListDTO`, `PageDetailDTO`, `PageMetaDTO`, `DataResponse` (todos `record`) |
| `pagination` | Parámetros base (tope de `limit` en 100) y `Paging.find(...)` |
| `search` | `SearchSpecificationUtils` (sin acentos con `unaccent`, comodines de `LIKE` escapados) |
| `ratelimit` | `RateLimiter`, `RateLimitFilter`, `RateLimitProperties` (`nexum.commons.rate-limit.*`) |
| `autoconfigure` | `CommonsAutoConfiguration`, registrada en `META-INF/spring/...AutoConfiguration.imports` |

## Comandos

```bash
./mvnw test                 # unitarios (*Test), sin Docker
./mvnw verify               # + integración (*IT, Testcontainers/PostgreSQL) + JaCoCo
./scripts/sonar-scan.sh     # SonarQube + Quality Gate (SONAR_HOST_URL y SONAR_TOKEN del entorno)
graphify update .           # actualizar el grafo local (graphify-out/, no versionado)
./mvnw install -DskipTests  # instalar en ~/.m2 para probar en un proyecto local
```

## Reglas

- **Contrato estable:** el JSON de errores (`code, message, statusCode, type, details`) y de respuestas (`data`,
  `meta` y sus campos) no cambia sin versión mayor. `ResponseJsonTest` y `GlobalExceptionHandlerTest` lo protegen.
- **Sin `@Component` ni `@Service`:** los beans se registran solo en `CommonsAutoConfiguration` con
  `@ConditionalOnMissingBean`. Los `*IT` usan `CommonsTestApplication`, cuyo escaneo apunta a un paquete vacío
  para que la librería llegue solo por autoconfiguración.
- **Dependencias de Spring opcionales** en el `pom`; los DTO sin anotaciones de Jackson.
- **Nada de dominio:** sin auth, JWT, `SecurityConfig`, entidades ni migraciones. Códigos de error de un módulo
  concreto van en la aplicación, en su propio enum que implemente `ErrorCode`.
- **Tests:** cada clase nueva con su test; primero el test que falla. Lo que toca base de datos va en un `*IT`
  contra PostgreSQL real, no H2.
- **Cambio mínimo:** sin abstracciones de un solo uso ni configuración que no varía.
- **Git:** rama propia; nunca commit ni push en `main` (lo bloquean los hooks de `.githooks/`, activos con
  `git config core.hooksPath .githooks`). Sin `git add -A` ni `git add .`. Sin commit ni push sin
  confirmación explícita.
- **Documentación:** en español, con `CHANGELOG.md` actualizado en el mismo cambio. No mencionar los proyectos
  que originaron la librería.

## Publicar una versión

1. `CHANGELOG.md`: mover `[Unreleased]` a la versión nueva.
2. Tag `vX.Y.Z` sobre `main`.
3. Pedir la construcción en `https://jitpack.io/#Nexum-Projects/nexum-spring-commons` y comprobar que termina bien.
4. Verificar la resolución desde un proyecto limpio (`-Dmaven.repo.local=<vacío>`).
