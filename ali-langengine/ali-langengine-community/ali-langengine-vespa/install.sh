#!/bin/bash

# Install script for ali-langengine-vespa
echo "Installing ali-langengine-vespa..."

mvn clean install -DskipTests

echo "Installation completed."
