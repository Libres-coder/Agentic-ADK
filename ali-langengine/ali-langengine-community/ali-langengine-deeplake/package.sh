#!/bin/bash

# Deep Lake package script
echo "Packaging ali-langengine-deeplake..."

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    echo "Maven is not installed. Please install Maven first."
    exit 1
fi

# Package the project
mvn clean package -DskipTests

echo "ali-langengine-deeplake packaged successfully!"
echo "JAR file available in target/ directory"
