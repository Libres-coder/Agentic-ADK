#!/bin/bash
set -e # Any subsequent failing command will exit the script immediately

echo "Packaging ali-langengine-slack..."
mvn clean package -Dmaven.test.skip
echo "Packaging completed successfully."
