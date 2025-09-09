#!/bin/bash

if [ ! -f package.sh ]; then
    echo "package.sh not found!"
    exit 1
fi

./package.sh
