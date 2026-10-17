# Feasibility and evidence

## Current conclusion

**Potentially supported and locally implemented; Suric still needs real-device verification.** The user previously supplied screenshots showing Ask Buster completing ChatGPT consent, returning to its own app, showing the signed-in account and rendering model-generated text and image answers. Suric reuses that Kotlin OAuth/Responses implementation under its own application ID and UI.

Those screenshots are evidence about the existing Ask Buster proof of concept, not evidence that this Suric APK has been installed, signed in or sent a successful request. Android SDK Platform 36 and Gradle are available in this workspace. Local unit tests, lint and debug APK build passed on 17 October 2026; the exact evidence is in `TESTING.md`. No Android phone or emulator is connected, so Suric's OAuth and inference remain unverified on this package.

## Findings from the current official documentation

Checked 17 October 2026 against the official SIWC docs linked below.

1. **Native Kotlin Android:** The official open-source protocol specifies a system browser and an HTTP loopback callback bound to `127.0.0.1`, with a temporary port allowed. The documentation does not name Android as a separate supported platform and does not provide a Kotlin Android SDK/reference app. A native app can implement this general public-client protocol, but platform eligibility is not explicitly promised by the docs. This project follows the documented loopback/state/nonce/PKCE mechanics; the Suric install still needs an on-device trial.
2. **Open-source eligibility:** A public GitHub repository by itself is not an approval guarantee. Open-source app usage is supported through the documented dynamic-registration flow; private clients are described as selected. The user’s supplied Ask Buster screenshots show consent for that earlier app, not approval of Suric’s separate OAuth registration.
3. **Registration:** First-time SIWC open-source registration uses `dynamic_agent_client` plus a stable opaque `ext_agent_host_id`; the browser callback returns the issued client ID, which must be stored and used thereafter. The code requires the new issued ID and uses the returned ID for token exchange. A client secret or API key is not used in this public-client flow.
4. **OAuth and callback:** Current docs call for a system browser, exact `127.0.0.1` loopback scheme/host/path, the same selected port in authorization and code exchange, random state and nonce, PKCE S256, callback-state validation, signature/issuer/audience/expiry/nonce ID-token validation, and checking the granted `chatgpt.tokens.use.direct` scope. The app implements these mechanics and commits a `urn:uuid:` host identifier before opening the browser, which the docs list as a supported format. It also retains the issued client ID when local tokens are disconnected, so later sign-in reuses that registration. The callback listener is local to the device; there is no custom server.
5. **Inference:** The open-source flow supports the public Responses API at `https://api.openai.com/v1/responses` with the OAuth access token. Requests must use `store: false` and `stream: true`; this code does. Model availability is account-specific and comes from `/v1/models`. The current preview docs allow text and images for eligible models, with tool/field limitations. The app does not use ChatGPT's private backend endpoints.
6. **Subscription use:** Eligible ChatGPT Plus and Pro accounts may use their ChatGPT plan for eligible requests in participating open-source apps; selected private clients are also mentioned. This is permission- and policy-dependent, not general API access. Plus usage limits are shared across SIWC apps rather than granting each app a separate allowance.
7. **Standalone Android/no backend:** The documented local-client flow does not require a custom backend or API key. Since no official Android SDK is documented, this Kotlin app contains its own small OAuth/client and HTTP implementation. That is protocol implementation, not an OpenAI-certified Android SDK.
8. **Images:** The current preview limitations permit image input when accepted by the chosen model. Suric attaches only a user-selected image using Android Photo Picker; only a successful real-device response can confirm the full flow for this package.

## Suric implementation details

- Android opens the authorization URL in the system browser; the app does not collect the ChatGPT password.
- The temporary callback binds only to `127.0.0.1`. Suric checks the expected path and state, rejects duplicate query parameters, checks that a new registration returned a client ID, verifies the ID token, and checks the plan-use scope.
- Refresh tokens and other OAuth credentials are AES-GCM encrypted using an Android Keystore key.
- `ChatApi` requests the account-specific model catalog and streams the Responses API output. It sends local notes only after an explicit Ask action and sends a photo only after the user selects one.
- Calendar access is optional and requested only when the user opens the Bills calendar control. Notes can be imported from another app through Android's explicit share flow. Suric cannot read another app's private note database.

## Official references

This project inherited the SIWC/Responses implementation that was used in the user's Ask Buster proof of concept. Recheck current requirements before changing authentication or distributing the app:

- [Sign in with ChatGPT quickstart](https://developers.openai.com/siwc/quickstart)
- [Token sharing for open-source projects](https://developers.openai.com/siwc/token-sharing-open-source)
- [Sign-in details](https://developers.openai.com/siwc/token-sharing-open-source/sign-in)
- [Models and inference](https://developers.openai.com/siwc/token-sharing-open-source/models-and-inference)
- [Preview limitations](https://developers.openai.com/siwc/token-sharing-open-source/preview-limitations)
- [Responses API](https://developers.openai.com/api/docs/guides/text)

A private personal Android APK is not made eligible simply by existing or by the user's subscription. Client identity, applicable OpenAI enrollment/approval, account eligibility, current usage limits and current preview restrictions remain controlled by OpenAI. This document records the user's Ask Buster evidence and code reuse; it does not substitute for current OpenAI approval or Suric-specific device evidence.

## Outstanding checks

- Build and run Suric on a physical Samsung Android device.
- Complete OAuth in the system browser and confirm return to this package's callback listener.
- Send the rosemary question through Suric and confirm the response arrives through the authenticated Responses API.
- Restart the app and check session recovery; disconnect and confirm stored tokens are removed.
- Select a photo with Android Photo Picker and verify a genuine visual answer.
- Grant calendar permission, import one event, and confirm it remains local until explicitly shared with AI.
- Test local share-from-Notes behavior with the device's actual Notes app.
