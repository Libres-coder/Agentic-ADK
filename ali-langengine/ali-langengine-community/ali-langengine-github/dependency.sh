#!/bin/bash

# Dependency check script for ali-langengine-github module

echo "Checking dependencies for ali-langengine-github module..."

# Check if pom.xml exists
if [ ! -f "pom.xml" ]; then
    echo "Error: pom.xml not found in current directory"
    exit 1
fi

# Check dependency tree
echo "Dependency tree:"
mvn dependency:tree

echo ""
echo "Analyzing dependencies..."
mvn dependency:analyze

echo ""
echo "Checking for dependency updates..."
mvn versions:display-dependency-updates

echo ""
echo "Checking for plugin updates..."
mvn versions:display-plugin-updates

echo "Dependency check completed"
