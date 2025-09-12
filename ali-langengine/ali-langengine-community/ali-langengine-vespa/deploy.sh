#!/bin/bash

# Deploy script for ali-langengine-vespa
echo "Deploying ali-langengine-vespa..."

mvn deploy -DskipTests

echo "Deployment completed."
