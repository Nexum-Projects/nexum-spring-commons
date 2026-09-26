# Contribuir a nexum-spring-commons

**Versión:** `v1.0`
**Fecha:** 2026-09-26

## Primer arranque

```bash
git clone https://github.com/Nexum-Projects/nexum-spring-commons.git
cd nexum-spring-commons
git config core.hooksPath .githooks   # activa los hooks del repositorio (una vez por clon)
./mvnw verify                         # requiere JDK 17 y Docker
graphify update .                     # opcional: grafo local del código
```

Después, [`INICIO.md`](INICIO.md) explica cómo retomar el trabajo y [`CLAUDE.md`](CLAUDE.md) las reglas del proyecto.

## Ramas

- `main` solo recibe cambios por Pull Request. Nunca se commitea ni se hace push directo sobre `main`, `master`,
  `develop` ni `release/*`.
- Cada cambio va en una rama propia desde `main` actualizado: `feat/<tema>`, `fix/<tema>`, `docs/<tema>`,
  `chore/<tema>`.

## Hooks de git

`.githooks/` contiene dos hooks que se activan con `git config core.hooksPath .githooks`:

| Hook | Qué bloquea |
|---|---|
| `pre-commit` | Un commit estando en `main`, `master`, `develop` o `release/*` |
| `pre-push` | Un push hacia esas ramas, incluido `git push origin HEAD:main` desde otra rama |

Funcionan igual para commits hechos a mano, desde el IDE o por un agente. Son una ayuda local: la protección
definitiva es la regla de rama protegida de GitHub sobre `main`. No se desactivan ni se saltan.

## Commits

[Conventional Commits](https://www.conventionalcommits.org/): título en inglés, cuerpo corto en español.

```
feat: add configurable rate limit module

RateLimitProperties permite cambiar el prefijo, las rutas excluidas y
los límites sin tocar código.
```

Tipos: `feat`, `fix`, `docs`, `test`, `refactor`, `chore`. Añadir los archivos uno a uno (`git add <ruta>`); no
usar `git add -A` ni `git add .`, para no subir por accidente archivos locales.

## Pull Requests

La plantilla ([`.github/pull_request_template.md`](.github/pull_request_template.md)) se carga sola al abrir un PR.
El estándar completo, con ejemplos de evidencia y el mensaje para pedir revisión, está en la skill
[`nexum-commons-pr`](.claude/skills/nexum-commons-pr/SKILL.md). Encabezados en inglés y contenido en español.

No hay entorno de desarrollo desplegado: la evidencia es local (`./mvnw verify` contra PostgreSQL con
Testcontainers) y, si cambia la API pública o el JSON, una prueba en un proyecto consumidor comparando sus
respuestas antes y después. Un PR no se revisa hasta que:

1. `./mvnw verify` está en verde en local y el check `verify` del CI ([`.github/workflows/ci.yml`](.github/workflows/ci.yml))
   pasa en el PR. GitHub no permite mergear sin ese check. En el primer PR de alguien de fuera, quien mantiene el
   repositorio aprueba la ejecución del CI.
2. Si cambia la API pública, una propiedad o el JSON, está la prueba en un proyecto consumidor.
3. SonarQube pasa el Quality Gate y sus valores están en las tablas (New Code y Overall Code). Si no se pudo
   ejecutar, se indica como pendiente; nunca se rellenan valores.
4. La sección *Public API / contract* dice si el cambio es compatible. Un cambio en el JSON de respuestas o de
   errores es incompatible y exige versión mayor.
5. `CHANGELOG.md` tiene la entrada bajo `[Unreleased]`.

La descripción es corta: qué cambia, por qué y lo que quien revisa debe mirar. Sin recorridos archivo por archivo.

## Skills de Claude Code incluidas

El repositorio trae skills en `.claude/skills/`, que Claude Code carga automáticamente al trabajar aquí:

| Skill | Cuándo |
|---|---|
| `/inicio-nexum-commons` | Al empezar una sesión: lee `CLAUDE.md`, `INICIO.md` y el último handoff local, y verifica el estado con git |
| `/nexum-commons-checks` | Antes de dar un cambio por terminado o abrir un PR: `./mvnw verify`, SonarQube con Quality Gate y grafo |
| `/nexum-commons-pr` | Al escribir la descripción de un PR o el mensaje para pedir revisión |
| `/nexum-commons-handoff` | Al cerrar una sesión: verifica el estado y escribe el handoff en `handoff/` (la crea si no existe) |

Otros agentes pueden seguir los mismos pasos leyendo esos `SKILL.md`, que están escritos como instrucciones.

## Archivos locales que no se suben

| Ruta | Qué es |
|---|---|
| `handoff/` | Bitácora personal de sesiones (ver `INICIO.md`) |
| `graphify-out/` | Grafo del código, se regenera con `graphify update .` |
| `target/` | Salida de Maven |

## Versiones

Versionado semántico. Cada versión es un tag `vX.Y.Z` sobre `main`; JitPack la construye al pedirla por primera
vez. Pasos en [`CLAUDE.md`](CLAUDE.md#publicar-una-versión).

---

## Changelog

| Versión | Fecha | Cambio |
|---|---|---|
| `v1.0` | 2026-09-26 | Versión inicial |
