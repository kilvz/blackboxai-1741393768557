#!/bin/bash

# Remove any existing project directory and zip
rm -rf vpn-forms-android
rm -f vpn-forms-android.zip

# Create project directory
mkdir -p vpn-forms-android
cd vpn-forms-android

# Create all necessary directories
mkdir -p app/src/main/java/com/vpnforms/{adapters,github,models,service,utils}
mkdir -p app/src/main/res/{drawable,layout,values,values-night,mipmap-hdpi}
mkdir -p gradle/wrapper

# Copy Gradle wrapper files
cp ../gradle/wrapper/gradle-wrapper.properties gradle/wrapper/

# Create Gradle wrapper scripts
cat > gradlew << 'EOL'
#!/usr/bin/env sh
exec "$(dirname "$0")/gradle/wrapper/gradle-wrapper.jar" "$@"
EOL

cat > gradlew.bat << 'EOL'
@rem
@rem Copyright 2015 the original author or authors.
@rem
@rem Licensed under the Apache License, Version 2.0 (the "License");
@rem you may not use this file except in compliance with the License.
@rem You may obtain a copy of the License at
@rem
@rem      https://www.apache.org/licenses/LICENSE-2.0
@rem
@rem Unless required by applicable law or agreed to in writing, software
@rem distributed under the License is distributed on an "AS IS" BASIS,
@rem WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
@rem See the License for the specific language governing permissions and
@rem limitations under the License.
@rem

@if "%DEBUG%" == "" @echo off
@rem ##########################################################################
@rem
@rem  Gradle startup script for Windows
@rem
@rem ##########################################################################

@if "%OS%"=="Windows_NT" setlocal

set DIRNAME=%~dp0
if "%DIRNAME%" == "" set DIRNAME=.
set APP_BASE_NAME=%~n0
set APP_HOME=%DIRNAME%

@rem Add default JVM options here. You can also use JAVA_OPTS and GRADLE_OPTS to pass JVM options to this script.
set DEFAULT_JVM_OPTS="-Xmx64m" "-Xms64m"

@rem Find java.exe
if defined JAVA_HOME goto findJavaFromJavaHome

set JAVA_EXE=java.exe
%JAVA_EXE% -version >NUL 2>&1
if "%ERRORLEVEL%" == "0" goto init

echo.
echo ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.
echo.
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation.

goto fail

:findJavaFromJavaHome
set JAVA_HOME=%JAVA_HOME:"=%
set JAVA_EXE=%JAVA_HOME%/bin/java.exe

if exist "%JAVA_EXE%" goto init

echo.
echo ERROR: JAVA_HOME is set to an invalid directory: %JAVA_HOME%
echo.
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation.

goto fail

:init
@rem Get command-line arguments, handling Windows variants

if not "%OS%" == "Windows_NT" goto win9xME_args

:win9xME_args
@rem Slurp the command line arguments.
set CMD_LINE_ARGS=

:win9xME_args_slurp
if "x%~1" == "x" goto execute

set CMD_LINE_ARGS=%*

:execute
@rem Setup the command line

set CLASSPATH=%APP_HOME%\gradle\wrapper\gradle-wrapper.jar

@rem Execute Gradle
"%JAVA_EXE%" %DEFAULT_JVM_OPTS% %JAVA_OPTS% %GRADLE_OPTS% "-Dorg.gradle.appname=%APP_BASE_NAME%" -classpath "%CLASSPATH%" org.gradle.wrapper.GradleWrapperMain %CMD_LINE_ARGS%

:end
@rem End local scope for the variables with windows NT shell
if "%ERRORLEVEL%"=="0" goto mainEnd

:fail
rem Set variable GRADLE_EXIT_CONSOLE if you need the _script_ return code instead of
rem the _cmd.exe /c_ return code!
if  not "" == "%GRADLE_EXIT_CONSOLE%" exit 1
exit /b 1

:mainEnd
if "%OS%"=="Windows_NT" endlocal

:omega
EOL

chmod +x gradlew

# Copy configuration files
cp ../build.gradle .
cp ../settings.gradle .
cp ../gradle.properties .
cp ../app/build.gradle app/

# Copy source files from current directory
cp -r ../app/src/main/java/com/vpnforms/* app/src/main/java/com/vpnforms/
cp -r ../app/src/main/res/drawable/* app/src/main/res/drawable/
cp -r ../app/src/main/res/layout/* app/src/main/res/layout/
cp -r ../app/src/main/res/values/* app/src/main/res/values/
cp -r ../app/src/main/res/values-night/* app/src/main/res/values-night/

# Copy only XML launcher icons
cp ../app/src/main/res/mipmap-hdpi/ic_launcher.xml app/src/main/res/mipmap-hdpi/
cp ../app/src/main/res/mipmap-hdpi/ic_launcher_round.xml app/src/main/res/mipmap-hdpi/

cp ../app/src/main/AndroidManifest.xml app/src/main/

# Create README
cat > README.md << 'EOL'
# VPN Forms Android App

An Android application that provides secure access to Google Forms through a VPN service.

## Requirements

- Android Studio Flamingo (2022.2.1) or newer
- JDK 17
- Android SDK 33
- Android device running Android 7.0 (API 24) or higher

## Setup

1. Create a forms.json file on GitHub with your form configurations
2. Update the GITHUB_RAW_URL in Constants.kt with your GitHub raw URL
3. Build and run the app
4. Grant VPN permissions when prompted

## Features

- Automatic VPN configuration
- Material Design UI
- Custom form titles and descriptions
- Secure form access
- Dark mode support

## Configuration

Create a forms.json file on GitHub:
```json
{
  "forms": [
    {
      "title": "Student Registration",
      "description": "Register for courses",
      "url": "https://docs.google.com/forms/d/YOUR_FORM_ID"
    }
  ]
}
```

Update Constants.kt with your GitHub URL:
```kotlin
const val GITHUB_RAW_URL = "https://raw.githubusercontent.com/YOUR_USERNAME/YOUR_REPO/main/forms.json"
```
EOL

# Create a zip file
cd ..
zip -r vpn-forms-android.zip vpn-forms-android/

echo "Project has been packaged into vpn-forms-android.zip"
echo "You can now download the zip file and open it in Android Studio"
