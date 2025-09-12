#!/bin/bash

# Deploy script for ali-langengine-github module

echo "Deploying ali-langengine-github module..."

# Check if pom.xml exists
if [ ! -f "pom.xml" ]; then
    echo "Error: pom.xml not found in current directory"
    exit 1
fi

# Deploy to remote repository
mvn clean deploy

# Check deployment result
if [ $? -eq 0 ]; then
    echo "Deployment successful"
else
    echo "Deployment failed"
    exit 1
fi
