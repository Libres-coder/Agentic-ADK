#!/bin/bash

cd $(dirname $0)

mvn clean deploy -Dmaven.test.skip=true
