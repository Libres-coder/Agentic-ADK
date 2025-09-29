#!/bin/bash

# Install script for ali-langengine-rockset

echo "Installing ali-langengine-rockset module..."

# Install to local repository
mvn clean install -DskipTests

if [ $? -eq 0 ]; then
    echo "✅ Installation successful"
    echo "Module installed to local Maven repository"
else
    echo "❌ Installation failed"
    exit 1
fi
