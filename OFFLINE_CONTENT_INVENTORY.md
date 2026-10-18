# Offline content inventory

This inventory describes the bundled JSON currently in `app/src/main/assets/`. The 30 rows below are authored article records with stable IDs and revision 1. Counts describe article records, not the larger content goal in the implementation brief.

The two JSON source files total **39,608 bytes** (18,252 + 21,356 bytes). They contain text only; no offline library photos are bundled. This is source-file size, not the final APK's compressed or installed asset size.

## Complete bundled articles by section

| Section | Complete articles | Examples | Online-only functions |
| --- | ---: | --- | --- |
| Garden | 5 | Tomato growth diagnosis, hardening off seedlings, compost, container potatoes, potted herbs | Current weather and local frost conditions |
| Cooking | 11 | Irish stew, Sekeli-style pork and sauerkraut goulash, tomato and lentil soup, boxty, chana masala, spanakorizo | None for the bundled recipes |
| Cleaning | 3 | Bathroom routine and chemical safety, laundry stains, kitchen touch points | None for the bundled guides |
| DIY & Home | 3 | Wall fixings, measuring a straight cut, refreshing a small sealant joint | Current product availability and manufacturer-specific instructions |
| Pottery | 3 | Wedging, pinch cup, even drying | Clay, glaze and kiln maker schedules must be checked with the specific product |
| Retro & Android | 3 | Console compatibility checks, emulator save backups, Android build details | Current firmware and exploit compatibility |
| Bills | 1 | Monthly bill review checklist | Provider balances, live account details and payments |
| Shopping | 1 | Online option comparison checklist | Live price, stock, delivery and retailer terms |
| Music | 0 | Personal local audio queue is user-supplied, not bundled knowledge | Online services and radio |
| Notes | 0 | Private user-created notes | None |
| **Total** | **30** | **19 guides and 11 Cooking recipe records** | — |

Cooking records include one sauce recipe (Argentinian-style chimichurri). They are not yet assigned to structured cuisine, BBQ, or Fish & Seafood collections. The cooking section contains 11 unique authored recipe records; collection membership counts are currently zero because those collections are not implemented.

## Cuisine and special-collection quotas

The requested 24-cuisine quota is not complete. No collection memberships have been declared, so each requested cuisine currently has 0 of 30 entries, BBQ has 0 of 100, and Fish & Seafood has 0 of 100. Existing recipes can later be referenced by multiple collections without duplicating their canonical article records.

| Requested cuisine group | Entries assigned |
| --- | ---: |
| African regional cuisines | 0 / 30 |
| Argentinian | 0 / 30 |
| Bosnian | 0 / 30 |
| Brazilian | 0 / 30 |
| Bulgarian | 0 / 30 |
| Chinese | 0 / 30 |
| Croatian | 0 / 30 |
| French | 0 / 30 |
| German | 0 / 30 |
| Greek | 0 / 30 |
| Hungarian | 0 / 30 |
| Indian | 0 / 30 |
| Irish | 0 / 30 |
| Italian | 0 / 30 |
| Japanese | 0 / 30 |
| Macedonian | 0 / 30 |
| Mexican | 0 / 30 |
| Moroccan | 0 / 30 |
| Nigerian | 0 / 30 |
| Persian | 0 / 30 |
| Romanian | 0 / 30 |
| Serbian | 0 / 30 |
| Spanish | 0 / 30 |
| American | 0 / 30 |
| BBQ Around the World | 0 / 100 |
| Fish & Seafood Around the World | 0 / 100 |

## Storage and navigation

The app loads the two version-1 JSON asset files off the main thread. Each article has a stable ID, section, title, summary, revision, tags and full text. Search checks titles, summaries, tags and body text across the bundled library. Selecting an article opens its detail view; Back returns to the same section and retains its list scroll state. `suric://library/{id}` can open a matching bundled article in an installed Suric copy. No verified HTTPS App Links domain is configured.

**Copy to section** creates an independent encrypted personal entry. It does not alter the bundled article, and later edits/deletion affect only the personal copy. Personal entries can be moved among sections in their editor; the move retains the entry ID and encrypted store, and shopping-only retailer metadata is cleared when moved out of Shopping.

Outgoing private-entry sharing requires confirmation and uses Android Sharesheet with a versioned text envelope. The receiving app shows a preview and saves only after confirmation. The export contains only the selected title, section and body; it excludes the local entry ID, credentials and other records. The text copy is readable by the chosen receiving app. Image attachments and portable binary bundles are not implemented.

## Music, photos and AI

Music can select local audio through Android's document picker, retain its granted URI, save a queue entry, play/pause, seek and remember the last position. This implementation has no playback service or notification controls, and playback stops when the Music screen leaves composition. Online choices open through Android's chooser. **Open Naxi Radio** uses Naxi's radio page; the online music chooser can offer apps that handle the YouTube URL. NewPipe is not bundled or embedded; its official upstream releases page is provided separately.

Ask Suric's photo composer now displays a thumbnail, the user message retains its URI for display/retry, and a zoomable preview opens over the conversation. The existing Responses request still sends the selected image only when Send is tapped. The conversation itself is in-memory; message history and photo thumbnails do not survive process death.

The assistant does not yet use typed function tools or perform app writes. Existing manual Add/Edit actions remain local. No whole-store context is uploaded automatically; the existing selected-note actions remain user initiated.

## Unimplemented content depth

The 30 bundled articles are useful starting content, not the requested broad offline library. Gardening needs more Irish climate-aware crop and diagnosis guides; pottery, DIY, cleaning and Retro & Android need substantial topic coverage; Music has no bundled offline knowledge. There are no offline photos attached to the library articles. Shopping, bills and external firmware/radio/music information have the online-only limits described above.

No tests, lint, builds, emulator checks or GitHub Actions were run for this change, as requested.
