#!/bin/bash

# USearch模块安装脚本

echo "Installing ali-langengine-usearch module..."

# 编译和安装
mvn clean compile install -DskipTests

echo "ali-langengine-usearch module installation completed."
