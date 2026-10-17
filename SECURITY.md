# Security and privacy

## Credentials

- Suric never requests or stores a ChatGPT password, browser cookie or ChatGPT app session.
- OAuth is opened in the system browser and uses a generated PKCE verifier, state and nonce. Callback values are validated before code exchange.
- Access/refresh/identity tokens are stored using AES-GCM with a key held in Android Keystore. Disconnect removes the local encrypted token record, its Android Keystore key, and the in-memory conversation. It retains only the non-secret issued client ID so a later sign-in can reuse the same registration. To revoke OpenAI-side access too, remove Suric under ChatGPT Settings → Apps.
- No OpenAI API key, password, private token or signing key belongs in source control. Git ignores local properties, signing material, environment files and build outputs.
- Network connections use HTTPS for OpenAI endpoints. The OAuth callback listener binds only to loopback on a temporary port; it does not listen on the network interface. TLS verification is not disabled.

## Personal data

- Notes, shopping entries, bills and other area records are encrypted at rest in app-private preferences. Android app backup is disabled to avoid copying the token vault/data to cloud backup.
- Calendar access is requested only when the user taps the Bills calendar control. Only selected upcoming event details are saved in Suric.
- Other apps' note text is accepted only from an explicit Android share intent or user entry. Suric does not inspect other apps' private files.
- Saved records are not sent to ChatGPT on app open. A user must explicitly choose the area action that includes saved note context. Conversation prompts and selected images are sent to the authenticated OpenAI service to answer the request.
- Android Photo Picker is used; no broad photo or storage permission is declared.

## Public-source hygiene

The repository is intended to remain personal/private in use even if source is shared. Do not commit OAuth tokens, client secrets, account email, logs containing prompts, screenshots with private account details, signing keys, local device data or generated test responses that contain personal information. Review changes before pushing anywhere.

## Known limits

AES encryption at rest does not protect data on an already unlocked, compromised device. Android Keystore key availability can be affected by OS reset or device migration; there is no cloud sync/backup or recovery process in this prototype. Loopback OAuth behavior and OpenAI's current private-client/token-sharing rules require device and current policy verification. No automated dependency scanner can prove runtime account/service security.
