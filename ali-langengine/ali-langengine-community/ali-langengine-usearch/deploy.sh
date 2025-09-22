#!/bin/bash

# USearch模块部署脚本

echo "Deploying ali-langengine-usearch module..."

# 部署到Maven仓库
mvn clean deploy -DskipTests

echo "ali-langengine-usearch module deployment completed."
