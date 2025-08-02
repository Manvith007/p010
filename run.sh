#!/bin/bash

# Personal Digital Organizer & Secure Vault - Run Script

# Check if JAR exists
if [ ! -f "build/vault.jar" ]; then
    echo "JAR file not found. Building application first..."
    ./build.sh
    
    if [ $? -ne 0 ]; then
        echo "Build failed. Cannot run application."
        exit 1
    fi
fi

echo "Starting Personal Digital Organizer & Secure Vault..."
echo "Default PIN: 1234"
echo ""

# Run the application
java -jar build/vault.jar