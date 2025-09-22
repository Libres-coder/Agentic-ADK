#!/bin/bash

# Deploy script for ali-langengine-rockset

echo "Deploying ali-langengine-rockset module..."

# Deploy to remote repository
mvn clean deploy -DskipTests

if [ $? -eq 0 ]; then
    echo "✅ Deployment successful"
else
    echo "❌ Deployment failed"
    exit 1
fi
