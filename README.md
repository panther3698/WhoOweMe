# Who Owes Me 💸 (v1.5 - Local Only)

A modern, privacy-focused Android application built with Jetpack Compose to help you track debts, manage repayments, and maintain clear financial records with your contacts.

## 🚀 What's New in v1.5 (Local Only)

- **Total Privacy**: All Cloud/Firebase dependencies have been removed. Your data never leaves your device.
- **Robust Local Database**: Upgraded to Room v6 with a smart migration system. Your data is safe even during app updates.
- **Modern Android Queries**: Enhanced sharing and WhatsApp reminders for Android 11+ (API 30-35).
- **ProGuard/R8 Optimized**: Fully hardened for release with custom rules for WorkManager and Room stability.

## ✨ Core Features

- **Local Persistence**: All your data stays on your device using a secure, version-controlled Room database.
- **Transaction Tracking**: Easily record when you give money ("Gave") or receive a repayment ("Took").
- **PDF Statements**: Generate professional transaction reports for any contact and share them instantly via WhatsApp, Email, or other platforms.
- **Biometric App Lock**: Secure your financial data with fingerprint or face unlock.
- **Smart Due Dates**: Set and track due dates. Repayments automatically inherit the original debt's timeline for consistency.
- **Haptic Feedback**: Tactile interaction system with long-press sensations for primary buttons and list items.
- **Material 3 UI**: A beautiful, responsive interface that supports both Light and Dark modes.

## 🛠 Tech Stack

- **UI**: Jetpack Compose with Material 3 & Custom Canvas Drawing
- **Navigation**: Compose Navigation with `AnimatedContentTransitionScope`
- **Language**: Kotlin
- **Database**: Room Persistence Library (v2.6.1)
- **Asynchronous Work**: Kotlin Coroutines & Flow
- **Background Tasks**: WorkManager
- **PDF Generation**: iText7
- **Architecture**: MVVM (Model-View-ViewModel)

## ⚙️ Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/panther3698/WhoOweMe.git
   ```
2. Open the project in **Android Studio (Ladybug or newer)**.
3. Build and run on an emulator or physical device (API 24+).

---
*Built with ❤️ to make managing money between friends simpler.*
