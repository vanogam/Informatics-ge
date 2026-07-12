#!/usr/bin/env bash
# Deploy Informatics Docker images to GHCR and redeploy on a remote host via SSH.
# Configuration: deploy/.bash-env (see deploy/.bash-env.example)
#
# Remote services are managed by systemd (docker run units), not docker compose.
#
# FULL_DEPLOY=1: zips FULL_DEPLOY_LOCAL_ROOT on this machine (minus logs), SCPs to host, rsync-merge into REMOTE_APP_DIR.
# Passing -F or --full-deploy forces full deploy (overrides FULL_DEPLOY in .bash-env).
#
# Usage:
#   ./deploy/deploy.sh [--full-deploy|-F]
#   BASH_ENV_FILE=/path/to/.bash-env ./deploy/deploy.sh -F

set -euo pipefail

DEPLOY_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${DEPLOY_ROOT}/.." && pwd)"

FULL_DEPLOY_CLI=""
usage() {
  cat >&2 <<USAGE
Deploy Informatics to GHCR and remote host via SSH.

Usage:
  $(basename "${BASH_SOURCE[0]}") [-F|--full-deploy] [--help|-h]

  -F, --full-deploy    Enable full deploy (zip FULL_DEPLOY_LOCAL_ROOT to REMOTE_APP_DIR).
  -h, --help            Show this message.

Config: deploy/.bash-env (or \$BASH_ENV_FILE). FULL_DEPLOY=1 in env also enables full deploy without -F.

Examples:
  $(basename "${BASH_SOURCE[0]}")
  $(basename "${BASH_SOURCE[0]}") --full-deploy
USAGE
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    -F | --full-deploy)
      FULL_DEPLOY_CLI=1
      shift
      ;;
    -h | --help)
      usage
      exit 0
      ;;
    *)
      echo "Unknown option: $1" >&2
      usage
      exit 1
      ;;
  esac
done

load_env_file() {
  local f="$1"
  if [[ ! -f "$f" ]]; then
    echo "ERROR: Env file not found: $f" >&2
    exit 1
  fi
  # shellcheck disable=SC1090
  source "$f"
}

if [[ -n "${BASH_ENV_FILE:-}" ]]; then
  load_env_file "$BASH_ENV_FILE"
elif [[ -f "${DEPLOY_ROOT}/.bash-env" ]]; then
  load_env_file "${DEPLOY_ROOT}/.bash-env"
elif [[ -f "${REPO_ROOT}/.bash-env" ]]; then
  load_env_file "${REPO_ROOT}/.bash-env"
else
  echo "ERROR: Set BASH_ENV_FILE or create deploy/.bash-env (copy from deploy/.bash-env.example)" >&2
  exit 1
fi

SSH_PORT="${SSH_PORT:-22}"
REMOTE_APP_DIR="${REMOTE_APP_DIR:-/home/informatics/app}"
REMOTE_CONFIG_DIR="${REMOTE_CONFIG_DIR:-${REMOTE_APP_DIR}/config}"
# Space-separated systemd service names to restart after image pull.
REMOTE_SERVICES="${REMOTE_SERVICES:-informatics-core informatics-ui}"
LOCAL_ENV_FILE="${LOCAL_ENV_FILE:-}"
# Auto-detect repo-root .env if LOCAL_ENV_FILE is not explicitly set.
if [[ -z "$LOCAL_ENV_FILE" ]] && [[ -f "${REPO_ROOT}/.env" ]]; then
  LOCAL_ENV_FILE="${REPO_ROOT}/.env"
fi
REMOTE_BACKUP_DIR="${REMOTE_BACKUP_DIR:-}"
FULL_DEPLOY_LOCAL_ROOT="${FULL_DEPLOY_LOCAL_ROOT:-}"
FULL_DEPLOY="${FULL_DEPLOY:-0}"
if [[ "${FULL_DEPLOY_CLI}" == "1" ]]; then
  FULL_DEPLOY=1
fi
BUILD_IMAGES="${BUILD_IMAGES:-1}"
SKIP_UI_BUILD="${SKIP_UI_BUILD:-0}"
SKIP_WORKER_BUILD="${SKIP_WORKER_BUILD:-0}"
SKIP_PUSH="${SKIP_PUSH:-0}"

require() {
  local n
  for n in "$@"; do
    if [[ -z "${!n:-}" ]]; then
      echo "ERROR: Required variable $n is not set in .bash-env" >&2
      exit 1
    fi
  done
}

require GHCR_IMAGE_PREFIX VERSION SSH_HOST SSH_USER SSH_KEY REMOTE_SERVICES

