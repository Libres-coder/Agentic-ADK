#!/bin/bash

# Dgraph 向量存储模块部署脚本
# Copyright (C) 2024 AIDC-AI

echo "Deploying ali-langengine-dgraph module..."

# 清理之前的构建
mvn clean

# 编译和打包
mvn compile package -DskipTests

# 安装到本地仓库
mvn install -DskipTests

# 部署到远程仓库（如果配置了）
if [ "$1" = "remote" ]; then
    echo "Deploying to remote repository..."
    mvn deploy -DskipTests
else
    echo "Local deployment completed. Use 'deploy.sh remote' to deploy to remote repository."
fi

echo "Deployment completed for ali-langengine-dgraph!"
