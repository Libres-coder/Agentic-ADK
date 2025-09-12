#!/bin/bash
set -e

# Zilliz Cloud deploy script
echo "Deploying Zilliz Cloud module..."

mvn clean deploy -DskipTests

echo "Zilliz Cloud module deployed successfully."