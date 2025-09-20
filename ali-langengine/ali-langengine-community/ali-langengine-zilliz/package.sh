#!/bin/bash
set -e

# Zilliz Cloud package script
echo "Packaging Zilliz Cloud module..."

mvn clean package -DskipTests

echo "Zilliz Cloud module packaged successfully."