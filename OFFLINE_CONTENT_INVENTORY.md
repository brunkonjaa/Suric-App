# Offline content inventory

This inventory describes the bundled JSON currently in `app/src/main/assets/`. The table below counts 30 article records with stable IDs and revision 1. The structured recipe catalogue is counted separately, so recipe entries do not inflate article totals.

The bundled JSON source files and recipe manifest total **312,830 bytes**. They contain text only; no offline library or recipe photos are bundled. This is source-file size, not the final APK's compressed or installed asset size.

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
| **Total** | **30** | **19 guides and 11 Cooking recipe records** | - |

The 11 Cooking article records remain available. Three recipe catalogue data assets contain 174 canonical, metric recipe records with stable IDs, country/region, timings, servings, difficulty, ingredients, numbered directions, sides, substitutions, storage, safety notes, tags and revision. A version-1 manifest registers supplemental catalogues without changing the loader. Four catalogue records link to existing article records (including the Irish stew and boxty); each also has structured recipe fields. Recipes are searched offline by title, origin, ingredients and tags. Recipe details show origin and offer Ask Suric, Android Sharesheet, an independent encrypted copy to Cooking and a local favourite toggle. The collection picker currently contains Irish, Argentinian, French and United States of America recipes. Offline articles and recipes also support a twenty-item recently viewed list.

## Cuisine and special-collection quotas

The requested 920 collection-entry quota is not complete. The catalogue currently declares four cuisine collections with exactly 30 distinct Irish, Argentinian, French and United States of America recipe entries. There are 174 canonical recipe records; 54 are not yet assigned to a requested collection. No BBQ or Fish & Seafood entries are assigned. Existing recipe records can be referenced by multiple collections without duplicating their canonical data.

| Requested cuisine group | Entries assigned |
| --- | ---: |
| African regional cuisines | 0 / 30 |
| Argentinian | 30 / 30 |
| Bosnian | 0 / 30 |
| Brazilian | 0 / 30 |
| Bulgarian | 0 / 30 |
| Chinese | 0 / 30 |
| Croatian | 0 / 30 |
| French | 30 / 30 |
| German | 0 / 30 |
| Greek | 0 / 30 |
| Hungarian | 0 / 30 |
| Indian | 0 / 30 |
| Irish | 30 / 30 |
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
| United States of America | 30 / 30 |
| BBQ Around the World | 0 / 100 |
| Fish & Seafood Around the World | 0 / 100 |

**Assigned collection total: 120 / 920.** The 54 unassigned canonical records do not count toward the requested quotas. BBQ and seafood need their own culturally appropriate collection assignments; the other 20 cuisine groups still need 30 entries apiece.

## Recipe authorship and cultural references

United States regional selections were informed by the [Library of Congress community-cookbook guide](https://guides.loc.gov/community-cookbooks/by-place), which describes broad regional patterns and the way dishes travelled, and the [USDA regional recipes](https://www.fna.usda.gov/americasharvest/regional). The records are Suric-authored home-cooking adaptations; regional labels describe the selected style and do not claim a single definitive origin for every dish.

Recipe directions and quantities in the catalogue JSON assets are original Suric-authored adaptations, not copied publisher text. Irish dish selections and regional descriptions were cross-checked against [Tourism Ireland's traditional food guide](https://www.ireland.com/en/magazine/food-and-drink/traditional-irish-food/) and the [National Museum of Ireland's account of Samhain foods](https://source.museum.ie/en-IE/News/2023/Ghost-turnips%2C-pucas%2C-fortune-telling-and-evil-abd). Argentinian regional dish selection was checked against [Argentina's official food-by-province guide](https://www.argentina.gob.ar/jefatura/turismo-y-ambiente/turismo/viaja-por-argentina/que-comer) and [Visit Argentina's Northwest food guide](https://www.argentina.travel/en/news/flavors-of-the-north-a-gastronomic-guide-to-explore-northern-argentina). The homemade floury roll is labelled Waterford-inspired and explicitly does not claim to reproduce the protected Waterford Blaa specification listed in [Ireland's protected-food-name register](https://www.gov.ie/en/department-of-agriculture-food-and-the-marine/publications/pdopgitsg-protected-food-names/).

French regional selections were cross-checked against [Explore France's regional gastronomy guide](https://www.france.fr/en/list/gastronomy-and-wine/), [its Alsatian cuisine guide](https://www.france.fr/en/article/alsatian-cuisine/) and [its Burgundy cuisine guide](https://www.france.fr/en/article/burgundy-cuisine/). The collection spans regional dishes and common home cooking; recipes are Suric-authored adaptations and do not claim one definitive version.

## Storage and navigation

The app loads the two article JSON files, the base recipe catalogue, and the supplemental catalogues named in the version-1 manifest off the main thread. Each article has a stable ID, section, title, summary, revision, tags and full text. Recipes have separate canonical records and collection references. Cooking search checks recipe titles, origin, ingredients and tags as well as article text. All four current cuisine collections open complete recipe details with measurements, directions, sides, substitutions, storage and food-safety notes. Back returns to the containing section. `suric://library/{id}` can open a matching bundled article in an installed Suric copy. Recipe-specific deep links and a verified HTTPS App Links domain are not configured.

**Copy to section** creates an independent encrypted personal entry. It does not alter the bundled article, and later edits/deletion affect only the personal copy. Personal entries can be moved among sections in their editor; the move retains the entry ID and encrypted store, and shopping-only retailer metadata is cleared when moved out of Shopping.

Outgoing private-entry sharing requires confirmation and uses Android Sharesheet with a versioned text envelope. The receiving app shows a preview and saves only after confirmation. The export contains only the selected title, section and body; it excludes the local entry ID, credentials and other records. The text copy is readable by the chosen receiving app. Image attachments and portable binary bundles are not implemented.

## Music, photos and AI

Music can select local audio through Android's document picker, retain its granted URI, save a queue entry, play/pause, seek and remember the last position. This implementation has no playback service or notification controls, and playback stops when the Music screen leaves composition. Online choices open through Android's chooser. **Open Naxi Radio** uses Naxi's radio page; the online music chooser can offer apps that handle the YouTube URL. NewPipe is not bundled or embedded; its official upstream releases page is provided separately.

Ask Suric's photo composer now displays a thumbnail, the user message retains its URI for display/retry, and a zoomable preview opens over the conversation. The existing Responses request still sends the selected image only when Send is tapped. The conversation itself is in-memory; message history and photo thumbnails do not survive process death.

The assistant does not yet use typed function tools or perform app writes. Existing manual Add/Edit actions remain local. No whole-store context is uploaded automatically; the existing selected-note actions remain user initiated.

## Unimplemented content depth

The Irish, Argentinian, French and United States of America 30-entry cuisine quotas are complete. The remaining 20 cuisine groups, 100-entry BBQ collection and 100-entry Fish & Seafood collection have not been added, so the total is 120 of 920 collection entries. Recipe favourites are implemented; ingredient handoff to shopping is not. Gardening needs more Irish climate-aware crop and diagnosis guides; pottery, DIY, cleaning and Retro & Android need substantial topic coverage; Music has no bundled offline knowledge. There are no offline photos attached to the library articles. Shopping, bills and external firmware/radio/music information have the online-only limits described above.

No tests, lint, builds, emulator checks or GitHub Actions were run for this change, as requested.
