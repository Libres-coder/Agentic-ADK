#!/bin/bash

# Dgraph 向量存储模块打包脚本
# Copyright (C) 2024 AIDC-AI

echo "Packaging ali-langengine-dgraph module..."

# 清理之前的构建
mvn clean

# 编译
mvn compile

# 运行测试
echo "Running tests..."
mvn test

# 打包
echo "Creating packages..."
mvn package

# 创建源码包
echo "Creating source package..."
mvn source:jar

# 创建 Javadoc 包
echo "Creating Javadoc package..."
mvn javadoc:jar

# 显示生成的包
echo "Generated packages:"
ls -la target/*.jar

echo "Packaging completed for ali-langengine-dgraph!"
echo "Packages are available in the target/ directory"
