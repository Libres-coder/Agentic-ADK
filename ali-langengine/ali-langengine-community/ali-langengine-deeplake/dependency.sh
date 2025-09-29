#!/bin/bash

# Deep Lake dependency script
echo "Getting dependencies for ali-langengine-deeplake..."

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    echo "Maven is not installed. Please install Maven first."
    exit 1
fi

# Get dependencies
mvn dependency:copy-dependencies -DoutputDirectory=target/dependency

echo "Dependencies downloaded to target/dependency/"
