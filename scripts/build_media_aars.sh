#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
MEDIA_DIR="$ROOT/third_party/media"
BRANCH="release-1.10.1-fongmi"

if [ ! -f "$MEDIA_DIR/gradlew" ]; then
  mkdir -p "$ROOT/third_party"
  curl -fL "https://github.com/FongMi/media/archive/refs/heads/${BRANCH}.tar.gz" -o "$ROOT/third_party/media.tar.gz"
  tar -xzf "$ROOT/third_party/media.tar.gz" -C "$ROOT/third_party"
  mv "$ROOT/third_party/media-${BRANCH}" "$MEDIA_DIR"
  rm "$ROOT/third_party/media.tar.gz"
fi

MODULES=(
  lib-common lib-container lib-database lib-datasource lib-datasource-okhttp
  lib-datasource-rtmp lib-decoder lib-exoplayer lib-exoplayer-dash
  lib-exoplayer-hls lib-exoplayer-rtsp lib-exoplayer-smoothstreaming
  lib-extractor lib-session lib-ui lib-ui-danmaku
)

TASKS=()
for module in "${MODULES[@]}"; do
  TASKS+=(":${module}:assembleRelease")
done

cd "$MEDIA_DIR"
chmod +x gradlew
./gradlew "${TASKS[@]}"

mkdir -p "$ROOT/app/libs"
find "$MEDIA_DIR" -path '*/outputs/aar/lib-*-release.aar' -exec cp {} "$ROOT/app/libs/" \;
echo "Media3 AARs copied to app/libs"
