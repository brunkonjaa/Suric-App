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
- My Library includes 69 offline reference articles/templates across Gardening, Pottery, Shopping, DIY & Home, Cooking, Cleaning, and Retro & Android, plus the existing two Bills templates. It supports search, category filters, favourites, recently viewed items and related-guide navigation. Reference content is separate from encrypted personal records; a copy is made only after tapping **Copy to my space**.
- Favourites and recently viewed references persist locally. Shared HTTPS links appear as external resources and open only when tapped; text shared from another app continues to become a private Notes entry.
- Shared HTTPS links now open a destination sheet: save to My Library or encrypted Notes, with an optional private note. Re-sharing a link to My Library uses the existing canonical URL deduplication.
- Offline guides can create a project and a linked exploratory task in the personal organiser. Contextual Ask Suric fills a draft with only the selected guide; it is not sent until you submit it.
- Section screens provide deliberate Android handoffs to Calendar, Maps for relevant areas, and the Android share sheet for saved section details. Suric checks for handler failures and leaves the current section in place when you return.
- Settings save Home-card visibility and section order separately from personal records and ChatGPT credentials. Hidden sections remain in the drawer; all cards can be hidden and restored.
- My Day brings the same encrypted tasks and dated records together, with completion, postponing, active projects, and a user-controlled section order/visibility. A quick-task entry goes to the shared Tasks section rather than creating a dashboard copy.
- Personal organiser areas include Tasks, Projects, My Devices and Inventory. Records can carry a type, category, ISO due date, daily/weekly/monthly/yearly recurrence, project name and tags; Search my records searches these fields locally.
- Bills support manually entered amounts, due dates, recurrence and paid/unpaid status. Marking a bill paid records a dated payment-history event; completing a recurring dated item advances its next due date. No bank connection or automatic payment claim is made.
- Plant records can log timestamped watering events. The latest three events appear on the plant record, with up to 100 retained per record.
- Dated tasks and bills can opt into a local notification around 9 AM on the due date. Android notification permission is requested only when you choose this. Alarms are restored after reboot and adjusted after device time changes; Android may deliver inexact alarms later.
- Personal records can attach one image from Android's system photo picker. Suric copies it into app-private storage, encrypts it with AES-GCM and stores only its generated file name in the encrypted record. Images are limited to 8 MB.
- Cooking records can be marked as meal plans with a planned date. Dated meals appear in My Day, and the saved recipe or ingredient text can be deliberately shared through Android's app chooser. A direct Liz's Shopping List integration is not available unless that app advertises a compatible share target.
- A completed Ask Suric response can be explicitly saved to encrypted Notes.

Suric does not read another app's private database. Notes enter Suric only through an Android share action or manual entry. Saved option links open through Android's link handler and are not fetched by Suric.

Calendar handoff creates a draft event in a compatible calendar app; Suric does not silently add events or request calendar write access.

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

1. Install and open Suric. Home, local sections, Settings and the offline parts of My Library work without an account or internet connection.
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
