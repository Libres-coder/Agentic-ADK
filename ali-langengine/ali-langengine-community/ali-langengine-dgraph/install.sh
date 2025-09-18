#!/bin/bash

# Dgraph 向量存储模块安装脚本
# Copyright (C) 2024 AIDC-AI

echo "Installing ali-langengine-dgraph module..."

# 清理之前的构建
mvn clean

# 安装依赖
./dependency.sh

# 编译测试
mvn compile test-compile

# 运行单元测试
echo "Running unit tests..."
mvn test

# 运行集成测试（如果启用）
if [ "$1" = "integration" ]; then
    echo "Running integration tests..."
    mvn test -Ddgraph.integration.test=true
fi

# 打包
mvn package -DskipTests

# 安装到本地仓库
mvn install -DskipTests

echo "Installation completed for ali-langengine-dgraph!"
echo "To run integration tests: ./install.sh integration"
