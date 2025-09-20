#!/bin/bash
set -e

# Zilliz Cloud install script
echo "Installing Zilliz Cloud module..."

mvn clean install -DskipTests

echo "Zilliz Cloud module installed successfully."