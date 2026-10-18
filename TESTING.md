# Testing

## Automated checks

From a Java 17 + Android SDK 36 environment with network access for Gradle dependencies:

```bash
./gradlew testDebugUnitTest
./gradlew lintDebug
./gradlew assembleDebug
```

The existing unit-test suite covers authentication, mocked Responses behavior, conversation state, local-entry validation, Starter Library catalogue/search/copy behavior, and Home visibility preferences. CI also runs Android lint and builds debug and unsigned release APKs. No Retro-specific test cases were added.

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

## Verification status

GitHub Actions and CodeQL run against each pull-request revision. The branch's Android CI passed unit tests, Android debug and release lint, and debug and unsigned release APK builds before the test-file cleanup; CodeQL also completed successfully on that revision. The cleanup removes the two additional Retro-specific test methods, so those results do not apply to the cleanup commit. See pull request #13 for the latest checks.

No Android phone or emulator verification is claimed. Passing CI does not establish device behavior; use the acceptance steps above for manual verification.
