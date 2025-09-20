#!/bin/bash

echo "Installing ali-langengine-lancedb..."

# 安装到本地仓库
mvn clean install -DskipTests

echo "Installation completed."
