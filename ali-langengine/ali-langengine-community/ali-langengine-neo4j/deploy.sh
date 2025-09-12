#!/bin/bash

cd "$(dirname "$0")"

mvn clean deploy -DskipTests
