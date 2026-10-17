# Suric project rules

## Purpose and scope

Suric is for the owner's own everyday use. Keep it private and personal. Build useful spaces for gardening, pottery, shopping, bills, notes, DIY, cooking, cleaning, retro consoles and Android devices. Do not turn this into a public platform, subscription product, business tool, or unrelated lifestyle app. Do not modify Liz's Shopping List or another repository.

## Interaction and visual quality

- Use the warm, photographic, restrained design direction from the supplied Liz's Shopping List guidelines as a visual reference, adapting each area to Suric.
- Prioritise comprehension and user control: stable navigation, clear one-line labels, one obvious primary action, predictable placement, comfortable spacing, visible state and plain error recovery.
- Keep screens calm and scannable. Avoid unnecessary choice, duplicate actions, decorative clutter and forced multi-step flows.
- Use the shared Material 3 bottom sheet pattern for create/edit forms. Cancel never writes; Save validates and writes once; Android Back and swipe dismissal behave predictably; keep controls reachable with the keyboard open.
- Use undo for routine item deletion. Ask confirmation only before irreversible loss of significant data.
- Support light and dark appearance with readable contrast and meaningful accessibility labels. Keep tap targets at least 48 dp.

## Data and privacy

- Personal notes stay local unless the user explicitly invokes an AI action that includes them.
- Request Android permissions only when the user starts a feature that requires them.
- Use Android's system picker and share intents. Never inspect another app's private storage.
- Keep tokens in the encrypted token vault. Never log tokens, prompts containing personal notes, or OAuth callback parameters.
- Do not add API keys, backend infrastructure, third-party analytics, or a paid service.
- Preserve saved entries on parse, write, migration or network failure. Never silently replace unreadable data with an empty list.

## Engineering

- Kotlin, Jetpack Compose, Material 3, Gradle Kotlin DSL, Java 17, min SDK 26.
- Keep UI, local storage, authentication and network request code in separate files/components.
- Add deterministic unit tests for pure logic and API parsing; use mocks for network-dependent tests.
- Run `./gradlew testDebugUnitTest lintDebug assembleDebug` before calling local checks complete.
- Never describe mocks as proof of a real ChatGPT request. State device/account checks separately.
- Keep dependencies current only through reviewed version changes; CI must not need personal credentials.
