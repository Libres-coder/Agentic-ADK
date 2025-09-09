#!/bin/bash

# Stack Overflow Search Module Install Script
# 安装脚本

echo "=== Installing Stack Overflow Search Module ==="

# Install to local Maven repository
echo "Installing to local Maven repository..."
mvn clean install -DskipTests

echo ""
echo "=== Installation Summary ==="
echo "Module: ali-langengine-stackoverflow"
echo "Version: 1.2.6-202508111516"
echo "Status: Installed to local repository"

echo ""
echo "Stack Overflow Search Module installation completed!"
