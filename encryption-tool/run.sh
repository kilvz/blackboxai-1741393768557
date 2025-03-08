#!/bin/bash

echo "VPN Forms Encryption Tool"
echo "========================"

# Check if Java is installed
if ! command -v java &> /dev/null; then
    echo "Error: Java is required but not installed."
    exit 1
fi

# Check if urls.txt exists
if [ ! -f "urls.txt" ]; then
    echo "Error: urls.txt not found."
    echo "Please create urls.txt with your GitHub URL:"
    echo "https://raw.githubusercontent.com/username/repo/main/forms.json"
    exit 1
fi

# Compile Java code
echo "Compiling encryption tool..."
javac ConstantsEncryptor.java

if [ $? -ne 0 ]; then
    echo "Error: Compilation failed."
    exit 1
fi

# Run encryption
echo -e "\nRunning encryption..."
java ConstantsEncryptor

# Clean up
rm -f *.class

echo -e "\nDone! You can now update your Constants.kt with the encrypted values."
