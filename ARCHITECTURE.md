# Architecture

Suric is a single-activity Android application with Compose screens and a screen-level `ConnectionViewModel`.

## Main components

| Component | Responsibility |
| --- | --- |
| `MainActivity` | Sets up Compose, saves the light/dark preference and creates the ViewModel. |
| `SuricScreens.kt` | Drawer, Home, area details, Settings, My Library browsing/copy, forms and optional conversation. |
| `SuricTheme.kt` | Warm light and dark Material 3 colour schemes. |
| `ConnectionViewModel` | Authentication state, token refresh, selected local entries and Responses conversation state. |
| `OpenAiOAuth` | Browser-based ChatGPT authorization, PKCE/state checks, token exchange, refresh and ID-token validation. |
| `TokenVault` | Encrypted persistence of OAuth credentials with Android Keystore-backed encryption. |
| `ChatApi` | Lists eligible models and streams Responses API output; sends an optional selected image. |
| `PersonalEntryStore` | AES-GCM encrypted, app-private local notes and list entries. |
| `StarterLibrary` | Immutable reference catalogue compiled into the APK; no network or personal-storage dependency. |
| `PersonalLibraryStore` | Device-local favourites, recently viewed IDs and shared HTTPS resources; kept separate from encrypted personal records. |
| `SectionVisibilityStore` | Device-local Home visibility and order for stable existing section IDs, independent of OAuth and personal records. |
| `MyDayPreferencesStore` | Device-local visibility and order for My Day panels, separate from personal records. |
| `CalendarEvents` | Reads a bounded upcoming calendar window after the user grants permission. |

## Data flow

1. The user selects Continue with ChatGPT. OpenAI authorization runs in the system browser, with a PKCE verifier, a random state value, nonce and loopback callback bound to `127.0.0.1` on a temporary port.
2. Suric checks callback path and state, exchanges the authorization code, verifies the signed ID token and required subscription-usage scope, then stores tokens encrypted.
3. For a question, the ViewModel refreshes an expiring token when permitted, resolves an available model and streams the genuine service response into conversation state.
4. Calendar entries and notes are read locally. A note enters an AI request only after the user taps the section's explicit Ask Suric action. A photo is selected through Android Photo Picker and attached to that request.
5. A contextual Ask Suric action places only the selected offline article in the draft. The user must submit it from the conversation screen; private records are not included.
6. Disconnect deletes local OAuth tokens and their Keystore key, plus the in-memory conversation. It retains only the issued client ID (not a secret) so later sign-in can reuse the OpenAI client registration; the user can revoke the grant in ChatGPT Settings.

## Local-first operation

Home and all personal sections load without ChatGPT authentication. Auth state controls only AI actions. The personal-entry store continues using its AES-GCM encryption and Android Keystore alias. Optional organiser fields are added to its JSON records; older encrypted records decode with defaults and are not rewritten until edited. Successful writes commit through the existing store before the UI reports success. Disconnect clears credentials and transient in-memory conversation state only; it does not touch personal records, appearance or Home visibility preferences.

Due-date notifications are opt-in per entry. The app uses an inexact AlarmManager alarm and requests Android notification permission only after the user enables a reminder. Create, edit, completion, deletion and undo update or cancel alarms. Boot, local-time and timezone broadcasts rebuild alarms from encrypted entries. Notifications contain the entry title and section, not its body. Photo picker selections are copied immediately to a generated file in app-private storage and encrypted with a separate Android Keystore AES-GCM key; only the generated filename is recorded. A missing or unreadable photo falls back to the normal record card. Orphaned photo files are removed after the encrypted record snapshot loads successfully.

Starter items are immutable Kotlin data bundled in the APK. Stable IDs, categories, type, structured blocks, image resource keys, revision and review date are separate from personal entries. **Copy to my space** creates a new ordinary entry in its intended existing section. Later catalogue revisions cannot modify that copy. There is no first-run seeding or network catalogue call.

Home visibility uses the nine existing section IDs. Missing preference data means all existing sections are visible; unknown IDs are ignored. The drawer always lists all sections. Filtering the existing lazy grid naturally reflows visible cards; an all-hidden state links to Settings. Appearance remains in the existing preferences file and is controlled from Settings.

Home ordering uses a separate ordered list of those same section IDs. Settings moves sections one position at a time and persists the order; hiding a section does not change its order or drawer access. My Library stores favourite IDs and a bounded recent-view list alongside shared HTTPS resources in its own preferences file. External links are opened only after a user taps them. It does not modify the encrypted personal-entry format or send references to ChatGPT.

Sharing an HTTPS link into Suric opens a destination sheet. My Library stores the canonical link with duplicate detection; Notes stores the URL and optional personal note through the existing encrypted personal-entry store. The optional note for a My Library destination is stored separately in encrypted Notes. Plain text shares without a valid HTTPS URL continue to enter Notes. Android Calendar insert, geo search, and system sharing are explicit intents launched only after the user taps a section action; Suric does not query installed apps, silently write calendar events, or transfer data without an explicit share action.

Starter guides can create an organiser project and a task carrying the same project title, category, tags, and source guide ID. The task and project remain ordinary encrypted personal records and are surfaced by existing My Day/search behaviour; the source guide remains immutable.

Assistant messages become saveable only after the Responses stream confirms completion. The explicit **Save to Notes** action writes the selected answer as a new encrypted Notes entry; unsaved conversation state remains transient and follows the existing lifecycle.

My Day is derived from encrypted personal records: it includes incomplete tasks, dated items and active projects without copying them into a second task list. Quick tasks are stored in the Tasks section. Marking a recurring dated record complete advances its next due date. Dashboard panel visibility/order is stored separately and never deletes records. Personal search filters the records already loaded on-device and does not send them to ChatGPT.

## Local model

`PersonalEntry` is an area-scoped record with a stable ID, title, details, updated timestamp and completion state. It can also store a record type, category, ISO due date, recurrence, linked project name, tags, bounded timestamped event history, an opt-in reminder and a generated photo filename. Older encrypted JSON records decode with defaults for these fields. Marking a bill paid adds a payment event to that same record; plant watering actions add watering events. Cooking records may use the `meal` type and planned date; these remain the same shared records surfaced in My Day and may be sent through Android's explicit text share chooser. This is a generic share handoff; no private app database is read and no direct Liz's Shopping List receiver is assumed. Shopping options retain their optional HTTPS retailer link and user-supplied check date. Suric opens a saved link only when the user taps it; it does not fetch retailer pages or verify live prices, stock or delivery.

The encrypted JSON codec is strict: malformed content fails visibly instead of becoming an empty collection. Android Keystore protects the AES key, and AES-GCM authenticates encrypted preferences. Calendar descriptions and user notes are not copied into ChatGPT automatically.

## Navigation and forms

A Material 3 modal drawer provides the same area navigation throughout the signed-in experience. Reusable area cards use bundled WebP photography from the supplied Suric section-image set. Add/edit forms use a modal bottom sheet with explicit Cancel and Save actions. List completion is an immediate tap; deleting a single entry can be undone from a snackbar. Shopping Assistant has its own link/date fields and comparison action; only options Bruno deliberately saved and sends are included in the prompt, which marks their details as user-supplied and current retailer information as unverified.
