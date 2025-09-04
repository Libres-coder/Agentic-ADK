#!/bin/bash

# Slack LangEngine Module Dependency Script
# This script handles dependency management for the Slack integration module

echo "=== Slack LangEngine Module Dependencies ==="

# Install dependencies
echo "Installing dependencies for ali-langengine-slack..."

# Check if Maven is available
if ! command -v mvn &> /dev/null; then
    echo "Error: Maven is not installed or not in PATH"
    exit 1
fi

# Install dependencies
mvn dependency:resolve

echo "Dependencies installation completed!"

# Check dependency tree
echo ""
echo "=== Dependency Tree ==="
mvn dependency:tree

# Check for dependency conflicts
echo ""
echo "=== Dependency Analysis ==="
mvn dependency:analyze

# Check for security vulnerabilities
echo ""
echo "=== Security Check ==="
mvn org.owasp:dependency-check-maven:check || echo "Warning: Security check plugin not configured"

# Check for dependency updates
echo ""
echo "=== Dependency Updates ==="
mvn versions:display-dependency-updates

echo "Slack LangEngine Module dependencies setup completed!"
