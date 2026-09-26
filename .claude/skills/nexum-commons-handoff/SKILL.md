---
name: nexum-commons-handoff
description: "Use when closing a work session on nexum-spring-commons and handing off to the next one — 'haz el handoff', 'cerremos la sesión', 'deja el traspaso'. Verifies git, remote, hooks and SonarQube state before writing, creates handoff/ if missing, and saves handoff/handoff_AAAA-MM-DD-HHMM.md with the project's fixed skeleton, including failed attempts and ordered next steps."
---

# Handoff de cierre de sesión — nexum-spring-commons

El handoff es lo que lee la sesión siguiente (skill `inicio-nexum-commons`) para retomar **sin releer la conversación
ni explorar el repositorio**. Es una bitácora personal: vive en `handoff/`, que está en `.gitignore` y no se sube.

## 1. Dónde y cómo se llama

```
handoff/handoff_AAAA-MM-DD-HHMM.md
```

- `AAAA-MM-DD-HHMM` es el momento en que se **escribe**, no el día que cubre. El formato ordena bien por nombre y por
  fecha.
- Si la carpeta no existe, crearla y comprobar que git la ignora:
  ```bash
  mkdir -p handoff
  git check-ignore -q handoff/x.md || echo "ATENCIÓN: handoff/ no está en .gitignore"
  ```
  Si no está ignorada, añadir `handoff/` a `.gitignore` antes de escribir.
- Si hay handoffs anteriores, leer el último y **seguir su formato**: es una serie, no un documento nuevo cada vez.
  ```bash
  ls -1t handoff/handoff_*.md 2>/dev/null | head -1
  ```
- Un handoff por cierre. Si en la misma sesión se cierra dos veces, se escribe uno nuevo; los anteriores no se editan
  salvo para corregir algo falso.

## 2. Verificar el estado antes de escribir

Un handoff con estado recordado en vez de comprobado es peor que ninguno. Ejecutar y volcar lo real:

```bash
git branch --show-current
git status --short
git log --oneline main..HEAD          # commits de la rama sobre main
git stash list
git remote -v                         # ¿hay remoto?
git config core.hooksPath             # debe ser .githooks
gh pr list --state open --json number,title,headRefName,reviewDecision   # solo si hay remoto
```

SonarQube, solo si se ejecutó en la sesión o hace falta el estado actual (token del entorno, nunca impreso):

```bash
curl -s -u "$SONAR_TOKEN:" "$SONAR_HOST_URL/api/qualitygates/project_status?projectKey=nexum-spring-commons" | jq -r '.projectStatus.status'
```

- Los resultados de tests (`./mvnw verify`) se anotan **solo si se ejecutaron en esta sesión**, con sus números. Si no,
  `NOT RUN` y el último resultado conocido con su fecha.
- **Declarar qué no se pudo verificar** y por qué. Nunca copiar el estado del handoff anterior como si siguiera vigente.

## 3. Esqueleto

```markdown
# Handoff — AAAA-MM-DD HH:MM

### Objetivo

Qué se está construyendo y para qué. Enlace al plan vigente si existe.

### Estado

Qué quedó hecho y commiteado, qué está a medias. Una frase por hilo de trabajo.

### Decisiones vigentes

- Decisiones que la sesión siguiente debe respetar (y por qué, si no es obvio).

### Relevante

- Archivos o clases que hay que conocer para continuar, con la razón.

### Validación

- `./mvnw verify`: PASS / FAIL / NOT RUN — números.
- SonarQube: PASS / FAIL / NOT RUN / BLOCKED — Quality Gate y métricas.
- graphify: PASS / NOT RUN.

### Bloqueos

- Lo que impide avanzar y qué hace falta para destrabarlo. Avisos no bloqueantes, marcados como tales.

### Failed attempts

- Qué falló en la sesión, la causa y la lección accionable.

### Pendiente

1. Numerado y ordenado por lo que desbloquea, con el comando o la ruta para arrancar.

### Git

- Rama y commits sobre `main`.
- Árbol de trabajo: limpio o qué queda sin commit.
- Remoto y PR abiertos (o "sin remoto").
```

## 4. Las secciones que más se usan

**Failed attempts.** Cada error con su causa y la lección, no la anécdota:

> La primera prueba de mutación no se detectó. El reinicio buscaba el proceso por una ruta que no coincidía con la de
> arranque y la app vieja siguió respondiendo. **Lección:** detener la app por su puerto (`lsof -tiTCP:<puerto>`) y
> comprobar el log de arranque antes de comparar.

**Decisiones vigentes.** Lo que no se debe rediscutir ni deshacer sin motivo nuevo (por ejemplo, que `ErrorCode` es
interfaz, o que el orden de las claves JSON no es contrato).

**Pendiente.** Primero lo que no depende de nadie. Si algo espera una decisión, decir de quién y cuál.

## 5. Reglas de redacción

- **Fechas absolutas.** Nunca "ayer", "hoy" ni "la semana pasada".
- **Evidencia en todo:** hash corto, nombre del test, `archivo:línea`, número de PR. Una afirmación sin ancla no se
  puede verificar en la sesión siguiente.
- **Distinguir lo verificado de lo asumido**, con esas palabras.
- **Decir qué está incompleto**, no solo lo entregado.
- Nunca secretos: tokens y contraseñas no se escriben, ni siquiera parciales.
- Sin voseo, sin "usted", sin "che". "Ejecutar", no "correr"; nunca "corrida".
- No repetir lo que ya está en `CLAUDE.md`, `INICIO.md`, el README o el CHANGELOG: enlazarlo. Si cambió una regla o
  un procedimiento, se actualiza **ese** documento, no solo el handoff.

## 6. Checklist de cierre

1. Verificar el estado (§2).
2. Escribir `handoff/handoff_AAAA-MM-DD-HHMM.md` (§3).
3. Si cambió una regla, un comando o el uso de la librería, actualizar `CLAUDE.md`, `INICIO.md`, el README o el
   CHANGELOG en un commit (con confirmación).
4. Si se cambió código: `graphify update .`.
5. Confirmar que `git status --short` no muestra nada de `handoff/` (debe estar ignorado).
