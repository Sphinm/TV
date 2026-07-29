#!/usr/bin/env bash
set -euo pipefail

# Optional: download Vosk model into APK assets for offline local builds.
# Production builds fetch the model from CDN on first use (see Constant.REMOTE_VOSK_MODEL).

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
MODEL_DIR="$ROOT/app/src/main/assets/model-cn"
MODEL_URL="https://alphacephei.com/vosk/models/vosk-model-small-cn-0.22.zip"
TMP_ZIP="$(mktemp /tmp/vosk-model-cn.XXXXXX.zip)"

cleanup() {
  rm -f "$TMP_ZIP"
}
trap cleanup EXIT

if [[ -f "$MODEL_DIR/conf/model.conf" ]]; then
  echo "Vosk Chinese model already present at $MODEL_DIR"
  exit 0
fi

echo "Downloading Vosk Chinese model..."
curl -L --fail --progress-bar "$MODEL_URL" -o "$TMP_ZIP"

echo "Unpacking model..."
rm -rf "$MODEL_DIR"
mkdir -p "$MODEL_DIR"
unzip -q "$TMP_ZIP" -d "$(dirname "$MODEL_DIR")"
mv "$(dirname "$MODEL_DIR")/vosk-model-small-cn-0.22" "$MODEL_DIR"

echo "Model ready: $MODEL_DIR"
