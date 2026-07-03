#!/usr/bin/env bash
set -euo pipefail

APP_DIR="${APP_DIR:-/opt/onboarded/onboarding-api}"
SERVICE_NAME="${SERVICE_NAME:-onboarding-api}"
BUILD_NUMBER="${BUILD_NUMBER:-manual}"
APP_USER="${APP_USER:-onboarding}"
APP_GROUP="${APP_GROUP:-onboarding}"
JAR_NAME="${JAR_NAME:-onboarding-api.jar}"
HEALTH_URL="${HEALTH_URL:-http://127.0.0.1:8080/api/health}"

INCOMING_DIR="$APP_DIR/incoming"
RELEASES_DIR="$APP_DIR/releases"
ENV_FILE="$APP_DIR/onboarding-api.env"
SERVICE_SOURCE="$INCOMING_DIR/onboarding-api.service"
JAR_SOURCE="$INCOMING_DIR/$JAR_NAME"
RELEASE_JAR="$RELEASES_DIR/$JAR_NAME.$BUILD_NUMBER"

if [[ ! -f "$JAR_SOURCE" ]]; then
  echo "Missing uploaded jar: $JAR_SOURCE" >&2
  exit 2
fi

if [[ ! -f "$SERVICE_SOURCE" ]]; then
  echo "Missing uploaded systemd service: $SERVICE_SOURCE" >&2
  exit 2
fi

if [[ ! -f "$ENV_FILE" ]]; then
  mkdir -p "$APP_DIR"
  cp "$INCOMING_DIR/onboarding-api.env.example" "$ENV_FILE"
  chmod 600 "$ENV_FILE"
  echo "Created $ENV_FILE from example. Fill real secrets and rerun deployment." >&2
  exit 3
fi

if ! getent group "$APP_GROUP" >/dev/null 2>&1; then
  groupadd --system "$APP_GROUP"
fi

if ! id -u "$APP_USER" >/dev/null 2>&1; then
  useradd --system --gid "$APP_GROUP" --home-dir "$APP_DIR" --shell /sbin/nologin "$APP_USER"
fi

mkdir -p "$RELEASES_DIR" "$APP_DIR/logs"
install -m 0644 "$JAR_SOURCE" "$RELEASE_JAR"
ln -sfn "$RELEASE_JAR" "$APP_DIR/$JAR_NAME"
chown -R "$APP_USER:$APP_GROUP" "$APP_DIR"

install -m 0644 "$SERVICE_SOURCE" "/etc/systemd/system/$SERVICE_NAME.service"
systemctl daemon-reload
systemctl enable "$SERVICE_NAME"
systemctl restart "$SERVICE_NAME"

sleep 5
systemctl --no-pager --full status "$SERVICE_NAME"

for _ in 1 2 3 4 5 6; do
  if curl -fsS "$HEALTH_URL"; then
    echo
    echo "Deployment health check passed: $HEALTH_URL"
    exit 0
  fi
  sleep 5
done

echo "Deployment health check failed: $HEALTH_URL" >&2
journalctl -u "$SERVICE_NAME" -n 120 --no-pager >&2 || true
exit 4
