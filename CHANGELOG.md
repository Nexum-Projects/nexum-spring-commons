# Changelog

Todos los cambios relevantes de la librería se anotan aquí. Formato basado en
[Keep a Changelog](https://keepachangelog.com/es-ES/1.1.0/) y versionado semántico.

## [Unreleased]

### Added

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
  interfaz `DataResponse`, como `record`. El JSON (`data`, `meta` y sus campos) está cubierto por un test de
  contrato.
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
