#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
JAR_PATH="$ROOT/app/src/main/assets/jar/spider.jar"
EXPECTED_MD5="cc0ca03ed255ca4c1c01f7c682adf883"

if [[ ! -f "$JAR_PATH" ]]; then
  echo "Missing spider jar: $JAR_PATH" >&2
  echo "Place spider.jar in app/src/main/assets/jar/ before building." >&2
  exit 1
fi

actual_md5="$(md5 -q "$JAR_PATH" 2>/dev/null || md5sum "$JAR_PATH" | awk '{print $1}')"
if [[ "$actual_md5" != "$EXPECTED_MD5" ]]; then
  echo "MD5 mismatch: expected $EXPECTED_MD5, got $actual_md5" >&2
  exit 1
fi

echo "Spider jar ready: $JAR_PATH"
