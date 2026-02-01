---
description: Build APK and Move to Delivery Folder
---
1. Clean and Build Debug APK
// turbo
2. .\gradlew assembleDebug

3. Run Move Script to organize APKs
// turbo
4. powershell -ExecutionPolicy Bypass -File .\.agent\scripts\move_apk.ps1
