# Suric expansion implementation progress

Updated 18 October 2026 on `feat/suric-android-app`.

This ledger tracks the complete application expansion brief in the supplied request. Counts are from the checked-in bundled assets. The request forbids tests, lint, builds, emulators and GitHub Actions at this stage.

## Completed in source

- Android Back handling closes the drawer and returns from a section, article or recipe to its containing screen.
- Ask Suric photo selection, thumbnail display, message attachment display, zoom preview and request attachment are implemented. Conversation state is in memory; it does not survive process death.
- Offline article assets and a canonical JSON recipe catalogue are loaded without a network connection. Stable recipe IDs allow multiple collections to reuse one recipe record. Search is available within section screens; Cooking also searches recipe title, origin, ingredients and tags.
- This implementation stage adds persistent, device-local favourites and a twenty-item recently viewed index for bundled articles and recipes. Section filters show all, saved or recent content. This index does not modify bundled records or encrypted personal entries.
- The United States of America cuisine collection adds 30 original, structured regional home-cooking recipes in a separate versioned asset. Each entry includes measured ingredients, numbered steps, timings, servings, difficulty, accompaniments, substitutions, storage and safety notes.
- The France cuisine collection adds 30 original, structured recipes across French regions in a separate versioned asset. A versioned asset manifest now registers supplemental catalogue files so more cuisines can be added without changing loader code.
- Guides and recipes can be copied into the encrypted personal space, and recipes can be shared as text. Bundled article sharing uses a `suric://library/{id}` link. Text imports and personal-entry exports use Android Sharesheet with a review step on receipt.
- Music can select and play local audio, retain queue references and position, and open online providers through Android's app chooser. Playback stops when leaving the Music screen.
- The 30 Irish, 30 Argentinian, 30 French and 30 United States of America recipe collections are complete. The current inventory documents 30 offline guides and all current collection counts.

These are source-level findings; this stage did not execute or independently verify the application.

## Content totals

- Complete bundled offline articles: **30** across Garden (5), Cooking (11), Cleaning (3), DIY & Home (3), Pottery (3), Retro & Android (3), Bills (1), and Shopping (1). Music and Notes have no bundled guides.
- Canonical recipe records: **174**. Collection entries assigned: **120 / 920**.
- Traditional cuisine collections: Ireland **30 / 30**; Argentina **30 / 30**; France **30 / 30**; United States of America **30 / 30**; each of the remaining 20 requested cuisine groups **0 / 30**.
- BBQ Around the World: **0 / 100**.
- Fish & Seafood Around the World: **0 / 100**.
- No recipe photos or offline library photographs are bundled with the text assets.

## Partially implemented

- Offline search is local and spans all article sections when entered from a section screen. There is no single application-wide search entry point, category index across the whole library, or indexed database for larger content volumes.
- Cooking has detailed structured recipe records, alphabetical collection sorting, origin labels, recipe sharing and personal copies. The remaining 600 traditional cuisine entries, plus all 200 BBQ and seafood memberships, are not supplied. Recipe favourites exist; add-ingredients-to-shopping-list handoff is not implemented.
- Garden, Pottery, DIY & Home, Cleaning, and Retro & Android have a small number of bundled guides, far short of the requested comprehensive coverage. Personal Garden records are plain saved entries rather than a dedicated plant journal with photo records and calendars.
- Local music playback works only while its screen remains composed; there is no background playback service or notification control. NewPipe is not embedded or bundled; provider links rely on installed applications and network access.
- Family transfer supports text exports and same-installation article IDs. There is no portable attachment bundle, HTTPS App Link domain, or verified cross-installation/deep-link back-stack restoration.
- Shopping, bills, notes, projects and My Day retain the existing personal-entry flows; several requested richer domain-specific features are still absent.

## Not implemented

- The remaining 20 traditional cuisine collections, all 100 BBQ entries and all 100 Fish & Seafood entries; recipe-to-shopping-list handoff; and required recipe image coverage.
- Comprehensive offline subject libraries for Gardening, Retro & Android, Pottery, DIY & Home and Cleaning, including their requested calendars, troubleshooting depth and photographs.
- Application-wide Buster context, typed structured AI tools, confirmed local create/edit/move/copy actions, and AI-driven cross-section search/navigation. Existing AI prompts do not execute local writes.
- Persistent conversation history and restored screen history after process death.
- Bundled NewPipe integration and background local-audio playback.
- Direct family sharing of personal content as a portable bundle with attachments and complete incoming navigation restoration.

## Verification and repository state

- Tests, lint, builds, emulator/device checks and GitHub Actions were not run, as instructed.
- The selected checkout is the GitHub-linked `Suric-App-github` clone on `feat/suric-android-app`. The branch was four commits ahead when this ledger was first created; the three local implementation commits recorded here bring it to seven commits ahead of `origin/feat/suric-android-app`.
- `gradle/wrapper/gradle-wrapper.properties` had a pre-existing uncommitted edit and was left untouched.
- This stage must not be described as completing the full 920-entry application expansion.
