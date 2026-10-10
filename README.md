# DocuCraft

**A privacy-first document studio for Android.**

DocuCraft is a native Kotlin and Jetpack Compose app with an experimental dark interface, electric-lime accents, violet details, and animated document cards.

## Features

- Full document scanning workflow powered by Google Play services Document Scanner, including automatic edge detection, crop and perspective adjustment, image enhancement, and multi-page capture.
- Scanner can return PDF and JPEG output; the app saves the resulting PDF in app-private storage.
- Import multiple images and create a PDF.
- Search saved PDFs, export/share documents, and delete them.
- No app-defined INTERNET permission; scan results are copied into the app's private documents directory.

## Scanner requirements

A compatible Android device or emulator with Google Play services is required. The first use may download the scanner module. The scanner experience is supplied by Google Play services rather than implemented as a custom camera screen in DocuCraft.

## Build and run

1. Open the project folder in Android Studio.
2. Wait for Gradle sync and dependency downloads to finish.
3. Run the `app` configuration on a compatible device or emulator.

The updated code has been committed to GitHub, but has not yet been verified on a physical device. Please run it locally and share any build or runtime errors so they can be fixed.
