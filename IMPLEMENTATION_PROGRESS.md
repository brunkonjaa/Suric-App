# Suric expansion implementation progress

Updated 19 October 2026 on `feat/suric-android-app`.

This ledger tracks the complete application expansion brief in the supplied request. Counts are from the checked-in bundled assets. The current update fixes the offline recipe loader, exposes all 891 recipes by default and verifies the result on the connected phone.

## Completed in source

- Android Back handling closes the drawer and returns from a section, article or recipe to its containing screen.
- Ask Suric photo selection, thumbnail display, message attachment display, zoom preview and request attachment are implemented. Message history and draft are encrypted on-device and restored after process recreation; image URI access is persisted where Android grants it. Navigation route and selected section are saveable.
- Offline article assets and a canonical JSON recipe catalogue are loaded without a network connection. Stable recipe IDs allow multiple collections to reuse one recipe record. A dedicated global-search screen searches all offline guides and recipes by title, section, origin, body, ingredients and tags, with category filters.
- The Cooking section opens with an “All recipes” view for the complete 891-recipe canonical catalogue. The collection picker can narrow the list to each cuisine or special collection.
- The recipe loader treats nullable article links and absent collection arrays in supplemental recipe-only catalogues as optional metadata, so those records no longer prevent the complete offline library from loading.
- A separate 87-item offline Starter Library supplies guides, recipes, checklists and templates, with search, category filters, source links and copy-to-personal-space actions. The 59 Retro & Android guides now have 9 categories and 24 subcategories, Android Back returns through that hierarchy, and global offline search includes these guides.
- Eight Retro & Android Starter Library guides were added for Android architecture/development, Android TV, Shield hardware and mirroring limits, whole-home streaming, emulation, console care and authorized application analysis. Android 17/API 37 is explicitly marked beta based on official Android Developers/AOSP pages checked 19 October 2026.
- Android 1.0 through Android 17/API 37 now have 37 separate offline Starter Library version entries in addition to the version overview. Each records its API, release month/year, selected platform changes and general device/app compatibility cautions. The history is a concise map and still needs deeper per-release source and security research.
- Four PlayStation guides add PS2 OPL storage choices, a distinct PS3 CFW-versus-HEN check, a PS4 exact-firmware verification workflow, and PS5 evidence-level/research limitations. No static PS4/PS5 jailbreak compatibility matrix is asserted without a current per-version source.
- This implementation stage adds persistent, device-local favourites and a twenty-item recently viewed index for bundled articles and recipes. Section filters show all, saved or recent content. This index does not modify bundled records or encrypted personal entries.
- The United States of America cuisine collection adds 30 original, structured regional home-cooking recipes in a separate versioned asset. Each entry includes measured ingredients, numbered steps, timings, servings, difficulty, accompaniments, substitutions, storage and safety notes.
- The France cuisine collection adds 30 original, structured recipes across French regions in a separate versioned asset. A versioned asset manifest now registers supplemental catalogue files so more cuisines can be added without changing loader code.
- The Italy cuisine collection adds 30 structured, regionally labelled home recipes in a supplemental catalogue registered by the same versioned manifest.
- The Germany cuisine collection adds 30 structured regional and everyday recipes in a supplemental catalogue; all directions and ingredients are bundled offline.
- The Spain cuisine collection adds 30 regionally labelled recipes in a supplemental catalogue, including complete methods and practical storage and safety notes.
- The Mexico cuisine collection adds 30 recipes across regional home cooking and everyday dishes in its own supplemental catalogue.
- The Brazil cuisine collection adds 30 recipes across several regions in a supplemental catalogue, including complete ingredient lists, methods, accompaniments, substitutions, storage and safety notes.
- The Bosnia and Herzegovina collection adds 30 structured recipes in a supplemental catalogue, including savory pies, soups, stews, grilled dishes and sweets.
- The Bulgaria cuisine collection adds 30 recipes spanning salads, soups, baked pies, grilled food, clay-pot dishes and sweets.
- The Greece cuisine collection adds 30 regional and everyday recipes in its own supplemental catalogue; Greek transliterations, complete methods and recipe-specific storage and safety notes are included.
- The Hungary cuisine collection adds 30 Hungarian home-cooking recipes in a supplemental catalogue, covering soups, paprika stews, vegetables, breads, pancakes and desserts.
- The India cuisine collection adds 30 regional home recipes in a supplemental catalogue, with selections across North, East, West and South India.
- The Japan cuisine collection adds 30 regional and everyday recipes in a supplemental catalogue, covering hotpots, noodles, rice dishes, grilled foods and sweets.
- The African regional collection adds 30 home-cooking adaptations from 15 countries across West, East, Central, North and Southern Africa.
- The Chinese cuisine collection adds 30 regional and everyday home-cooking adaptations across China.
- The Croatian cuisine collection adds 30 regional home-cooking adaptations spanning coastal and continental Croatia.
- The Macedonian cuisine collection adds 30 North Macedonian home-cooking adaptations across regional and everyday dishes.
- The Moroccan cuisine collection adds 30 home-cooking adaptations across soups, tagines, salads, breads and sweets.
- The Nigerian cuisine collection adds 30 home-cooking recipes across all six geopolitical zones.
- The Persian cuisine collection adds 30 regional Iranian home-cooking adaptations.
- The Romanian cuisine collection adds 30 regional and everyday home-cooking adaptations.
- The Serbian cuisine collection adds 30 regional home-cooking adaptations across Vojvodina, central, western and southern Serbia.
- The BBQ Around the World collection contains 100 entries, reusing suitable canonical grill recipes and adding regional barbecue recipes with measured ingredients, complete methods and food-safety notes.
- The Fish & Seafood Around the World collection contains 100 entries, reusing suitable canonical seafood recipes and adding globally varied seafood dishes with measured ingredients, complete methods and seafood-handling notes. Ten new grilled seafood recipes are shared by both collections through canonical IDs.
- AI-generated photorealistic BBQ and Fish & Seafood collection images are bundled and shown above their recipe lists. These are collection covers, not individual images for each recipe.
- Recipe cards show each available offline thumbnail in the list, and tapping a card image opens a zoomable full-screen preview. Recipe detail screens retain an offline horizontal gallery; its images open the same preview. The local reading index accepts Unicode letters in recipe IDs so Croatian names open correctly. All 66 supplied Argentina photos are attached across Argentina recipes, and all 30 photos from the Croatia folder are attached across Croatian recipes. The supplied Argentina guiso carrero photo is an additional gallery image alongside its existing Commons image. Together with five Texas brisket images, the app now bundles 102 recipe image files across 97 of 891 canonical recipes; collection covers remain separate.
- Music, offline article and recipe detail screens now explicitly opt in to the experimental Material 3 top-app-bar API required by the current Compose Material dependency.
- Recipe images are packaged as static lossy WebP in `drawable-nodpi`, with a hard maximum of 50,000 bytes per image. All 102 current recipe images are at or below the cap, use embedded sRGB and preserve their aspect ratios; quality is 50–87 and long edges are at most 1280 px.
- `COOKING_DISH_IMAGE_INDEX.md` lists all 891 unique canonical recipes once, with stable IDs, origins, collection memberships, image search queries, serving-scene direction and explicit missing/attached status. `tools/build_recipe_image_index.py` regenerates it from the registered catalogue files. The target is one matching photograph per canonical recipe (891 total); unrelated photographs must remain missing.
- Recipe image specifications call for static lossy WebP, embedded sRGB, aspect-preserving dimensions up to 1280 px, no animation, offline files and per-image source, creator, licence and attribution metadata. Every image has a hard 50,000-byte limit; use quality up to 87, reduce to quality 50, then resize as needed. `tools/optimize_recipe_images.py` records final quality, dimensions and bytes. A licensed-web-image downloader and duplicate-detection utility are not implemented.
- The 102 current recipe images total 4,771,014 bytes (46,774 bytes average); the largest is 49,996 bytes. The Argentina folder contributed 66 photos, and the Croatia folder contributed 30; every imported image was recompressed under the same cap. Source rights for the Argentina local images were not independently checked. The brisket files originated as legacy quality-76, 960 x 720 RGB WebP without embedded ICC profiles; they have been re-encoded to embed sRGB and meet the cap. The existing guiso carrero image remains sourced from Wikimedia Commons under CC BY-SA 3.0.
- Recipe details can add each measured ingredient as a separate checkable entry to the encrypted local Shopping section. Repeating the action skips ingredients already added for that recipe.
- Ask Suric restores encrypted local message history and draft after process recreation, surfaces read failures without overwriting saved chat, shares text history through Android Sharesheet, and offers a confirmed local deletion action. Shared history excludes image files and attachment URIs.
- Guides and recipes can be copied into the encrypted personal space, and recipes can be shared as text. Bundled article sharing uses a `suric://library/{id}` link. Text imports and personal-entry exports use Android Sharesheet with a review step on receipt.
- Music can select and play local audio, retain queue references and position, and open online providers through Android's app chooser. Playback stops when leaving the Music screen.
- The 24 traditional cuisine collections are complete at 30 entries each, and BBQ Around the World and Fish & Seafood Around the World are complete at 100 entries each. Five new guides add garden disease/watering, safe wall drilling, household chemical safety and phone reuse/recycling coverage; the inventory documents 35 guides and current collection counts.

