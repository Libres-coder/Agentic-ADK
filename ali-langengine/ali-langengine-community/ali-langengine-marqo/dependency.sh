#!/bin/bash

if [ ! -f dependency.sh ]; then
    echo "dependency.sh not found!"
    exit 1
fi

./dependency.sh
