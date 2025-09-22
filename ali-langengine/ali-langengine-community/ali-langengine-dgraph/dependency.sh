#!/bin/bash

# Dgraph 向量存储模块依赖脚本
# Copyright (C) 2024 AIDC-AI

echo "Installing dependencies for ali-langengine-dgraph module..."

# 安装依赖
mvn dependency:resolve

# 输出依赖树
echo "Dependency tree:"
mvn dependency:tree

echo "Dependencies installation completed for ali-langengine-dgraph!"
