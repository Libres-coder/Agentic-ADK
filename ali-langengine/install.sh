#!/bin/bash
set -e

cd "$(dirname "$0")"

mvn clean install -DskipTests