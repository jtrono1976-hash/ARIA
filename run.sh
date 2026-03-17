#!/usr/bin/env bash
# Run ARIA — builds first if jar is missing
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

if [ ! -f "target/aria-companion-1.0.0.jar" ]; then
    echo "Building ARIA..."
    mvn package -q -DskipTests
fi

echo "Starting ARIA..."
java -jar target/aria-companion-1.0.0.jar
