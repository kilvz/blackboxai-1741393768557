#!/bin/bash

# Remove any existing zip file
rm -f vpn-forms-project.zip

# Remove the conflicting ic_launcher.png file
rm -f app/src/main/res/mipmap-hdpi/ic_launcher.png

# Create a zip file containing all project files
zip -r vpn-forms-project.zip \
    app/ \
    build.gradle \
    gradle.properties \
    gradlew \
    gradlew.bat \
    settings.gradle \
    gradle/ \
    README.md

echo "Project has been zipped to vpn-forms-project.zip"
echo "You can now download vpn-forms-project.zip which contains the complete project with the following changes:"
echo "1. Updated GitHub URL to point to the correct endpoint"
echo "2. Re-encrypted the URL in Constants.kt"
