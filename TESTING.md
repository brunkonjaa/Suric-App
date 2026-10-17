# Testing

## Automated checks

From a Java 17 + Android SDK 36 environment with network access for Gradle dependencies:

```bash
./gradlew testDebugUnitTest
./gradlew lintDebug
./gradlew assembleDebug
```

Unit tests cover authentication transitions and expiry policy, mocked Responses streaming/model parsing and service errors, conversation state updates and local-entry encoding/validation. CI builds a debug APK and uploads it as a 30-day GitHub Actions artifact without ChatGPT credentials.

## Device acceptance

Use a physical Android 13+ device. No real-device check should be inferred from a mock test or successful build.

1. Install the debug APK and connect through **Continue with ChatGPT**.
2. Cancel the browser consent screen and verify the app remains signed out with a clear state.
3. Complete consent; verify the browser returns to Suric and the status is connected.
4. Ask: “What are the three most important things to consider when growing rosemary outdoors in Kilkenny, Ireland?” Confirm the answer is a live response shown inside Suric.
5. Ask a follow-up and verify the earlier turns remain in the request context.
6. Attach a real photo using Android Photo Picker and ask a question about it. Verify the live response describes the selected photo.
7. Add and edit a Shopping Assistant option with an HTTPS link, check date and details. Confirm insecure links are rejected, the saved retailer link opens only after tapping it, compare action includes only saved options, and current price/availability are described as unverified. Delete and undo an option, then restart and confirm persistence.
8. Add a bill with amount/date, grant calendar access only when prompted, choose one upcoming event, and verify it is saved locally.
9. Share plain text from the device's Notes app to Suric and verify it appears in Notes. Confirm it is not sent to ChatGPT until an explicit Ask action.
10. Switch light/dark appearance, rotate the device, try larger system text, and verify section labels remain one line and all primary controls stay reachable.
11. Use the drawer from Home, an area and the conversation. Verify Back closes the drawer/sheet before leaving its screen.
12. Disconnect, relaunch, and verify authentication is removed and the app requests sign-in again.

## Current environment evidence

On 17 October 2026, the final local source revision passed `testDebugUnitTest`, `lintDebug`, and `assembleDebug` with Java 17 and Android SDK Platform 36. The debug APK was produced at `app/build/outputs/apk/debug/app-debug.apk` (19,509,221 bytes; SHA-256 `1F8951D2263BB522B7B6686E5D334F04B70317A84F7CDB9FB98BDEA9B53B7944`). The APK is a local debug artifact and is not a release build.

No Android phone or emulator is connected (`adb devices -l` returned no devices). Suric's screen behavior, ChatGPT sign-in, live AI response, photo request, restart recovery, calendar flow, and error handling remain unverified on-device. These local checks do not establish that GitHub Actions passed; no workflow run has been triggered for this revision.
