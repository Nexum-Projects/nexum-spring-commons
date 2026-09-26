# Inicio de sesión — nexum-spring-commons

**Versión:** `v1.0`
**Fecha:** 2026-09-26

Pasos para retomar el trabajo en la librería, en una sesión nueva o después de un tiempo. En Claude Code, la skill
`/inicio-nexum-commons` los ejecuta. Las reglas del
proyecto están en [`CLAUDE.md`](CLAUDE.md); el uso para quien consume la librería, en [`README.md`](README.md).

## 1. Estado

```bash
git status
git branch --show-current
git log --oneline -10
```

`main` no recibe commits directos: se trabaja en una rama propia y se integra con un PR. Comprobar que los hooks
están activos (`git config core.hooksPath` debe devolver `.githooks`); ver [`CONTRIBUTING.md`](CONTRIBUTING.md).

## 2. Bitácora local (handoff)

Si existe `handoff/`, leer el archivo más reciente (`handoff/handoff_AAAA-MM-DD-HHMM.md`) y verificar lo que dice
contra el código y git antes de continuar. La carpeta está en `.gitignore`: es la bitácora personal de desarrollo
y no se sube. Al cerrar una sesión, dejar un handoff nuevo con la skill `/nexum-commons-handoff` (formato fijo:
objetivo, estado, decisiones vigentes, validación, bloqueos, failed attempts, pendientes y estado de git).

## 3. Grafo del código (graphify)

`graphify-out/` no se versiona. En un clon nuevo, o si falta:

```bash
graphify update .
```

Para preguntas de estructura, antes de leer todo el árbol: `graphify query "<pregunta>"`. Después de cambiar
código: `graphify update .`.

## 4. Validación antes de dar un cambio por terminado

| Paso | Comando | Requisito |
|---|---|---|
| Tests unitarios, integración y cobertura | `./mvnw verify` | Docker (Testcontainers) |
| SonarQube y Quality Gate | `./scripts/sonar-scan.sh` | `SONAR_HOST_URL` y `SONAR_TOKEN` exportados en el entorno |
| Grafo | `graphify update .` | graphify instalado |

En Claude Code, la skill `/nexum-commons-checks` ejecuta estos pasos y reporta el resultado.

- El token de Sonar se exporta en la sesión (`export SONAR_TOKEN=...`) y nunca se guarda en el repositorio.
  Antes de reportar Sonar como bloqueado, comprobar el token con
  `curl -s -u "$SONAR_TOKEN:" "$SONAR_HOST_URL/api/authentication/validate"`.
- Reportar cada paso como PASS, FAIL, NOT RUN o BLOCKED. No se afirma PASS de lo que no se ejecutó.
- Un cambio en el formato JSON de las respuestas o de los errores es un cambio incompatible: versión mayor.

## 5. Documentación que acompaña cada cambio

- `CHANGELOG.md`: entrada bajo `[Unreleased]`.
- `README.md`: si cambia el uso, una propiedad o un módulo.

---

## Changelog

| Versión | Fecha | Cambio |
|---|---|---|
| `v1.0` | 2026-09-26 | Versión inicial |
