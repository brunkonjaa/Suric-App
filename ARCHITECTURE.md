# Architecture

Suric is a single-activity Android application with Compose screens and a screen-level `ConnectionViewModel`.

## Main components

| Component | Responsibility |
| --- | --- |
| `MainActivity` | Sets up Compose, saves the light/dark preference and creates the ViewModel. |
| `SuricScreens.kt` | Signed-out connection screen, drawer, home, area details, forms and conversation. |
| `SuricTheme.kt` | Warm light and dark Material 3 colour schemes. |
| `ConnectionViewModel` | Authentication state, token refresh, selected local entries and Responses conversation state. |
| `OpenAiOAuth` | Browser-based ChatGPT authorization, PKCE/state checks, token exchange, refresh and ID-token validation. |
| `TokenVault` | Encrypted persistence of OAuth credentials with Android Keystore-backed encryption. |
| `ChatApi` | Lists eligible models and streams Responses API output; sends an optional selected image. |
| `PersonalEntryStore` | AES-GCM encrypted, app-private local notes and list entries. |
| `CalendarEvents` | Reads a bounded upcoming calendar window after the user grants permission. |

## Data flow

1. The user selects Continue with ChatGPT. OpenAI authorization runs in the system browser, with a PKCE verifier, a random state value, nonce and loopback callback bound to `127.0.0.1` on a temporary port.
2. Suric checks callback path and state, exchanges the authorization code, verifies the signed ID token and required subscription-usage scope, then stores tokens encrypted.
3. For a question, the ViewModel refreshes an expiring token when permitted, resolves an available model and streams the genuine service response into conversation state.
4. Calendar entries and notes are read locally. A note enters an AI request only after the user taps the section's explicit Ask Suric action. A photo is selected through Android Photo Picker and attached to that request.
5. Disconnect deletes local OAuth tokens and their Keystore key, plus the in-memory conversation. It retains only the issued client ID (not a secret) so later sign-in can reuse the OpenAI client registration; the user can revoke the grant in ChatGPT Settings.

## Local model

`PersonalEntry` is an area-scoped record with a stable ID, title, details, updated timestamp and completion state. Shopping options also store an optional HTTPS retailer link and the date Bruno says he checked it. Older encrypted JSON records decode with those fields empty. Suric opens a saved link only when Bruno taps it; it does not fetch retailer pages or verify live prices, stock or delivery. This remains a small personal research list, not a retailer catalogue, bill-payment system or gardening database.

The encrypted JSON codec is strict: malformed content fails visibly instead of becoming an empty collection. Android Keystore protects the AES key, and AES-GCM authenticates encrypted preferences. Calendar descriptions and user notes are not copied into ChatGPT automatically.

## Navigation and forms

A Material 3 modal drawer provides the same area navigation throughout the signed-in experience. Reusable area cards use bundled WebP photography from the supplied Suric section-image set. Add/edit forms use a modal bottom sheet with explicit Cancel and Save actions. List completion is an immediate tap; deleting a single entry can be undone from a snackbar. Shopping Assistant has its own link/date fields and comparison action; only options Bruno deliberately saved and sends are included in the prompt, which marks their details as user-supplied and current retailer information as unverified.