The content and feature descriptions above are source-level findings unless the verification section below records a fresh check.

## Content totals

- Complete bundled offline articles: **35** across Garden (7), Cooking (11), Cleaning (4), DIY & Home (4), Pottery (3), Retro & Android (4), Bills (1), and Shopping (1). Music and Notes have no bundled guides.
- Separate code-authored Starter Library: **87** items, including **59 Retro & Android guides** across **9 categories / 24 subcategories**. These are not included in the 35 JSON article count.
- Canonical recipe records: **891**. Collection entries assigned: **920 / 920**.
- All 24 requested traditional cuisine collections are complete at **30 / 30** each.
- BBQ Around the World: **100 / 100**.
- Fish & Seafood Around the World: **100 / 100**.
- BBQ and Fish & Seafood have generated offline collection-cover images; individual recipe images are bundled for one canonical recipe, while offline library article photographs are not bundled.
- Recipe image target: one matching photograph for each of 891 unique canonical recipes. Argentina has 66 folder photos across 66 recipes; Croatia has 30 folder photos attached across 30 recipes. Five legacy brisket images and one additional guiso carrero image bring the total to 102 image files across 97 recipes. The remaining 794 recipes have no accepted image.
- A licensed web-image search/downloader and image-preparation utility are not implemented yet. New-source license checks, automatic resizing and duplicate detection remain required before images are accepted.

