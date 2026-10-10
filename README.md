# DocuCraft

**A playful, privacy-first document studio for Android.**

DocuCraft is a native Kotlin application with an expressive interface: deep ink surfaces, electric-lime accents, violet details, spring motion, and expandable document cards.

## Current prototype

- Native Android app built with Kotlin and Jetpack Compose.
- Experimental dark UI with animated expandable document rows and a custom studio dashboard.
- Quick capture using Android's system camera preview.
- Import multiple images through Android's system picker.
- Combine selected images into a multi-page PDF.
- Keep generated PDFs in app-private storage.
- Search, share/export, and delete generated PDFs.
- No app network permission; documents are not uploaded by this prototype.

## Requirements

- Android Studio with Android SDK API 35.
- JDK 17.
- Android 8.0 (API 26) or newer.

## Run

1. Clone this repository and open the project root in Android Studio.
2. Allow Gradle to sync and install any requested SDK components.
3. Run the `app` configuration on an emulator or connected device.

GitHub Actions builds the debug APK and runs unit tests on pushes and pull requests to `main`.

## Current limitations

This is an early functional prototype, not a production-grade scanner. Quick capture uses Android's camera preview intent and does not provide a live in-app camera, edge detection, perspective correction, filters, OCR, or searchable PDF text. Imported images are embedded as image-based PDF pages. Export uses Android's file-sharing mechanism. These capabilities can be expanded after the first device test.

## Privacy

PDF files are stored in the app's private internal storage. Exporting shares a PDF with the app selected by the user. Uninstalling the app removes its private files. Images are read through Android's system picker.
