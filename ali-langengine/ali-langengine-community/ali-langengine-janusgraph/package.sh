#!/bin/bash

cd $(dirname $0)

mvn clean package -Dmaven.test.skip=true
