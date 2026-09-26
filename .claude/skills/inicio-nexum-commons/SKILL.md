---
name: inicio-nexum-commons
description: "Use at the start of a session on nexum-spring-commons, or when asked '/inicio-nexum-commons', 'inicio commons', 'retoma la librería', or to continue from a handoff of this repository. Loads CLAUDE.md, INICIO.md and the latest local handoff, then verifies the real state against git before doing anything."
---

# Inicio — nexum-spring-commons

Punto de arranque para retomar el trabajo en la librería. Las fuentes están en la raíz del repositorio:

| Archivo | Contenido | En git |
|---|---|---|
| `CLAUDE.md` | Qué es la librería, paquetes, comandos y reglas | Sí |
| `INICIO.md` | Orden de arranque y validación | Sí |
| `CONTRIBUTING.md` | Ramas, hooks, commits y estándar de PR | Sí |
| `handoff/handoff_*.md` | Bitácora personal de sesiones | No (`.gitignore`) |

## Qué hacer

1. Leer completos `CLAUDE.md` e `INICIO.md`.
2. Leer el handoff más reciente, si existe:
   ```bash
   ls -1t handoff/handoff_*.md 2>/dev/null | head -1
   ```
   Si no hay `handoff/`, es un clon nuevo o la persona no lleva bitácora: continuar con el paso 3.
3. Verificar el estado real antes de afirmar nada del handoff:
   ```bash
   git status --short
   git branch --show-current
   git log --oneline -10
   git config core.hooksPath   # debe ser .githooks
   ```
   Si `core.hooksPath` no es `.githooks`, activarlo (`git config core.hooksPath .githooks`) y avisarlo.
4. Grafo: si falta `graphify-out/graph.json`, ejecutar `graphify update .`. Para preguntas de estructura, usar
   `graphify query "<pregunta>"` antes de leer el árbol completo.
5. Resumir en pocas líneas: rama, último commit, qué dice el handoff que falta y qué difiere del estado real.

## Reglas mientras se trabaja

- No tocar código ni proponer un plan hasta haber hecho los pasos 1 a 3.
- El handoff es estado curado, no verdad: lo que contradiga a git o al código se corrige en el handoff.
- Nunca commit ni push en `main`, `master`, `develop` ni `release/*`: rama propia y PR. Sin commit ni push sin
  confirmación explícita. Sin `git add -A` ni `git add .`.
- Antes de dar un cambio por terminado: skill `nexum-commons-checks`.
- Al cerrar la sesión: skill `nexum-commons-handoff`.
