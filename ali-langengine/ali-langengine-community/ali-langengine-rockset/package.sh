#!/bin/bash

# Build script for ali-langengine-rockset

echo "Building ali-langengine-rockset module..."

# Clean and compile
mvn clean compile

if [ $? -eq 0 ]; then
    echo "✅ Compilation successful"
else
    echo "❌ Compilation failed"
    exit 1
fi

# Run tests
echo "Running tests..."
mvn test

if [ $? -eq 0 ]; then
    echo "✅ All tests passed"
else
    echo "❌ Tests failed"
    exit 1
fi

# Package
echo "Packaging..."
mvn package -DskipTests

if [ $? -eq 0 ]; then
    echo "✅ Packaging successful"
    echo "Build completed successfully!"
else
    echo "❌ Packaging failed"
    exit 1
fi
