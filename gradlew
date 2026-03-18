#!/bin/sh
#
# Gradle wrapper. To generate the wrapper JAR first run: gradle wrapper
# Then run: ./gradlew build
#
set -e
ROOT="$(cd "$(dirname "$0")" && pwd)"
WRAPPER_JAR="$ROOT/gradle/wrapper/gradle-wrapper.jar"
if [ -f "$WRAPPER_JAR" ]; then
  exec java -jar "$WRAPPER_JAR" "$@"
fi
if command -v gradle >/dev/null 2>&1; then
  exec gradle "$@"
fi
echo "Gradle wrapper JAR not found. Install Gradle (e.g. brew install gradle) then run: gradle wrapper"
exit 1
