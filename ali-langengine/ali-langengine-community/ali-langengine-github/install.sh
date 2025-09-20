#!/bin/bash

# Install script for ali-langengine-github module
# Usage: ./install.sh [options]
# Options:
#   -s, --skip-tests   Skip running tests during installation
#   -h, --help        Show this help message

set -e  # Exit immediately if a command exits with a non-zero status

SKIP_TESTS=false

# Parse command line arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        -s|--skip-tests)
            SKIP_TESTS=true
            shift
            ;;
        -h|--help)
            echo "Usage: $0 [options]"
            echo "Options:"
            echo "  -s, --skip-tests   Skip running tests during installation"
            echo "  -h, --help        Show this help message"
            exit 0
            ;;
        *)
            echo "Unknown option: $1"
            echo "Use -h or --help for usage information"
            exit 1
            ;;
    esac
done

echo "Installing ali-langengine-github module..."

# Check if pom.xml exists
if [ ! -f "pom.xml" ]; then
    echo "Error: pom.xml not found in current directory"
    exit 1
fi

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    echo "Error: Maven (mvn) is not installed or not in PATH"
    exit 1
fi

# Build Maven command
MVN_CMD="mvn clean install"
if [ "$SKIP_TESTS" = true ]; then
    MVN_CMD="$MVN_CMD -DskipTests"
    echo "Skipping tests..."
fi

# Install to local repository
echo "Running: $MVN_CMD"
$MVN_CMD

# Check installation result
if [ $? -eq 0 ]; then
    echo "✓ Installation successful"
    echo "✓ Module installed to local Maven repository"
else
    echo "Installation failed"
    exit 1
fi
