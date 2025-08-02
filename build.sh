#!/bin/bash

# Personal Digital Organizer & Secure Vault - Build Script

echo "Building Personal Digital Organizer & Secure Vault..."

# Create build directory
mkdir -p build/classes

# Compile Java source files
echo "Compiling Java sources..."
javac -d build/classes -cp src/main/java src/main/java/vault/*.java

if [ $? -eq 0 ]; then
    echo "Compilation successful!"
    
    # Create executable JAR
    echo "Creating JAR file..."
    cd build/classes
    jar cfm ../vault.jar ../../manifest.txt vault/*.class
    cd ../..
    
    if [ $? -eq 0 ]; then
        echo "JAR created successfully: build/vault.jar"
        echo ""
        echo "To run the application:"
        echo "  java -jar build/vault.jar"
        echo "  or"
        echo "  ./run.sh"
    else
        echo "Failed to create JAR file"
        exit 1
    fi
else
    echo "Compilation failed"
    exit 1
fi

echo "Build completed successfully!"