## Partially implemented

- Offline global search is local and filters articles and recipes across all categories. Results are loaded from bundled assets in memory; there is no indexed database for larger content volumes.
- Cooking has detailed structured recipe records, alphabetical collection sorting, origin labels, recipe sharing, personal copies and a local ingredient-to-Shopping handoff. Fifty-three canonical recipes are not assigned to one of the requested collections; all requested collection quotas are complete.
- Garden, Pottery, DIY & Home, Cleaning, and Retro & Android remain far short of the requested comprehensive coverage. Retro now has an initial navigable taxonomy and 59 guides, but lacks deep technical topic trees, comprehensive compatibility matrices, category-specific images, detailed per-version architecture/security research and integrated favorites/recent tracking. Personal Garden records are plain saved entries rather than a dedicated plant journal with photo records and calendars.
- Local music playback works only while its screen remains composed; there is no background playback service or notification control. NewPipe is not embedded or bundled; provider links rely on installed applications and network access.
- Family transfer supports text exports and same-installation article IDs. There is no portable attachment bundle, HTTPS App Link domain, or verified cross-installation/deep-link back-stack restoration.
- Shopping, bills, notes, projects and My Day retain the existing personal-entry flows; several requested richer domain-specific features are still absent.

## Not implemented

- The 859 remaining matching recipe photographs needed to cover all 891 canonical recipes, plus offline library article photography. The dish index and per-image metadata schema are in place; the BBQ and seafood collection covers remain separate from recipe images.
- Comprehensive offline subject libraries for Gardening, Retro & Android, Pottery, DIY & Home and Cleaning, including their requested calendars, troubleshooting depth and photographs.
- Application-wide Buster context, typed structured AI tools, confirmed local create/edit/move/copy actions, and AI-driven cross-section search/navigation. Existing AI prompts do not execute local writes.
- Image attachments still depend on Android retaining the selected URI permission; chat export is text-only and does not package image attachments.
- Bundled NewPipe integration and background local-audio playback.
- Direct family sharing of personal content as a portable bundle with attachments and complete incoming navigation restoration.

## Verification and repository state

- On 18 October 2026, all 66 Argentina folder photos and 30 Croatia folder photos were bundled as WebP under 50,000 bytes each. Final `assembleDebug` passed with cached Gradle 8.13, JDK 17 and the installed SDK. The updated debug APK installed with `adb install -r` on the Samsung SM-A127F, preserving app data. On-device checks showed 891 recipes, visible list thumbnails, a working full-screen image preview, a Croatian recipe detail/photo opening successfully, and two guiso carrero photos. No unit tests or lint were run for this image/UI update.
- The initial 30-photo Croatian batch was converted, bundled and linked in the catalogue on 18 October 2026. Its first handoff preceded build and phone verification; the later image/UI verification is recorded below.

- Local `testDebugUnitTest`, `lintDebug` and `assembleDebug` passed on 18 October 2026 using cached Gradle 8.13, JDK 17 and the installed Android SDK. On the connected Samsung SM-A127F, the installed debug APK opened Cooking and displayed **891 recipes** in “All recipes”; no emulator was used.
- The pre-existing uncommitted `gradle/wrapper/gradle-wrapper.properties` edit selects Gradle 9.7.1, which is incompatible with this project's Android Gradle Plugin 8.13. The passing local check used cached Gradle 8.13, matching the committed wrapper value; the user edit remains untouched and excluded from commits.
- PR #15 made “All recipes” the Cooking default. The recipe-loader repair and on-device confirmation are recorded in the current update's pull request.
- The selected checkout is the GitHub-linked `Suric-App-github` clone on `feat/suric-android-app`. The branch contains earlier local commits as well as the implementation slices recorded here; consult Git status for the current ahead count.
- `gradle/wrapper/gradle-wrapper.properties` had a pre-existing uncommitted edit and was left untouched.
- All 920 requested recipe collection entries are now present. This does not complete the broader application expansion described in the supplied brief.
