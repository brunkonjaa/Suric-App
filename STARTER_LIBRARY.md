# Suric Starter Library

The Starter Library is immutable reference material compiled into the APK. It is available without connectivity and is not inserted into personal records on installation. A user must select **Copy to my space** to create a separate, editable entry in the existing encrypted personal-entry store.

Items use revision `1`; the general collection shows `17 October 2026` and the Retro & Android collection shows `19 October 2026` as the review date. Library content is held in `StarterLibrary.kt`; IDs are stable across releases. Catalogue changes must preserve existing IDs where meaning is unchanged, increment the revision for edited content, and must never rewrite personal copies.

## Inventory

| Category | Stable IDs | Count |
| --- | --- | ---: |
| Gardening | `garden.spring-start`, `garden.summer-care`, `garden.autumn-care`, `garden.winter-protection`, `garden.herbs-in-pots`, `garden.plant-inspection` | 6 |
| Cooking | `cook.sekeli-goulash`, `cook.tomato-lentil-soup`, `cook.chicken-traybake`, `cook.pasta-veg`, `cook.soda-bread`, `cook.apple-crumble` | 6 |
| Cleaning | `clean.daily-kitchen`, `clean.weekly-bathroom`, `clean.weekly-home`, `clean.appliance-monthly`, `clean.deep-clean` | 5 |
| DIY & Home | `diy.project`, `diy.tools`, `diy.measurements`, `diy.maintenance` | 4 |
| Pottery | `pottery.project`, `pottery.glaze-test`, `pottery.kiln-log` | 3 |
| Shopping | `shopping.research`, `shopping.comparison` | 2 |
| Bills | `bills.monthly`, `bills.irregular` | 2 |
| Retro & Android | `retro.start-here`, `retro.android-version-map`, `retro.android-root-roms`, `retro.android-architecture`, `retro.android-tv`, `retro.shield-overview`, `retro.shield-mirroring`, `retro.whole-home-streaming`, `retro.emulation`, `retro.console-care`, `retro.reverse-engineering`, `retro.ps1-homebrew`, `retro.ps2-homebrew`, `retro.ps3-homebrew`, `retro.ps4-homebrew`, `retro.ps5-homebrew`, `retro.homebrew-catalogues`, `retro.modded-warfare` | 18 |
| **Total** |  | **46** |

## Retro & Android navigation

`RetroLibraryNavigation.kt` groups the 18 stable guide IDs into 9 categories and 24 subcategories. PlayStation has distinct PS1–PS5 entries. Android versions, architecture/development and device modification are separated. Android TV, NVIDIA Shield, emulation, homebrew, reverse engineering, streaming/networking and tools each have populated topic cards. Selecting a topic opens its existing Starter Library article; global offline search also searches these guides. The category cards currently reuse Suric's single general Retro image, so category-specific licensed photography and diagrams remain outstanding.

## Source and safety notes

- Gardening guides are practical summaries informed by Teagasc's [Introduction to Vegetable Growing](https://teagasc.ie/crops/horticulture/vegetables/introduction-a-guide-to-vegetable-growing/) and [Pest and Disease guidance](https://teagasc.ie/crops/horticulture/nursery-stock/pest-and-disease/). They avoid local forecast or fixed-frost-date claims and direct users to plant-specific instructions.
- Cleaning checklists use conservative product-label and chemical-separation advice. The cited [HSE safe-use sheet](https://www.hse.gov.uk/pubns/cais22.pdf) is written for hospitality workplaces; it supports the warning not to mix bleach products with acids or ammonia and is not presented as a household-cleaning standard.
- Cooking content is household recipe guidance. The chicken recipe states an internal temperature of at least 75°C and general leftover handling consistent with the [Food Safety Authority of Ireland cooking and reheating guidance](https://www.fsai.ie/consumer-advice/food-safety-and-hygiene/cooking-and-reheating). Follow package cooking instructions and current FSAI advice when they differ.
- Sekeli goulash is labelled a household version because recipes vary. It is served with fresh bread and potato dumplings as requested.
- DIY templates are blank planning fields. They do not instruct the user to perform gas, electrical or structural work.
- Pottery templates record project, glaze and kiln details. They do not prescribe firing temperatures; follow the clay, glaze and kiln manufacturer's instructions and the studio's approved firing process.
- Shopping items are blank user research templates. They do not claim live prices, stock or retailer verification and continue to use the existing HTTPS validation when saved in the Shopping section.
- Bill templates are blank and do not request calendar permission when opened.
- Retro & Android guides cover Android versions/API levels 1–37, device-first Android root/custom-ROM research, Android system architecture and app inspection, PS1–PS5 homebrew starting points, Android TV/NVIDIA Shield, emulation and home-network streaming. Firmware support changes; guides direct users to upstream project releases rather than claiming a universal jailbreak. Android 17/API 37 remains beta in the official documentation checked on 19 October 2026.
- Android modification guidance links the Android Open Source Project bootloader documentation, LineageOS device-specific installation pages and Magisk's upstream project. Console cards link to the relevant project repositories or their own documentation. The PS1 card calls out FreePSXBoot's full-memory-card overwrite warning.
- The PKG-Zone reference is identified as a homebrew catalogue, not a source for commercial game downloads. The cards direct users to homebrew and software they are authorised to use.
- The PS3HEN 3.6.0 README lists CEX OFW 4.80–4.82, CEX HFW 4.83.1–4.93.1, DEX OFW 4.82 and DEX HFW 4.84.2. This describes HEN support, not CFW eligibility. Recheck the current project README before acting.

## Offline imagery

Where available, the library reuses Suric's existing optimized WebP section photographs. Retro & Android cards use the existing `suric_retro` image. No image is downloaded at runtime, no new image dependency is required, and the detail view remains usable if no image key is assigned.
