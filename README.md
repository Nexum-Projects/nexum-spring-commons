# nexum-spring-commons

Librería para APIs REST con Spring Boot: respuestas, paginación, manejo de errores, rate limit y búsqueda.

> **Versión actual:** `v1.4.0`. Cambios en [`CHANGELOG.md`](CHANGELOG.md).

## Por qué existe

Varios proyectos Spring Boot siguen un patrón similar y repetían el mismo código en cada uno: las envolturas de
respuesta, los parámetros de paginación, el manejo de errores y el límite de peticiones. Copiar ese código
significa que una corrección o una mejora hay que aplicarla en cada proyecto y que, con el tiempo, cada copia se
desvía de las demás.

Esta librería reúne ese código en un solo lugar. Se corrige una vez, se versiona, y todas las aplicaciones que la
usan responden igual.

## Qué incluye

| Módulo | Paquete | Contenido |
|---|---|---|
| Errores | `com.nexum.commons.error` | `ErrorCode`, `CommonErrorCode`, `BusinessException`, `NotFoundException`, `ErrorDTO` y un `GlobalExceptionHandler` que devuelve siempre el mismo formato de error |
| Respuestas | `com.nexum.commons.response` | `PageDetailDTO` (un recurso), `PageDTO` (lista paginada), `ListDTO` (lista sin paginar) y `PageMetaDTO` |
| Paginación | `com.nexum.commons.pagination` | Parámetros de consulta (`page`, `limit`, `order`, `orderBy`, `query`, `pagination`) con un tope de `limit`, y `Paging.find(...)` para armar un listado en una línea |
| Búsqueda | `com.nexum.commons.search` | Especificaciones de Spring Data JPA para filtrar por estado y buscar texto sin distinguir acentos |
| Rate limit | `com.nexum.commons.ratelimit` | Limitador de peticiones en memoria, configurable por propiedades |
| Seguridad | `com.nexum.commons.security` | `RestAuthenticationEntryPoint` (401) y `RestAccessDeniedHandler` (403) con el formato de error estándar, y `TokenUtils` para tokens de un solo uso |
| Username | `com.nexum.commons.username` | Regla de nombre de usuario tipo slug: anotación `@Username` y `UsernamePolicy`, activable y configurable por propiedades |
| Autoconfiguración | `com.nexum.commons.autoconfigure` | Registra el manejador de errores y el rate limit; cada bean se puede reemplazar |

Todos los módulos están implementados y probados.

## Qué no incluye

La librería no incluye autenticación, JWT, configuración de Spring Security, entidades ni migraciones de base de
datos. Esas piezas dependen de cada aplicación.

## Requisitos

- Java 17 o superior.
- Spring Boot 4.0.x (se compila contra 4.0.6).
- La aplicación debe tener Spring Web MVC, Spring Data JPA y Spring Security (y Bean Validation para `@Username`). La librería los usa, pero no los
  arrastra como dependencias: la aplicación aporta las versiones que le corresponden.

## Instalación

