#!/usr/bin/env bash
#
# Migrate existing PostgreSQL data to PostgreSQL 18+ without data loss.
# Detects the source version from the data directory (PG_VERSION file).
# Run from the project root (where .env and docker-compose.yml are).
#
# Steps:
#   1. Dump all data from the existing data directory (using matching PG version).
#   2. Start a temporary PG 18 container with the new data directory (empty).
#   3. Restore the dump into PG 18.
#   4. Remove the temporary container; data_pg now contains your upgraded data.
#
# Usage:
#   ./docker/scripts/migrate-postgres-to-18.sh [--prod]
#   Default: dev paths (/home/informatics/dev/data -> data_pg)
#   --prod:  prod paths (/home/informatics/prod/data -> data_pg)
#
# Prerequisites:
#   - Existing PostgreSQL data in .../data (or .../prod/data)
#   - .env with DB_NAME, DB_USER, DB_PASSWORD (same as in docker-compose)
#   - Docker available
#

set -e

PROJECT_ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
cd "$PROJECT_ROOT"

# Load DB_* from .env (support "KEY: value" and KEY=value)
if [ -f .env ]; then
  while IFS= read -r line; do
    [[ "$line" =~ ^# ]] && continue
    key=""
    if [[ "$line" =~ ^(DB_NAME|DB_USER|DB_PASSWORD):[[:space:]]*(.*)$ ]]; then
      key="${BASH_REMATCH[1]}"
      export "$key=${BASH_REMATCH[2]}"
    elif [[ "$line" =~ ^(DB_NAME|DB_USER|DB_PASSWORD)=(.*)$ ]]; then
      key="${BASH_REMATCH[1]}"
      export "$key=${BASH_REMATCH[2]}"
    fi
  done < .env
fi

for var in DB_NAME DB_USER DB_PASSWORD; do
  if [ -z "${!var}" ]; then
    echo "Error: $var is not set. Set it in .env or the environment."
    exit 1
  fi
done

if [ "${1:-}" = "--prod" ]; then
  OLD_DATA="/home/informatics/prod/data"
  NEW_DATA="/home/informatics/prod/data_pg"
  MODE="prod"
else
  OLD_DATA="/home/informatics/dev/data"
  NEW_DATA="/home/informatics/dev/data_pg"
  MODE="dev"
fi

BACKUP_FILE="${PROJECT_ROOT}/migration-backup.sql"
CONTAINER_PG_SOURCE="pg_src_migrate_$$"
CONTAINER_PG18="pg18_migrate_$$"

cleanup() {
  docker rm -f "$CONTAINER_PG_SOURCE" 2>/dev/null || true
  docker rm -f "$CONTAINER_PG18" 2>/dev/null || true
}
trap cleanup EXIT

if [ ! -d "$OLD_DATA" ] || [ -z "$(ls -A "$OLD_DATA" 2>/dev/null)" ]; then
  echo "Error: No existing data found at $OLD_DATA. Nothing to migrate."
  exit 1
fi

# Detect source PostgreSQL major version (PG_VERSION contains e.g. "17")
PG_VERSION_FILE="$OLD_DATA/PG_VERSION"
if [ ! -f "$PG_VERSION_FILE" ]; then
  echo "Error: $PG_VERSION_FILE not found. Is $OLD_DATA a PostgreSQL data directory?"
  exit 1
fi
PG_SOURCE_VERSION="$(cat "$PG_VERSION_FILE" | tr -d '\r\n' | grep -E '^[0-9]+$' || true)"
if [ -z "$PG_SOURCE_VERSION" ]; then
  echo "Error: Could not read major version from $PG_VERSION_FILE"
  exit 1
fi

echo "=== PostgreSQL $PG_SOURCE_VERSION → 18 migration ($MODE) ==="
echo "  Old data: $OLD_DATA (PG $PG_SOURCE_VERSION)"
echo "  New data: $NEW_DATA"
echo "  Backup:   $BACKUP_FILE"
echo ""

# 1. Dump from source (same major version as existing data)
echo "Starting temporary PostgreSQL $PG_SOURCE_VERSION container and dumping..."
docker run -d --name "$CONTAINER_PG_SOURCE" \
  -v "$OLD_DATA:/var/lib/postgresql/data" \
  -e POSTGRES_DB="$DB_NAME" \
  -e POSTGRES_USER="$DB_USER" \
  -e POSTGRES_PASSWORD="$DB_PASSWORD" \
  "postgres:${PG_SOURCE_VERSION}"

# Wait for PostgreSQL to accept connections (use "postgres" DB - always exists with existing data)
for i in $(seq 1 60); do
  if ! docker inspect -f '{{.State.Running}}' "$CONTAINER_PG_SOURCE" 2>/dev/null | grep -q true; then
    echo "PostgreSQL $PG_SOURCE_VERSION container stopped unexpectedly. Logs:"
    docker logs "$CONTAINER_PG_SOURCE" 2>&1
    exit 1
  fi
  if docker exec "$CONTAINER_PG_SOURCE" pg_isready -U "$DB_USER" -d postgres 2>/dev/null; then
    break
  fi
  if [ "$i" -eq 60 ]; then
    echo "Timeout waiting for PostgreSQL $PG_SOURCE_VERSION. Logs:"
    docker logs "$CONTAINER_PG_SOURCE" 2>&1
    exit 1
  fi
  sleep 1
done

docker exec -e PGPASSWORD="$DB_PASSWORD" "$CONTAINER_PG_SOURCE" pg_dumpall -U "$DB_USER" > "$BACKUP_FILE"
docker rm -f "$CONTAINER_PG_SOURCE"

if [ ! -s "$BACKUP_FILE" ]; then
  echo "Error: Dump file is empty."
  exit 1
fi
echo "Dump saved to $BACKUP_FILE"

# 2. Prepare new data directory
mkdir -p "$NEW_DATA"
# PG image typically runs as uid 999
if command -v sudo >/dev/null 2>&1; then
  sudo chown -R 999:999 "$NEW_DATA" 2>/dev/null || true
fi

# 3. Start PG18 with empty data dir and restore
echo "Starting temporary PostgreSQL 18 container and restoring..."
docker run -d --name "$CONTAINER_PG18" \
  -v "$NEW_DATA:/var/lib/postgresql" \
  -e POSTGRES_DB="$DB_NAME" \
  -e POSTGRES_USER="$DB_USER" \
  -e POSTGRES_PASSWORD="$DB_PASSWORD" \
  postgres:latest

for i in $(seq 1 60); do
  if ! docker inspect -f '{{.State.Running}}' "$CONTAINER_PG18" 2>/dev/null | grep -q true; then
    echo "PostgreSQL 18 container stopped unexpectedly. Logs:"
    docker logs "$CONTAINER_PG18" 2>&1
    exit 1
  fi
  if docker exec "$CONTAINER_PG18" pg_isready -U "$DB_USER" -d postgres 2>/dev/null; then
    break
  fi
  if [ "$i" -eq 60 ]; then
    echo "Timeout waiting for PostgreSQL 18. Logs:"
    docker logs "$CONTAINER_PG18" 2>&1
    exit 1
  fi
  sleep 1
done

docker exec -i -e PGPASSWORD="$DB_PASSWORD" "$CONTAINER_PG18" \
  psql -U "$DB_USER" -d postgres -v ON_ERROR_STOP=0 < "$BACKUP_FILE" >/dev/null 2>&1 || true

docker rm -f "$CONTAINER_PG18"

echo "Migration complete. Data is in $NEW_DATA"
echo "You can start your stack with: docker compose --profile dev up -d"
echo "Optional: remove backup with: rm $BACKUP_FILE"
