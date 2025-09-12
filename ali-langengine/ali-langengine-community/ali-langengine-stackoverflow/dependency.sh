#!/bin/bash

# Stack Overflow Search Module Dependency Script
# 依赖管理脚本

echo "=== Stack Overflow Search Module Dependencies ==="

# Install dependencies
echo "Installing dependencies for ali-langengine-stackoverflow..."

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

# Check for security vulnerabilities
echo ""
echo "=== Security Check ==="
mvn org.owasp:dependency-check-maven:check || echo "Warning: Security check plugin not configured"

echo "Stack Overflow Search Module dependencies setup completed!"
