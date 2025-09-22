#!/bin/bash

# Dependency analysis script for ali-langengine-rockset

echo "Analyzing dependencies for ali-langengine-rockset module..."

# Check dependency tree
mvn dependency:tree

echo ""
echo "Analyzing for potential conflicts..."
mvn dependency:analyze

echo ""
echo "Checking for updates..."
mvn versions:display-dependency-updates
