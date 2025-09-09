#!/bin/bash

# Stack Overflow Search Module Deploy Script
# 部署脚本

echo "=== Deploying Stack Overflow Search Module ==="

# Check if deployment configuration is available
if [ -z "$MAVEN_DEPLOY_URL" ]; then
    echo "Warning: MAVEN_DEPLOY_URL not set, deploying to local repository only"
    mvn clean deploy -DaltDeploymentRepository=local::default::file:./target/staging-deploy
else
    echo "Deploying to repository: $MAVEN_DEPLOY_URL"
    mvn clean deploy
fi

echo ""
echo "=== Deployment Summary ==="
echo "Module: ali-langengine-stackoverflow"
echo "Version: 1.2.6-202508111516"
echo "Status: Deployed"

echo ""
echo "Stack Overflow Search Module deployment completed!"
