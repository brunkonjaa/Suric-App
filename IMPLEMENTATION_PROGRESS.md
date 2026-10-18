# Suric expansion implementation progress

Updated 18 October 2026 on `feat/suric-android-app`.

This ledger tracks the complete application expansion brief in the supplied request. Counts are from the checked-in bundled assets. The request forbids tests, lint, builds, emulators and GitHub Actions at this stage.

## Completed in source

- Android Back handling closes the drawer and returns from a section, article or recipe to its containing screen.
- Ask Suric photo selection, thumbnail display, message attachment display, zoom preview and request attachment are implemented. Message history and draft are encrypted on-device and restored after process recreation; image URI access is persisted where Android grants it. Navigation route and selected section are saveable.
- Offline article assets and a canonical JSON recipe catalogue are loaded without a network connection. Stable recipe IDs allow multiple collections to reuse one recipe record. A dedicated global-search screen searches all offline guides and recipes by title, section, origin, body, ingredients and tags, with category filters.
- This implementation stage adds persistent, device-local favourites and a twenty-item recently viewed index for bundled articles and recipes. Section filters show all, saved or recent content. This index does not modify bundled records or encrypted personal entries.
- The United States of America cuisine collection adds 30 original, structured regional home-cooking recipes in a separate versioned asset. Each entry includes measured ingredients, numbered steps, timings, servings, difficulty, accompaniments, substitutions, storage and safety notes.
- The France cuisine collection adds 30 original, structured recipes across French regions in a separate versioned asset. A versioned asset manifest now registers supplemental catalogue files so more cuisines can be added without changing loader code.
- The Italy cuisine collection adds 30 structured, regionally labelled home recipes in a supplemental catalogue registered by the same versioned manifest.
- The Germany cuisine collection adds 30 structured regional and everyday recipes in a supplemental catalogue; all directions and ingredients are bundled offline.
- The Spain cuisine collection adds 30 regionally labelled recipes in a supplemental catalogue, including complete methods and practical storage and safety notes.
- The Mexico cuisine collection adds 30 recipes across regional home cooking and everyday dishes in its own supplemental catalogue.
- The Brazil cuisine collection adds 30 recipes across several regions in a supplemental catalogue, including complete ingredient lists, methods, accompaniments, substitutions, storage and safety notes.
- The Bosnia and Herzegovina collection adds 30 structured recipes in a supplemental catalogue, including savory pies, soups, stews, grilled dishes and sweets.
- Recipe details can add each measured ingredient as a separate checkable entry to the encrypted local Shopping section. Repeating the action skips ingredients already added for that recipe.
- Ask Suric restores encrypted local message history and draft after process recreation, surfaces read failures without overwriting saved chat, shares text history through Android Sharesheet, and offers a confirmed local deletion action. Shared history excludes image files and attachment URIs.
- Guides and recipes can be copied into the encrypted personal space, and recipes can be shared as text. Bundled article sharing uses a `suric://library/{id}` link. Text imports and personal-entry exports use Android Sharesheet with a review step on receipt.
- Music can select and play local audio, retain queue references and position, and open online providers through Android's app chooser. Playback stops when leaving the Music screen.
- The 30 Irish, 30 Argentinian, 30 Bosnian, 30 Brazilian, 30 French, 30 German, 30 Italian, 30 Mexican, 30 Spanish and 30 United States of America recipe collections are complete. Five new guides add garden disease/watering, safe wall drilling, household chemical safety and phone reuse/recycling coverage; the inventory documents 35 guides and current collection counts.

These are source-level findings; this stage did not execute or independently verify the application.

## Content totals

- Complete bundled offline articles: **35** across Garden (7), Cooking (11), Cleaning (4), DIY & Home (4), Pottery (3), Retro & Android (4), Bills (1), and Shopping (1). Music and Notes have no bundled guides.
- Canonical recipe records: **354**. Collection entries assigned: **300 / 920**.
- Traditional cuisine collections: Ireland **30 / 30**; Argentina **30 / 30**; Bosnia and Herzegovina **30 / 30**; Brazil **30 / 30**; France **30 / 30**; Germany **30 / 30**; Italy **30 / 30**; Mexico **30 / 30**; Spain **30 / 30**; United States of America **30 / 30**; each of the remaining 14 requested cuisine groups **0 / 30**.
- BBQ Around the World: **0 / 100**.
- Fish & Seafood Around the World: **0 / 100**.
- No recipe photos or offline library photographs are bundled with the text assets.

## Partially implemented

- Offline global search is local and filters articles and recipes across all categories. Results are loaded from bundled assets in memory; there is no indexed database for larger content volumes.
- Cooking has detailed structured recipe records, alphabetical collection sorting, origin labels, recipe sharing, personal copies and a local ingredient-to-Shopping handoff. The remaining 420 traditional cuisine entries, plus all 200 BBQ and seafood memberships, are not supplied.
- Garden, Pottery, DIY & Home, Cleaning, and Retro & Android have a small number of bundled guides, far short of the requested comprehensive coverage. Personal Garden records are plain saved entries rather than a dedicated plant journal with photo records and calendars.
- Local music playback works only while its screen remains composed; there is no background playback service or notification control. NewPipe is not embedded or bundled; provider links rely on installed applications and network access.
- Family transfer supports text exports and same-installation article IDs. There is no portable attachment bundle, HTTPS App Link domain, or verified cross-installation/deep-link back-stack restoration.
- Shopping, bills, notes, projects and My Day retain the existing personal-entry flows; several requested richer domain-specific features are still absent.

## Not implemented

- The remaining 14 traditional cuisine collections, all 100 BBQ entries and all 100 Fish & Seafood entries; and recipe image coverage where suitable offline assets are available.
- Comprehensive offline subject libraries for Gardening, Retro & Android, Pottery, DIY & Home and Cleaning, including their requested calendars, troubleshooting depth and photographs.
- Application-wide Buster context, typed structured AI tools, confirmed local create/edit/move/copy actions, and AI-driven cross-section search/navigation. Existing AI prompts do not execute local writes.
- Image attachments still depend on Android retaining the selected URI permission; chat export is text-only and does not package image attachments.
- Bundled NewPipe integration and background local-audio playback.
- Direct family sharing of personal content as a portable bundle with attachments and complete incoming navigation restoration.

## Verification and repository state

- Tests, lint, builds, emulator/device checks and GitHub Actions were not run, as instructed.
- The selected checkout is the GitHub-linked `Suric-App-github` clone on `feat/suric-android-app`. The branch contains earlier local commits as well as the implementation slices recorded here; consult Git status for the current ahead count.
- `gradle/wrapper/gradle-wrapper.properties` had a pre-existing uncommitted edit and was left untouched.
- This stage must not be described as completing the full 920-entry application expansion.
