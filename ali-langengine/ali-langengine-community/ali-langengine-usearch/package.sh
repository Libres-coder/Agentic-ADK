#!/bin/bash

# USearch模块打包脚本

echo "Packaging ali-langengine-usearch module..."

# 清理和打包
mvn clean package -DskipTests

echo "ali-langengine-usearch module packaging completed."
