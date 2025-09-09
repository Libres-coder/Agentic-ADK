#!/bin/bash

# Dependency script for ali-langengine-vespa
echo "Checking dependencies for ali-langengine-vespa..."

# Check for required dependencies
mvn dependency:tree | grep -E "(vespa|httpclient5|jackson)"

echo "Dependencies check completed."
