# Who Owes Me 💸

A modern, privacy-focused Android application built with Jetpack Compose to help you track debts, manage repayments, and maintain clear financial records with your contacts.

## 🚀 Features

- **Transaction Tracking**: Easily record when you give money ("Gave") or receive a repayment ("Took").
- **PDF Statements**: Generate professional transaction reports for any contact and share them instantly via WhatsApp, Email, or other platforms.
- **Direct WhatsApp Reminders**: Send quick, pre-filled repayment reminders directly to WhatsApp without annoying browser redirects.
- **Smart Search & Filters**: Find specific entries by name, note, or exact amount. Filter history by transaction type to see your cash flow clearly.
- **Historical Entry Support**: Backdate transactions or contact creations to keep your records accurate even if you forgot to log them at the time.
- **Due Date System**: Set and track due dates for debts. Repayments automatically inherit the original debt's timeline for consistency.
- **Material 3 UI**: A beautiful, responsive interface that supports both Light and Dark modes.
- **Local Persistence**: All your data stays on your device using a secure Room database.

## 🛠 Tech Stack

- **UI**: Jetpack Compose with Material 3
- **Language**: Kotlin
- **Database**: Room Persistence Library
- **Asynchronous Work**: Kotlin Coroutines & Flow
- **Background Tasks**: WorkManager (for reminders and sync)
- **PDF Generation**: iText7
- **Architecture**: MVVM (Model-View-ViewModel)

## 📸 Screen Guide

- **Dashboard**: Overview of total receivables and payables.
- **Contact Details**: Deep dive into a specific person's history with quick action buttons for settlement and sharing.
- **Transaction History**: A searchable list of every financial move you've recorded.
- **Settings**: Customize your experience with Dark Mode and notification preferences.

## ⚙️ Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/panther3698/WhoOweMe.git
   ```
2. Open the project in **Android Studio (Ladybug or newer)**.
3. Build and run on an emulator or physical device (API 24+).

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

---
*Built with ❤️ to make managing money between friends simpler.*