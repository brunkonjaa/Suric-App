# Suric

A private, personal Android companion for everyday household life: gardening, pottery, weekly shopping, bills, DIY, cooking, cleaning, music, retro consoles, Android devices and personal notes.

Suric is not a public service or a business product. It is a personal-use proof of concept. Personal entries are encrypted and stored on the device. The app reuses the ChatGPT sign-in and Responses connection proven in Ask Buster; there is no separately billed API key or custom server in this project. A new package install still needs its own real sign-in and request check before it can be called verified.

## Current experience

- Warm cream and sage colours, real photographic section cards, and a collapsible left navigation drawer.
- Light and dark appearance, saved on this device.
- Separate spaces with consistent add/edit bottom sheets, check-off controls where useful, and undo for deleted entries.
- Shopping Assistant saves product options with retailer links and the date you checked them. You choose which saved options to share with Ask Suric; it does not fetch retailer pages or verify live prices or stock.
- Each section includes bundled offline guides and recipes where available. Global offline search covers bundled guide content and recipes, with section filters. Cooking has canonical recipe data in a base asset plus supplemental assets registered by a versioned manifest, with 30-entry Irish, Argentinian, French, German, Italian and United States of America collections, detailed offline instructions, Ask Suric, share, encrypted personal copy, favourites, recently viewed filters and an ingredient handoff to Shopping. Guide and recipe reading state is kept on this device separately from personal records. The other requested cuisine and BBQ/seafood collection quotas are still being filled. Guides can be copied into encrypted personal notes without changing their bundled source.
- Music can play audio files selected with Android's document picker, save the queue locally and open online music or Naxi Radio through Android's app chooser. Local playback stops when leaving the Music screen.
- Personal entries can be shared as a user-selected, readable text copy. Bundled guides can be shared using a Suric URI when the receiving Suric installation contains the same article ID.
- Bills can be entered locally or selectively saved from upcoming calendar events after calendar permission is granted.
- Text notes can be shared from another Android app into Suric's Notes area.
- “Ask Suric” opens the conversation; message history and drafts are stored encrypted on this device. You can share a text transcript or delete the saved chat, and a section's saved notes are included only after you tap “Ask Suric about these notes”.
- Android's system photo picker attaches an image to a question.

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

1. Install and open Suric.
2. Tap **Continue with ChatGPT** and approve the app in the system browser.
3. Return to Suric, choose a space, then use **Add** to save something locally.
4. For AI help, open **Ask Suric**. Saved personal notes are not sent unless you explicitly choose a saved-note action. In Shopping Assistant, the comparison button sends only the options you chose to save, with a reminder that current retailer details are unverified.

A connected state means the sign-in flow completed. A genuine AI response must be verified from the installed Suric APK by sending a question and seeing the service response in the app; this workspace has no connected Android device or account credentials.

## Project documents

- [OFFLINE_CONTENT_INVENTORY.md](OFFLINE_CONTENT_INVENTORY.md) - current bundled content counts and gaps.


- [ARCHITECTURE.md](ARCHITECTURE.md) - app components and data flow.
- [FEASIBILITY.md](FEASIBILITY.md) - integration evidence and verification limits.
- [SECURITY.md](SECURITY.md) - credential and personal data handling.
- [TESTING.md](TESTING.md) - automated and device test checklist.
- [AGENTS.md](AGENTS.md) - scope and implementation rules.
