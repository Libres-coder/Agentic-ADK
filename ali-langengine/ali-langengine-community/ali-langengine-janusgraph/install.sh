#!/bin/bash

cd $(dirname $0)

mvn clean install -Dmaven.test.skip=true
