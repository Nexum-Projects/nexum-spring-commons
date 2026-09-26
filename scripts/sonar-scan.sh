#!/usr/bin/env bash
# Tests unitarios + integración + JaCoCo, análisis SonarQube y Quality Gate.
# Requiere en el entorno: SONAR_HOST_URL y SONAR_TOKEN (el token nunca se guarda en el repo).
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

: "${SONAR_HOST_URL:?SONAR_HOST_URL es obligatoria (ej. http://localhost:9000)}"
: "${SONAR_TOKEN:?SONAR_TOKEN es obligatoria}"

PROJECT_KEY="nexum-spring-commons"
PROJECT_NAME="nexum-spring-commons"

echo "==> Verificando acceso a SonarQube..."
HTTP_STATUS=$(curl -s -o /dev/null -w "%{http_code}" -u "$SONAR_TOKEN:" \
  "$SONAR_HOST_URL/api/projects/search?projects=$PROJECT_KEY")
if [ "$HTTP_STATUS" != "200" ]; then
  echo "ERROR: no se pudo consultar SonarQube en $SONAR_HOST_URL (HTTP $HTTP_STATUS)." >&2
  exit 1
fi

if ! curl -s -u "$SONAR_TOKEN:" "$SONAR_HOST_URL/api/projects/search?projects=$PROJECT_KEY" \
  | grep -q "\"key\":\"$PROJECT_KEY\""; then
  echo "==> Creando el proyecto '$PROJECT_KEY' en SonarQube..."
  curl -s -f -X POST -u "$SONAR_TOKEN:" "$SONAR_HOST_URL/api/projects/create" \
    --data-urlencode "project=$PROJECT_KEY" \
    --data-urlencode "name=$PROJECT_NAME" \
    --data-urlencode "mainBranch=main" \
    --data-urlencode "visibility=private" > /dev/null
fi

echo "==> Tests (unitarios + *IT) y cobertura JaCoCo..."
./mvnw -q clean verify

echo "==> Análisis SonarQube..."
./mvnw -q org.sonarsource.scanner.maven:sonar-maven-plugin:5.7.0.6970:sonar \
  -Dsonar.host.url="$SONAR_HOST_URL" \
  -Dsonar.token="$SONAR_TOKEN"

echo "==> Quality Gate..."
curl -s -u "$SONAR_TOKEN:" \
  "$SONAR_HOST_URL/api/qualitygates/project_status?projectKey=$PROJECT_KEY" \
  | python3 -m json.tool

echo "==> Resultado: $SONAR_HOST_URL/dashboard?id=$PROJECT_KEY"
