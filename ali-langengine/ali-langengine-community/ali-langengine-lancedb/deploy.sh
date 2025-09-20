#!/bin/bash

echo "Deploying ali-langengine-lancedb..."

# 部署到远程仓库
mvn clean deploy -DskipTests

echo "Deployment completed."
