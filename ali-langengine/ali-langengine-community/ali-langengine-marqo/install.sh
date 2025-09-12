#!/bin/bash

if [ ! -f install.sh ]; then
    echo "install.sh not found!"
    exit 1
fi

./install.sh
