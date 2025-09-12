#!/bin/bash

# Build script for ali-langengine-github module

echo "Building ali-langengine-github module..."

# Check if pom.xml exists
if [ ! -f "pom.xml" ]; then
    echo "Error: pom.xml not found in current directory"
    exit 1
fi

# Clean and compile
mvn clean compile

# Check compilation result
if [ $? -eq 0 ]; then
    echo "Compilation successful"
else
    echo "Compilation failed"
    exit 1
fi

# Run tests
echo "Running tests..."
mvn test

# Check test result
if [ $? -eq 0 ]; then
    echo "Tests passed"
else
    echo "Tests failed"
    exit 1
fi

# Package
echo "Packaging..."
mvn package -DskipTests

# Check packaging result
if [ $? -eq 0 ]; then
    echo "Packaging successful"
    echo "Build completed successfully"
else
    echo "Packaging failed"
    exit 1
fi
