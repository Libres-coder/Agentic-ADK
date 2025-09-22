#!/bin/bash

# Deep Lake install script
echo "Installing ali-langengine-deeplake..."

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    echo "Maven is not installed. Please install Maven first."
    exit 1
fi

# Install to local repository
mvn clean install -DskipTests

echo "ali-langengine-deeplake installed successfully!"
