# Changelog

Todos los cambios relevantes de la librería se anotan aquí. Formato basado en
[Keep a Changelog](https://keepachangelog.com/es-ES/1.1.0/) y versionado semántico.

## [Unreleased]

### Fixed

- Un cuerpo JSON mal formado o con un valor de enum desconocido responde 400 (`BAD_REQUEST`, `Malformed request body`)
  en lugar de 500.

## [1.3.0] - 2026-09-26

### Added

- `ErrorResponses.write(...)`: escribe el `ErrorDTO` estándar desde filtros (JWT, rate limit) sin armar el JSON a mano.
- `RestAuthenticationEntryPoint` (401) y `RestAccessDeniedHandler` (403) con el formato de error estándar; la
  autoconfiguración los registra y la aplicación los conecta en su `SecurityFilterChain`.
- `TokenUtils`: token aleatorio URL-safe y hash SHA-256 para tokens de un solo uso.

### Changed

- Los beans de la autoconfiguración llevan el prefijo `nexumCommons` (`nexumCommonsGlobalExceptionHandler`,
  `nexumCommonsRateLimitFilter`, …): una aplicación que conserva sus propios beans con los nombres habituales ya no
  falla al arrancar por nombres duplicados.
- `@Username` valida la forma normalizada del valor (recortada y en minúsculas si la regla está activa): `Maria_Lopez`
  se acepta y el service guarda `maria_lopez`. Antes se rechazaba por las mayúsculas.

## [1.2.0] - 2026-09-26

### Added

- Módulo de username (`com.nexum.commons.username`): `@Username` (Bean Validation) y `UsernamePolicy` (normaliza a
  minúsculas sin espacios) con la regla de slug `^[a-z][a-z0-9_]{2,31}$` por defecto. Se activa, desactiva o cambia con
  `nexum.commons.username.*` sin tocar los DTO. Bean Validation queda como dependencia opcional.

## [1.1.0] - 2026-09-26

### Added

- CI con GitHub Actions: `./mvnw verify` en cada PR y en cada merge a `main`; el check `verify` es obligatorio para
  mergear.

### Fixed

- Los errores de binding de parámetros ya no exponen nombres de clases internas en `details.fieldErrors`: si el
  setter rechaza el valor con `IllegalArgumentException` se devuelve su mensaje (por ejemplo `Invalid orderBy field:
  password`); otros fallos, como un tipo incorrecto, devuelven `invalid value`.

## [1.0.0] - 2026-09-26

### Added

- Licencia MIT.
- Flujo de contribución: `CONTRIBUTING.md`, plantilla de PR, hooks de git en `.githooks/` (bloquean commit y push
  sobre `main`, `master`, `develop` y `release/*`) y skills de Claude Code del proyecto en `.claude/skills/`
  (`inicio-nexum-commons`, `nexum-commons-checks`, `nexum-commons-pr` con el estándar de PR, y
  `nexum-commons-handoff` con el formato de la bitácora de sesiones).
- Documentación para desarrollo: `CLAUDE.md` (reglas del proyecto) e `INICIO.md` (arranque de sesión: estado, grafo
  del código y validación). La bitácora de sesiones (`handoff/`) y el grafo (`graphify-out/`) quedan fuera de git.
- Análisis con SonarQube: `scripts/sonar-scan.sh` y propiedades de Sonar en el `pom`.
- Autoconfiguración (`CommonsAutoConfiguration`, registrada en `AutoConfiguration.imports`): en aplicaciones
  servlet registra `GlobalExceptionHandler` y `RateLimitFilter`, ambos reemplazables con
  `@ConditionalOnMissingBean`; el rate limit se desactiva por propiedad.
- Módulo de rate limit (`com.nexum.commons.ratelimit`): `RateLimiter` (ventana fija en memoria), `RateLimitFilter`
  (por IP y ruta, y el login además por email) y `RateLimitProperties` (`nexum.commons.rate-limit.*`), con el
  prefijo protegido, las rutas excluidas y los límites configurables.
- Módulo de búsqueda (`com.nexum.commons.search`): `SearchSpecificationUtils.activeAndTextQuery(...)` filtra por
  estado y busca texto en varios campos sin distinguir mayúsculas ni acentos (`unaccent` de PostgreSQL).
- Módulo de paginación (`com.nexum.commons.pagination`): `BaseQueryParamsDTO` (con tope de `limit` en 100),
  `BaseSortableQueryParamsDTO`, `BaseSearchableQueryParamsDTO` y `Paging.find(...)`, que arma un `PageDTO` o un
  `ListDTO` en una línea. Probado contra PostgreSQL con Testcontainers.
- Módulo de respuestas (`com.nexum.commons.response`): `PageDetailDTO`, `PageDTO`, `ListDTO`, `PageMetaDTO` y la
  interfaz `DataResponse`, como `record`. Los nombres y valores del JSON (`data`, `meta` y sus campos) están
  cubiertos por un test de contrato; el orden de las claves dentro de `meta` puede diferir de implementaciones
  anteriores, lo que no afecta a un cliente JSON.
- Módulo de errores (`com.nexum.commons.error`):
  - `ErrorCode`: interfaz que cada aplicación implementa con su propio enum; `name()` es el código que ve el
    cliente y `httpStatus()` el estado de la respuesta.
  - `CommonErrorCode`, `BusinessException`, `NotFoundException` (404) y `ErrorDTO`.
  - `GlobalExceptionHandler`: negocio, validación (con el detalle por campo), violación de unicidad, bloqueo
    optimista (409), acceso denegado, ruta inexistente, argumento inválido y errores inesperados (500 con
    mensaje genérico, y el detalle solo en el log).

- Esqueleto del proyecto: `pom.xml` con Spring Boot 4.0.6 y Java 17, dependencias de Spring opcionales,
  publicación de las fuentes, cobertura con JaCoCo, tests de integración con Testcontainers y configuración de
  JitPack (`jitpack.yml`).
- Documentación inicial: `README.md` con el propósito, el alcance, la instalación y el uso previsto.

### Fixed

- La búsqueda de texto escapa los comodines de `LIKE`: antes, una búsqueda con `_` o `%` devolvía filas que no
  coincidían.
- El rate limit toma la ruta de la URI sin el context path: con `getServletPath()` no limitaba nada si la
  aplicación cambiaba el mapeo del `DispatcherServlet` (`spring.mvc.servlet.path`).
- `ErrorCode` extiende `Serializable`, porque viaja dentro de `BusinessException`.
