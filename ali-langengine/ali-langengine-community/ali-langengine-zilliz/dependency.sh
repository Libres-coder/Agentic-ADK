#!/bin/bash
set -e

# Zilliz Cloud dependency script
echo "Installing Zilliz Cloud dependencies..."

mvn dependency:resolve
mvn dependency:tree

echo "Zilliz Cloud dependencies installed successfully."