Se distribuye por [JitPack](https://jitpack.io). En el `pom.xml` de la aplicación:

```xml
<repositories>
  <repository>
    <id>jitpack.io</id>
    <url>https://jitpack.io</url>
  </repository>
</repositories>

<dependencies>
  <dependency>
    <groupId>com.github.Nexum-Projects</groupId>
    <artifactId>nexum-spring-commons</artifactId>
    <version>v1.4.0</version>
  </dependency>
</dependencies>
```

## Uso

**Códigos de error propios de cada aplicación.** El nombre del enum es el código que recibe el cliente y el estado
HTTP viaja con el código, así que no hace falta modificar el manejador de errores:

```java
public enum PurchaseErrorCode implements ErrorCode {
    PURCHASE_ALREADY_CONFIRMED(HttpStatus.CONFLICT);

    private final HttpStatus status;

    PurchaseErrorCode(HttpStatus status) { this.status = status; }

    @Override public HttpStatus httpStatus() { return status; }
}

throw new BusinessException(PurchaseErrorCode.PURCHASE_ALREADY_CONFIRMED, "La compra ya está confirmada");
```

Respuesta: `409` con el cuerpo

```json
{ "code": "PURCHASE_ALREADY_CONFIRMED", "message": "La compra ya está confirmada",
  "statusCode": 409, "type": "CONFLICT", "details": null }
```

**Un listado paginado.** Los parámetros de consulta extienden `BaseSortableQueryParamsDTO` (o
`BaseSearchableQueryParamsDTO` si hay búsqueda de texto) y declaran qué campos se pueden ordenar:

```java
public class ProductQueryParamsDTO extends BaseSearchableQueryParamsDTO {
    @Override protected String defaultOrderBy() { return "createdAt"; }
    @Override protected Set<String> allowedOrderByFields() { return Set.of("name", "createdAt"); }
    @Override protected Set<String> searchableFields() { return Set.of("name"); }
}
```

Valores por defecto: `page=1`, `limit=10` (máximo 100), `order=ASC`, `pagination=true`. Un `orderBy` fuera de la
lista responde 400. El `mapper` recibe entidades, así que el service es `@Transactional(readOnly = true)`; las
relaciones que use el DTO se cargan con `@EntityGraph` para no caer en N+1:

```java
@Transactional(readOnly = true)
public DataResponse<ProductResponseDTO> findMany(ProductQueryParamsDTO params) {
    return Paging.find(params, repository, ProductSpecifications.byParams(params), mapper::toResponse);
}
```

**Búsqueda de texto sin acentos.** Requiere la extensión `unaccent` de PostgreSQL (habilitarla en una migración:
`CREATE EXTENSION IF NOT EXISTS unaccent;`). El texto del usuario se trata como literal: `%` y `_` no actúan como
comodines.

```java
Specification<Product> spec = SearchSpecificationUtils.activeAndTextQuery(
        "isActive", true, params.getQuery(), params.getSearchableFields());
```

**Username tipo slug.** `@Username` valida el campo según las propiedades; `UsernamePolicy` normaliza el valor antes
de guardarlo. Por defecto: 3 a 32 caracteres, empieza con letra minúscula y sigue con minúsculas, dígitos o `_`
(`maria_hernandez23` sí; `Maria Lopez`, `maría`, `maria-hernandez` no).

```java
public record RegisterRequestDTO(@NotBlank @Username String name, ...) {}

user.setName(usernamePolicy.normalize(request.name()));   // trim + minúsculas
```

```properties
nexum.commons.username.enabled=true                    # false: acepta cualquier valor y normalize solo recorta
nexum.commons.username.pattern=^[a-z][a-z0-9_]{2,31}$
nexum.commons.username.message=Username must be 3-32 characters: start with a letter, then lowercase letters, digits or underscore
```

Se valida la forma normalizada: `" Maria_Lopez "` se acepta y el service guarda `maria_lopez`. `null` es válido para
`@Username`: combinarlo con `@NotBlank` si el campo es obligatorio. Si un frontend valida la
misma regla, cambiarla allí también al desactivarla o modificarla.

**Errores 401 y 403 con el mismo formato.** La autoconfiguración registra los dos handlers; la aplicación los
conecta en su `SecurityFilterChain`:

```java
http.exceptionHandling(e -> e
        .authenticationEntryPoint(restAuthenticationEntryPoint)   // 401 UNAUTHORIZED
        .accessDeniedHandler(restAccessDeniedHandler));           // 403 FORBIDDEN
```

En un filtro propio (por ejemplo el de JWT), `ErrorResponses.write(response, CommonErrorCode.UNAUTHORIZED, "…")`
escribe el mismo `ErrorDTO` sin armar el JSON a mano.

**Tokens de un solo uso** (reset de contraseña, verificación de email, refresh): se envía el valor aleatorio y en base
de datos se guarda su hash.

```java
String raw = TokenUtils.generateUrlSafeToken(32);   // 43 caracteres URL-safe
token.setTokenHash(TokenUtils.sha256Hex(raw));
```

**Rate limit.** Limita los `POST` bajo un prefijo por IP y ruta, y el login además por el email del cuerpo. Al
superar el límite responde `429` con `Retry-After` y el formato de error estándar. Valores por defecto:

```properties
nexum.commons.rate-limit.enabled=true
nexum.commons.rate-limit.path-prefix=/api/v1/auth/
nexum.commons.rate-limit.excluded-paths=/api/v1/auth/change-password
nexum.commons.rate-limit.login-path=/api/v1/auth/login
nexum.commons.rate-limit.auth-limit=10
nexum.commons.rate-limit.login-email-limit=5
nexum.commons.rate-limit.window-seconds=60
```

El contador vive en memoria: se pierde al reiniciar y no se comparte entre instancias. Detrás de un proxy, usar
`server.forward-headers-strategy=native` para que la IP sea la del cliente.

**Qué se registra solo.** En una aplicación servlet, la autoconfiguración añade `GlobalExceptionHandler` y
`RateLimitFilter`; no hace falta `@Import` ni `@ComponentScan`.

**Reemplazar o desactivar.** Si la aplicación declara su propio bean de tipo `GlobalExceptionHandler` (por ejemplo
una subclase que añade handlers), el de la librería no se registra. Un `@RestControllerAdvice` propio con mayor
prioridad también gana, porque el de la librería tiene la prioridad mínima. El rate limit se apaga con
`nexum.commons.rate-limit.enabled=false`.

## Migrar un proyecto existente

Para un proyecto que ya tiene su propia copia de estas clases:

1. Añadir la dependencia (ver Instalación).
2. Borrar las copias locales: envolturas de respuesta, parámetros de paginación, excepciones, `ErrorDTO`, el enum
   `ErrorCode`, `GlobalExceptionHandler`, la utilidad de búsqueda y el rate limit.
3. Cambiar los imports a `com.nexum.commons.*`.
4. Los códigos de error genéricos pasan a `CommonErrorCode`. Los que son propios de un módulo (por ejemplo
   `BAD_REQUEST_INVALID_CREDENTIALS` o `CONFLICT_EMAIL_ALREADY_EXISTS` en autenticación) van en un enum del módulo
   que implemente `ErrorCode`, con el mismo nombre para que el cliente reciba el mismo código.
5. Las envolturas son `record`: `getData()`, `getMeta()` y `getTotalPages()` pasan a `data()`, `meta()` y
   `totalPages()`. El JSON no cambia.
6. Las propiedades del rate limit pasan a `nexum.commons.rate-limit.*`.
7. Opcional: reemplazar cada `findMany` por `Paging.find(...)`.

Antes de desplegar, comparar las respuestas de la versión anterior y la migrada con las mismas peticiones (errores,
listados paginados y sin paginar, `limit` grande, 401, 404 y 429). Si el proyecto tenía un `GlobalExceptionHandler`
con handlers propios, conservarlos en una subclase del de la librería.

## Principios de diseño

Para quien contribuya:

- Los beans se registran en una clase `@AutoConfiguration` con `@ConditionalOnMissingBean`. Ninguna clase de la
  librería usa `@Component` ni `@Service`, para que el escaneo de componentes de una aplicación no las recoja por
  accidente.
- Las dependencias de Spring (Web, Data JPA, Security) son opcionales.
- Los DTO no llevan anotaciones de Jackson, para no depender de una versión concreta de Jackson.
- El formato JSON de los errores es un contrato estable: `{ code, message, statusCode, type, details }`.
- Un cambio incompatible solo entra en una versión mayor.

## Desarrollo

Para contribuir: [`CONTRIBUTING.md`](CONTRIBUTING.md) (ramas, hooks, commits, estándar de PR y skills de Claude Code).
Para retomar el trabajo: [`INICIO.md`](INICIO.md). Reglas del proyecto: [`CLAUDE.md`](CLAUDE.md).

Requisitos: JDK 17 y Docker (los tests de integración usan Testcontainers con PostgreSQL). Para el análisis
estático, SonarQube con `SONAR_HOST_URL` y `SONAR_TOKEN` exportados; para el grafo del código, `graphify`.

```bash
./mvnw test      # tests unitarios (*Test), sin Docker
./mvnw verify           # unitarios + integración (*IT) + cobertura JaCoCo
./scripts/sonar-scan.sh # SonarQube y Quality Gate
graphify update .       # grafo local del código (no se versiona)
```

Estructura:

```
src/main/java/com/nexum/commons/
  error/  response/  pagination/  search/  ratelimit/  autoconfigure/
src/main/resources/META-INF/spring/
  org.springframework.boot.autoconfigure.AutoConfiguration.imports
```

Cada clase nueva lleva su test. No se acepta un cambio con `./mvnw verify` en rojo. Los cambios se anotan en
[`CHANGELOG.md`](CHANGELOG.md).

### Versionado y publicación

Versionado semántico (`MAJOR.MINOR.PATCH`). Cada versión es un tag de git con el formato `vX.Y.Z`; JitPack
construye la librería la primera vez que se pide ese tag.

## Licencia

[MIT](LICENSE).
