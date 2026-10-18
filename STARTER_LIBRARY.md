# Suric Starter Library

Suric's Starter Library is immutable reference material compiled into the APK. It works offline and is not copied into personal records at installation. **Copy to my space** makes a separate, editable entry in the existing encrypted personal-entry store. Library favourites and recently viewed items remain in their separate local store.

Articles are in `StarterLibrary.kt` and `StarterLibraryExpansion.kt`. Stable IDs must be preserved when meaning is unchanged. Increment the revision when editing an existing item's meaning. A later catalogue update never rewrites a personal copy.

## Current inventory

| Category | Count | Coverage |
| --- | ---: | --- |
| Gardening | 11 | Irish vegetable planning, seasonal care, pots, compost, herbs, plant checks, pests and pruning |
| Pottery | 8 | Clay bodies, handbuilding, drying, glaze testing, kiln safety and project/firing records |
| Shopping | 7 | Research and comparison templates, specifications, Android devices, tools, appliances and storage |
| DIY & Home | 9 | Project records, toolkit, wall fixings, painting, surface preparation, maintenance and safe trade boundaries |
| Cooking | 9 | Six foundation recipes plus sarma, paprika pork stew and bread dumplings |
| Cleaning | 9 | Kitchen/bathroom/whole-home checklists, appliances, laundry, stains, surface compatibility and moisture awareness |
| Retro & Android | 16 | PS1, PS2, PS3, PS4, PS5 research, homebrew source checking, Android fundamentals, bootloaders, root, recovery and app sources |
| Bills | 2 | Existing monthly and irregular bill templates |
| **Total** | **71** | **69 reference articles/templates across the seven requested subjects, plus two existing bill templates** |

## Library behaviour

- Search and category filters include the full offline catalogue.
- Users can favourite items, see recently viewed items, and save their own HTTPS links in My Library.
- Article detail pages show the original explanation, reviewed date and source links. Source links open only after a tap.
- Related guides link to other offline articles. Related items are navigable from the article itself.
- Garden, DIY & Home, Pottery, Shopping, Cooking, Cleaning and Retro & Android items copy into the matching existing personal section. These copies use current encrypted personal-entry storage.
- Bundled section photography is reused. No image download or network request is required to read the catalogue.

## Source and currency notes

- Gardening references include Teagasc guidance for Irish vegetable growing and pest/disease context. Sowing and frost dates are not asserted as universal; check the crop instructions and local conditions.
- Technical references prioritise project documentation, AOSP/Android documentation, Sony support pages and maintained specialist wikis. ConsoleMods and video/community resources are identified as community references, not Sony firmware documentation.
- PS4 and PS5 guides intentionally contain no hard-coded supported firmware table. Their compatibility changes and must be checked against current project documentation on the day of use. The link directory's URLs and project identities were checked on 18 October 2026; this does not verify live compatibility.
- Android bootloader, Verified Boot, Magisk, LineageOS and F-Droid entries link to their project or primary documentation. Device instructions are model-specific; unlocking normally erases user data and weakens some security guarantees.
- Cooking articles are original household recipes. Follow current FSAI advice and appliance/manufacturer instructions; the chicken foundation recipe has its own measured-temperature guidance.
- Cleaning guidance is label-led and avoids product mixing. The HSE safe-use sheet cited in existing material is workplace-focused; it supports conservative chemical handling but is not represented as a household cleaning standard.
- DIY articles describe basic planning and safe observation. They do not teach gas, live electrical, structural or specialist repairs. Irish contractor registration links are provided where relevant.
- Pottery guides do not prescribe universal firing temperatures or glaze recipes. Follow the exact clay, glaze, kiln and studio documentation. Kiln operation, silica dust and glaze handling have safety guidance.
- Shopping guides are decision frameworks rather than product rankings. Suric does not fetch current prices, availability, product ratings or retailer pages.

## Coverage gaps

This is a useful offline foundation, not an exhaustive encyclopedia. It does not yet contain a plant-by-plant catalogue, month-by-month Irish sowing database, comprehensive pottery artist directory, broad recipe collection, detailed room-by-room cleaning catalogue, or a model-by-model console/Android compatibility database. No prices, current stock, live firmware status or specific device support are stored as facts. Those areas need curated, dated expansions before being described as complete.
