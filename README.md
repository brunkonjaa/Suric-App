# Suric

A private, personal Android companion for everyday household life: gardening, pottery, weekly shopping, bills, DIY, cooking, cleaning, retro consoles, Android devices and personal notes.

Suric is not a public service or a business product. It is a personal-use proof of concept. Personal entries are encrypted and stored on the device. The app reuses the ChatGPT sign-in and Responses connection proven in Ask Buster; there is no separately billed API key or custom server in this project. A new package install still needs its own real sign-in and request check before it can be called verified.

## Current experience

- Warm cream and sage colours, real photographic section cards, and a collapsible left navigation drawer.
- Light and dark appearance, saved on this device.
- Separate spaces with consistent add/edit bottom sheets, check-off controls where useful, and undo for deleted entries.
- Shopping Assistant saves product options with retailer links and the date you checked them. You choose which saved options to share with Ask Suric; it does not fetch retailer pages or verify live prices or stock.
- Bills can be entered locally or selectively saved from upcoming calendar events after calendar permission is granted.
- Text notes can be shared from another Android app into Suric's Notes area.
- “Ask Suric” opens the conversation; a section's saved notes are included only after you tap “Ask Suric about these notes”.
- Android's system photo picker attaches an image to a question.
- Suric opens directly in local mode. ChatGPT sign-in is optional and requested only when the user chooses an AI action.
- A bundled offline Starter Library provides 28 guides, recipes, checklists and blank templates. Reference content is separate from encrypted personal records; a copy is made only after tapping **Copy to my space**.
- Settings save Home-card visibility separately from personal records and ChatGPT credentials. Hidden sections remain in the drawer; all cards can be hidden and restored.
- A completed Ask Suric response can be explicitly saved to encrypted Notes.

Suric does not read another app's private database. Notes enter Suric only through an Android share action or manual entry. Saved option links open through Android's link handler and are not fetched by Suric.

## Build

Requirements: Android Studio with Android SDK Platform 36, Java 17, and network access for the first Gradle/dependency download.

```bash
./gradlew testDebugUnitTest
./gradlew lintDebug
./gradlew assembleDebug
```

Install `app/build/outputs/apk/debug/app-debug.apk` on an Android device with:

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

The debug APK is unsigned for release. Do not publish or redistribute it; it is intended for personal installation. GitHub Actions (when run in a connected GitHub repository) builds and stores a debug APK artifact without requiring ChatGPT credentials.

## First use

1. Install and open Suric. Home, local sections, Settings and Starter Library work without an account or internet connection.
2. Choose a space and use **Add** to save something locally, or browse and explicitly copy a library item.
3. For optional AI help, open **Ask Suric**, tap **Continue with ChatGPT** and approve in the system browser.
4. Saved personal notes are not sent unless you explicitly choose a saved-note action. In Shopping Assistant, the comparison button sends only the options you chose to save, with a reminder that current retailer details are unverified.

A connected state means the sign-in flow completed. A genuine AI response must be verified from the installed Suric APK by sending a question and seeing the service response in the app; this workspace has no connected Android device or account credentials.

Starter items and maintenance details are listed in [STARTER_LIBRARY.md](STARTER_LIBRARY.md).

## Project documents

- [ARCHITECTURE.md](ARCHITECTURE.md) — app components and data flow.
- [FEASIBILITY.md](FEASIBILITY.md) — integration evidence and verification limits.
- [SECURITY.md](SECURITY.md) — credential and personal data handling.
- [TESTING.md](TESTING.md) — automated and device test checklist.
- [AGENTS.md](AGENTS.md) — scope and implementation rules.