if [[ -n "${GHCR_TOKEN:-}" ]]; then
  require GHCR_USER
fi

if [[ ! -f "$SSH_KEY" ]]; then
  echo "ERROR: SSH private key not found: $SSH_KEY" >&2
  exit 1
fi

ssh_base=(
  ssh -i "$SSH_KEY" -p "$SSH_PORT" -o BatchMode=yes -o StrictHostKeyChecking=accept-new "${SSH_USER}@${SSH_HOST}"
)
scp_base=(
  scp -i "$SSH_KEY" -P "$SSH_PORT" -o BatchMode=yes -o StrictHostKeyChecking=accept-new
)

core_jar="${REPO_ROOT}/informatics-system/target-informatics/Informatics-exec.jar"
preflight_build() {
  if [[ ! -f "$core_jar" ]]; then
    echo "ERROR: Core JAR missing. Build with Maven first, e.g.: (cd \"${REPO_ROOT}\" && mvn -pl informatics-system -am package -DskipTests)" >&2
    exit 1
  fi
  if [[ "$SKIP_WORKER_BUILD" != "1" ]]; then
    shopt -s nullglob
    local -a w=( "${REPO_ROOT}/informatics-worker/target/informatics-worker-"*.jar )
    shopt -u nullglob
    if [[ ${#w[@]} -eq 0 ]]; then
      echo "ERROR: Worker JAR missing under informatics-worker/target/. Build with Maven or set SKIP_WORKER_BUILD=1" >&2
      exit 1
    fi
  fi
}

docker_login_local() {
  if [[ -z "${GHCR_TOKEN:-}" ]]; then
    echo "Skipping local ghcr.io login (GHCR_TOKEN unset; using existing Docker credentials)." >&2
    return 0
  fi
  printf '%s' "$GHCR_TOKEN" | docker login ghcr.io -u "$GHCR_USER" --password-stdin
}

tag_and_push() {
  local base="$1"
  local name="$2"
  local src="informatics/${name}:${VERSION}"
  local dst="${base}/${name}:${VERSION}"
  docker tag "$src" "$dst"
  if [[ "$SKIP_PUSH" != "1" ]]; then
    docker push "$dst"
  fi
}

build_images() {
  preflight_build
  cd "$REPO_ROOT"
  docker build -t "informatics/core:${VERSION}" -f docker/main/Dockerfile .
  if [[ "$SKIP_UI_BUILD" != "1" ]]; then
    docker build -t "informatics/ui:${VERSION}" -f informatics-ui/Dockerfile ./informatics-ui
  fi
  if [[ "$SKIP_WORKER_BUILD" != "1" ]]; then
    docker build -t "informatics/worker:${VERSION}" -f informatics-worker/Dockerfile ./informatics-worker
  fi
}

push_ghcr() {
  tag_and_push "$GHCR_IMAGE_PREFIX" core
  if [[ "$SKIP_UI_BUILD" != "1" ]]; then
    tag_and_push "$GHCR_IMAGE_PREFIX" ui
  fi
  if [[ "$SKIP_WORKER_BUILD" != "1" ]]; then
    tag_and_push "$GHCR_IMAGE_PREFIX" worker
  fi
}

remote_docker_login() {
  if [[ -z "${GHCR_TOKEN:-}" ]]; then
    echo "Skipping remote ghcr.io login (GHCR_TOKEN unset; host must already be logged in to ghcr.io)." >&2
    return 0
  fi
  printf '%s\n' "$GHCR_TOKEN" | "${ssh_base[@]}" docker login ghcr.io -u "$GHCR_USER" --password-stdin
}

remote_pull() {
  local gh="$GHCR_IMAGE_PREFIX"
  local v="$VERSION"
  "${ssh_base[@]}" docker pull "${gh}/core:${v}"
  if [[ "$SKIP_UI_BUILD" != "1" ]]; then
    "${ssh_base[@]}" docker pull "${gh}/ui:${v}"
  fi
  if [[ "$SKIP_WORKER_BUILD" != "1" ]]; then
    "${ssh_base[@]}" docker pull "${gh}/worker:${v}"
  fi
}

local_snapshot_upload_unpack() {
  if [[ "$FULL_DEPLOY" != "1" ]]; then
    return 0
  fi
  require FULL_DEPLOY_LOCAL_ROOT
  local src="$FULL_DEPLOY_LOCAL_ROOT"
  if [[ ! -d "$src" ]]; then
    echo "ERROR: FULL_DEPLOY_LOCAL_ROOT is not a directory: $src" >&2
    exit 1
  fi
  local parent base zip_local remote_zip
  parent="$(cd "$(dirname "$src")" && pwd)"
  # Zip root matches last path segment (e.g. .../prod → archive contains prod/…).
  base="$(basename "$src")"
  echo "=== Full deploy: zip local '$src' (excluding logs), upload, unpack to ${REMOTE_APP_DIR} ==="
  zip_local="${TMPDIR:-/tmp}/informatics-prod-local-$$.zip"
  rm -f "$zip_local"
  (
    cd "$parent"
    zip -rq "$zip_local" "$base" \
      -x "${base}/logs/*" \
      -x "${base}/logs/**" \
      -x "${base}/*/logs/*" \
      -x "${base}/*/logs/**" \
      -x "*.log" \
      -x "${base}/*.log" \
      -x "${base}/**/*.log"
  )
  remote_zip="/tmp/informatics-prod-upload-$(date +%Y%m%d%H%M%S)-$$.zip"
  "${scp_base[@]}" "$zip_local" "${SSH_USER}@${SSH_HOST}:${remote_zip}"

  local backup_dir="${REMOTE_BACKUP_DIR:-$(dirname "$REMOTE_APP_DIR")/backups}"
  "${ssh_base[@]}" bash -s "$remote_zip" "$REMOTE_APP_DIR" "$base" "$backup_dir" <<'REMOTE_UNPACK_SCRIPT'
set -euo pipefail
ZIP_REMOTE="$1"
APP_DIR="$2"
ROOT_INSIDE="$3"
BACKUP_BASE="$4"
mkdir -p "$APP_DIR"
EX="/tmp/informatics-prod-extract-$$"
rm -rf "$EX"
mkdir -p "$EX"
cleanup() { rm -rf "$ZIP_REMOTE" "$EX"; }
trap cleanup EXIT
# Backup existing app dir before overwriting.
if [[ -d "$APP_DIR" ]]; then
  BACKUP_DIR="${BACKUP_BASE}/$(date +%Y%m%d%H%M%S)"
  mkdir -p "$BACKUP_DIR"
  rsync -a \
    --exclude='logs/' \
    --exclude='*/logs/' \
    --exclude='*.log' \
    "$APP_DIR/" "$BACKUP_DIR/"
  echo "Backup saved: $BACKUP_DIR"
fi
unzip -oq "$ZIP_REMOTE" -d "$EX"
if [[ -d "$EX/$ROOT_INSIDE" ]]; then
  rsync -a --delete "$EX/$ROOT_INSIDE/" "$APP_DIR/"
else
  echo "ERROR: expected directory $ROOT_INSIDE inside archive, under $EX" >&2
  exit 1
fi
echo "Remote app dir updated: $APP_DIR"
REMOTE_UNPACK_SCRIPT

  rm -f "$zip_local"
}

remote_upload_env() {
  if [[ -z "${LOCAL_ENV_FILE:-}" ]]; then
    return 0
  fi
  if [[ ! -f "$LOCAL_ENV_FILE" ]]; then
    echo "ERROR: LOCAL_ENV_FILE not found: $LOCAL_ENV_FILE" >&2
    exit 1
  fi
  echo "=== Uploading .env to ${SSH_USER}@${SSH_HOST}:${REMOTE_CONFIG_DIR}/.env ==="
  "${ssh_base[@]}" mkdir -p "$REMOTE_CONFIG_DIR"
  "${scp_base[@]}" "$LOCAL_ENV_FILE" "${SSH_USER}@${SSH_HOST}:${REMOTE_CONFIG_DIR}/.env"
}

remote_systemd_restart() {
  # shellcheck disable=SC2086
  "${ssh_base[@]}" bash -s $REMOTE_SERVICES <<'REMOTE_RESTART_SCRIPT'
set -euo pipefail
services=("$@")
for svc in "${services[@]}"; do
  echo "Stopping $svc..."
  systemctl stop "$svc" || true
  container="${svc#informatics-}"
  docker rm -f "$container" 2>/dev/null || true
done
for svc in "${services[@]}"; do
  echo "Starting $svc..."
  systemctl start "$svc"
done
REMOTE_RESTART_SCRIPT
}

main() {
  echo "=== Informatics deploy: version ${VERSION} ==="
  docker_login_local
  if [[ "$BUILD_IMAGES" == "1" ]]; then
    build_images
  fi
  push_ghcr

  echo "=== Remote: ${SSH_USER}@${SSH_HOST} ==="
  remote_docker_login
  remote_pull
  local_snapshot_upload_unpack
  # remote_upload_env
  remote_systemd_restart
  echo "=== Done ==="
}

main