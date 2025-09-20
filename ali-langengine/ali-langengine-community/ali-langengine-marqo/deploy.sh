#!/bin/bash

if [ ! -f deploy.sh ]; then
    echo "deploy.sh not found!"
    exit 1
fi

./deploy.sh
