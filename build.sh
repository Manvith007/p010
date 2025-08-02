#!/bin/bash

# Personal Digital Organizer & Secure Vault Console
# Build and Run Script

echo "=========================================="
echo "Personal Digital Organizer & Secure Vault"
echo "=========================================="

# Create necessary directories
echo "Creating directories..."
mkdir -p bin
mkdir -p logs
mkdir -p vault_data

# Compile Java source files
echo "Compiling Java sources..."
find src/main/java -name "*.java" > sources.txt

if javac -d bin -cp src/main/java @sources.txt; then
    echo "✓ Compilation successful!"
else
    echo "✗ Compilation failed!"
    rm -f sources.txt
    exit 1
fi

# Clean up
rm -f sources.txt

# Run the application
echo "Starting application..."
echo "=========================================="
java -cp bin Main

echo "=========================================="
echo "Application terminated."