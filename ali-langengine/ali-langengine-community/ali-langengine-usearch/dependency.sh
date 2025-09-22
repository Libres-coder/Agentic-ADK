#!/bin/bash

# USearch模块依赖安装脚本

echo "Installing dependencies for ali-langengine-usearch..."

# 安装Maven依赖
mvn clean install -DskipTests

echo "Dependencies installation completed."
