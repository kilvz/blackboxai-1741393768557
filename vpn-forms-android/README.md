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
