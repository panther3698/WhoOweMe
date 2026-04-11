# Who Owes Me 💸 (v1.5 - Local Only)

A modern, privacy-focused Android application built with Jetpack Compose to help you track debts, manage repayments, and maintain clear financial records with your contacts. **Who Owes Me** is designed for users who want a professional financial tracking tool without the risks of cloud storage.

## 🚀 What's New in v1.5 (Local Only)

- **Total Privacy**: All Cloud/Firebase dependencies have been completely removed. Your data never leaves your device.
- **Robust Local Database**: Upgraded to Room v6 with a smart migration system. Your data is safe and migrates automatically even during app updates.
- **Modern Android Compatibility**: Enhanced sharing and WhatsApp reminders optimized for Android 11 through Android 15 (API 30-35).
- **ProGuard/R8 Optimized**: Fully hardened for release with custom rules for WorkManager and Room stability.

## ✨ Core Functions & Use Cases

### 1. Simple Debt Management
*   **Track Receivables (Gave)**: Record money you lend to friends, family, or colleagues.
*   **Track Payables (Took)**: Keep track of money you owe to others to ensure timely repayment.
*   **Instant Balances**: See exactly how much someone owes you (or you owe them) at a glance.

### 2. Professional Reporting
*   **PDF Statement Generation**: Create professional-looking transaction reports for any contact.
*   **Instant Sharing**: Share statements via WhatsApp, Email, or Telegram to settle disputes or provide proof of payment.

### 3. Smart Reminders
*   **WhatsApp Integration**: Send pre-filled repayment reminders directly to WhatsApp.
*   **Due Date Tracking**: Set due dates for every transaction. The app tracks overdue payments and "missed promises."
*   **Smart Inheritance**: New transactions automatically inherit due dates from previous debts to maintain consistency in your records.

### 4. Advanced Security
*   **Biometric App Lock**: Secure your financial data with Fingerprint, Face Unlock, or Device PIN.
*   **Local Only Storage**: No account creation required. No data is sent to external servers.

### 5. Premium User Experience
*   **Material 3 Design**: A modern, responsive interface with full support for Light and Dark modes.
*   **Haptic Feedback**: Tactile interaction system provides physical confirmation for buttons and list items.
*   **Fluid Animations**: High-performance slide and fade transitions for a seamless navigation feel.

## 🛠 Tech Stack

- **UI**: Jetpack Compose with Material 3 & Custom Canvas Drawing
- **Navigation**: Compose Navigation with `AnimatedContentTransitionScope`
- **Database**: Room Persistence Library (v2.6.1) with Safe Migrations
- **Asynchronous Work**: Kotlin Coroutines & Flow
- **Background Tasks**: WorkManager (for local reminders)
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
*Built with ❤️ to make managing money between friends simpler, safer, and faster.*
