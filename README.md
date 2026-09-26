# nexum-spring-commons

Librería para APIs REST con Spring Boot: respuestas, paginación, manejo de errores, rate limit y búsqueda.

> **Estado: en desarrollo** (`0.1.0-SNAPSHOT`). Todavía no hay una versión publicada y la API descrita en
> "Uso previsto" puede cambiar hasta `v1.0.0`.

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
| Autoconfiguración | `com.nexum.commons.autoconfigure` | Registra el manejador de errores y el rate limit; cada bean se puede reemplazar |

Estado: **errores** implementado; el resto, en desarrollo.

## Qué no incluye

La librería no incluye autenticación, JWT, configuración de Spring Security, entidades ni migraciones de base de
datos. Esas piezas dependen de cada aplicación.

## Requisitos

- Java 17 o superior.
- Spring Boot 4.0.x (se compila contra 4.0.6).
- La aplicación debe tener Spring Web MVC, Spring Data JPA y Spring Security. La librería los usa, pero no los
  arrastra como dependencias: la aplicación aporta las versiones que le corresponden.

## Instalación

Estará disponible desde la primera versión publicada, a través de [JitPack](https://jitpack.io). En el `pom.xml`
de la aplicación:

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
    <version>v1.0.0</version> <!-- usar la última versión publicada -->
  </dependency>
</dependencies>
```

## Uso previsto

> Ejemplos del diseño. Se confirman con cada módulo terminado.

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

**Un listado paginado.** El `mapper` se ejecuta dentro de la transacción del service, por eso el método es
`@Transactional(readOnly = true)`:

```java
@Transactional(readOnly = true)
public DataResponse<ProductResponseDTO> findMany(ProductQueryParamsDTO params) {
    return Paging.find(params, repository, ProductSpecifications.byParams(params), mapper::toResponse);
}
```

**Configuración del rate limit:**

```properties
nexum.commons.rate-limit.enabled=true
nexum.commons.rate-limit.paths=/api/v1/auth/
nexum.commons.rate-limit.auth-limit=10
nexum.commons.rate-limit.login-email-limit=5
nexum.commons.rate-limit.window-seconds=60
```

**Reemplazar un bean de la librería.** Si la aplicación define su propio `GlobalExceptionHandler`, el de la
librería no se registra. Un `@RestControllerAdvice` propio con mayor prioridad también gana sobre el de la
librería.

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

Requisitos: JDK 17 y Docker (los tests de integración usan Testcontainers con PostgreSQL).

```bash
./mvnw test      # tests unitarios (*Test), sin Docker
./mvnw verify    # unitarios + integración (*IT) + cobertura JaCoCo
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

Pendiente de definir antes de la primera versión publicada.
