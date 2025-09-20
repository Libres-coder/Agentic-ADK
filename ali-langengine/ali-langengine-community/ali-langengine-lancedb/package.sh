#!/bin/bash

echo "Packaging ali-langengine-lancedb..."

# 打包项目
mvn clean package -DskipTests

echo "Packaging completed."
