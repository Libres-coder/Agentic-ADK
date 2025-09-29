#!/bin/bash

# Deep Lake deploy script
echo "Deploying ali-langengine-deeplake..."

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    echo "Maven is not installed. Please install Maven first."
    exit 1
fi

# Deploy to repository
mvn clean deploy -DskipTests

echo "ali-langengine-deeplake deployed successfully!"
