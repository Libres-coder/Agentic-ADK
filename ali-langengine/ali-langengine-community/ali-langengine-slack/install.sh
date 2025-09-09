#!/bin/bash
set -e # Any subsequent failing command will exit the script immediately

echo "Installing ali-langengine-slack..."
mvn clean install -DskipTests
echo "Installation completed successfully."
