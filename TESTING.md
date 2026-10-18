# Testing

## Automated checks

From a Java 17 + Android SDK 36 environment with network access for Gradle dependencies:

```bash
./gradlew testDebugUnitTest
./gradlew lintDebug
./gradlew assembleDebug
```

Unit tests cover authentication transitions and expiry policy, mocked Responses streaming/model parsing and service errors, conversation state updates and local-entry encoding/validation. Starter Library tests check size/IDs/categories/destinations, offline case-insensitive search and copy formatting, plus the Android/API 1–37 map, PS1–PS5 guide coverage, HTTPS source links, Retro & Android search and source-preserving copies. Home visibility tests check defaults, filtering order, all-hidden handling and obsolete IDs. CI builds a debug APK and uploads it as a 30-day GitHub Actions artifact without ChatGPT credentials.

## Device acceptance

Use a physical Android 13+ device. No real-device check should be inferred from a mock test or successful build.

1. Install the debug APK with internet disabled and no ChatGPT connection. Open Home, browse/search the Starter Library, copy a template, create/edit a local note, restart offline and verify the note remains.
2. Hide several Home cards, restart, verify grid reflow, confirm hidden areas remain in the drawer, hide every card, and restore defaults. Confirm existing notes remain unchanged.
3. Re-enable internet and connect through **Continue with ChatGPT**. Cancel consent once and verify local sections remain available; then complete consent and check the connected state.
4. Ask: “What are the three most important things to consider when growing rosemary outdoors in Kilkenny, Ireland?” Confirm a live response, tap **Save to Notes**, disconnect and verify that response can be edited offline.
5. Ask a follow-up and verify earlier turns remain in request context; attach a photo through Android Photo Picker and verify a genuine visual response.
6. Add and edit a Shopping Assistant option with an HTTPS link, check date and details. Confirm insecure links are rejected, links open only after tapping, comparisons include only chosen options and current price/availability are described as unverified. Delete and undo an option, then restart and confirm persistence.
7. Add a bill, grant calendar access only when prompted, choose one upcoming event and verify it is saved locally.
8. Share plain text from another app to Suric and verify it appears in Notes. Confirm it is not sent to ChatGPT until an explicit Ask action.
9. Switch light/dark appearance, rotate, try larger system text and verify controls stay reachable. Use the drawer from Home, an area, Starter Library and the conversation; check Back behavior.
10. Disconnect, relaunch offline, and verify authentication is removed while local information remains available.

## Baseline and current environment evidence

Before this enhancement, the audited `main` baseline had passed `testDebugUnitTest`, `lintDebug`, and `assembleDebug` on 17 October 2026. Its documented debug APK was 19,692,265 bytes; that artifact does not verify this feature branch.

For this enhancement, the requested Gradle checks were attempted but could not start. Java 17.0.20 is installed, but this environment has no Android SDK (`ANDROID_HOME`/`ANDROID_SDK_ROOT` unset and no `sdkmanager` or `adb` found). The wrapper is correctly configured for Gradle 8.13, but the Gradle cache contains only an empty `.part` file and lock file; `./gradlew` cannot reach `services.gradle.org` because network access is unavailable. The combined `./gradlew testDebugUnitTest lintDebug assembleDebug` invocation and `./gradlew lintRelease assembleRelease` both stop while bootstrapping Gradle, before any task executes. Thus unit tests, Android lint, debug build, and release checks are **BLOCKED / NOT RUN**, not passing or failing product checks. No APK from this branch is available to report. `git diff --check` passes.

At this inspection, the feature branch is local and has not been published to GitHub; no remote branch run or pull-request CI result is available for these uncommitted changes. The base commit is `315241bbddf802b03630288ec773b6d13289c9fb`; the feature implementation remains uncommitted pending build and runtime verification.

The source diff does not modify `PersonalEntryStore`, its encrypted payload format or Keystore alias, `TokenVault`, the manifest, or the application ID (`ie.suric.suricapp`). Static integration inspection found all nine Home IDs match both the visibility preference IDs and existing personal-entry sections; all 28 parsed starter IDs are unique; and all referenced drawable resources resolve. These checks do not replace Kotlin compilation or automated tests. Upgrade preservation of data on an installed app has **NOT BEEN DEVICE-VERIFIED**.

No Android phone or emulator can be queried because `adb` is not installed. Suric's screen behavior, ChatGPT sign-in, live AI response, photo request, restart recovery, calendar flow, offline recovery, and error handling remain **NOT VERIFIED on-device**. GitHub Actions and CodeQL results are revision-specific; consult the pull request checks for the current branch head. Passing CI does not establish phone behavior.
