#!/bin/bash

# Stack Overflow Search Module Package Script
# 打包脚本

echo "=== Packaging Stack Overflow Search Module ==="

# Clean and package
echo "Cleaning and packaging..."
mvn clean package

# Create source package
echo "Creating source package..."
mvn source:jar

# Create javadoc package
echo "Creating javadoc package..."
mvn javadoc:jar

echo ""
echo "=== Package Summary ==="
echo "Module: ali-langengine-stackoverflow"
echo "Target directory: target/"
echo "Artifacts created:"
echo "  - ali-langengine-stackoverflow-1.2.6-202508111516.jar"
echo "  - ali-langengine-stackoverflow-1.2.6-202508111516-sources.jar"
echo "  - ali-langengine-stackoverflow-1.2.6-202508111516-javadoc.jar"

echo ""
echo "Stack Overflow Search Module packaging completed!"
