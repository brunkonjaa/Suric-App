# Offline content inventory

This inventory describes the bundled JSON currently in `app/src/main/assets/`. The table below counts 35 article records with stable IDs and revision 1. The structured recipe catalogue is counted separately, so recipe entries do not inflate article totals.

The bundled JSON source files and recipe manifest total **446,131 bytes**. They contain text only; no offline library or recipe photos are bundled. This is source-file size, not the final APK's compressed or installed asset size.

## Complete bundled articles by section

| Section | Complete articles | Examples | Online-only functions |
| --- | ---: | --- | --- |
| Garden | 7 | Tomato growth diagnosis, hardening off seedlings, compost, container potatoes, potted herbs, blight response, container watering | Current weather and local frost conditions |
| Cooking | 11 | Irish stew, Sekeli-style pork and sauerkraut goulash, tomato and lentil soup, boxty, chana masala, spanakorizo | None for the bundled recipes |
| Cleaning | 4 | Bathroom routine, laundry stains, kitchen touch points, cleaner labels and chemical safety | None for the bundled guides |
| DIY & Home | 4 | Wall fixings, measuring a straight cut, refreshing a small sealant joint, safe drilling plan | Current product availability and manufacturer-specific instructions |
| Pottery | 3 | Wedging, pinch cup, even drying | Clay, glaze and kiln maker schedules must be checked with the specific product |
| Retro & Android | 4 | Console compatibility checks, emulator save backups, Android build details, phone reuse and recycling | Current firmware and exploit compatibility |
| Bills | 1 | Monthly bill review checklist | Provider balances, live account details and payments |
| Shopping | 1 | Online option comparison checklist | Live price, stock, delivery and retailer terms |
| Music | 0 | Personal local audio queue is user-supplied, not bundled knowledge | Online services and radio |
| Notes | 0 | Private user-created notes | None |
| **Total** | **35** | **24 guides and 11 Cooking recipe records** | - |

The 11 Cooking article records remain available. Five recipe catalogue data assets contain 234 canonical, metric recipe records with stable IDs, country/region, timings, servings, difficulty, ingredients, numbered directions, sides, substitutions, storage, safety notes, tags and revision. A version-1 manifest registers supplemental catalogues without changing the loader. Four catalogue records link to existing article records (including the Irish stew and boxty); each also has structured recipe fields. A global offline-search screen filters all bundled guides and recipes by content and category. Recipe details offer Ask Suric, Android Sharesheet, an independent encrypted copy to Cooking, a local favourite toggle and an encrypted Shopping handoff that creates separately checkable ingredient rows and skips exact repeat additions for the same recipe. The collection picker currently contains Irish, Argentinian, French, German, Italian and United States of America recipes. Offline articles and recipes also support a twenty-item recently viewed list.

## Cuisine and special-collection quotas

The requested 920 collection-entry quota is not complete. The catalogue currently declares six cuisine collections with exactly 30 distinct Irish, Argentinian, French, German, Italian and United States of America recipe entries. There are 234 canonical recipe records; 54 are not yet assigned to a requested collection. No BBQ or Fish & Seafood entries are assigned. Existing recipe records can be referenced by multiple collections without duplicating their canonical data.

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
| German | 30 / 30 |
| Greek | 0 / 30 |
| Hungarian | 0 / 30 |
| Indian | 0 / 30 |
| Irish | 30 / 30 |
| Italian | 30 / 30 |
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

**Assigned collection total: 180 / 920.** The 54 unassigned canonical records do not count toward the requested quotas. BBQ and seafood need their own culturally appropriate collection assignments; the other 18 cuisine groups still need 30 entries apiece.

## Recipe authorship and cultural references

United States regional selections were informed by the [Library of Congress community-cookbook guide](https://guides.loc.gov/community-cookbooks/by-place), which describes broad regional patterns and the way dishes travelled, and the [USDA regional recipes](https://www.fna.usda.gov/americasharvest/regional). The records are Suric-authored home-cooking adaptations; regional labels describe the selected style and do not claim a single definitive origin for every dish.

Recipe directions and quantities in the catalogue JSON assets are original Suric-authored adaptations, not copied publisher text. Irish dish selections and regional descriptions were cross-checked against [Tourism Ireland's traditional food guide](https://www.ireland.com/en/magazine/food-and-drink/traditional-irish-food/) and the [National Museum of Ireland's account of Samhain foods](https://source.museum.ie/en-IE/News/2023/Ghost-turnips%2C-pucas%2C-fortune-telling-and-evil-abd). Argentinian regional dish selection was checked against [Argentina's official food-by-province guide](https://www.argentina.gob.ar/jefatura/turismo-y-ambiente/turismo/viaja-por-argentina/que-comer) and [Visit Argentina's Northwest food guide](https://www.argentina.travel/en/news/flavors-of-the-north-a-gastronomic-guide-to-explore-northern-argentina). The homemade floury roll is labelled Waterford-inspired and explicitly does not claim to reproduce the protected Waterford Blaa specification listed in [Ireland's protected-food-name register](https://www.gov.ie/en/department-of-agriculture-food-and-the-marine/publications/pdopgitsg-protected-food-names/).

German regional selection was guided by [Germany Travel's culinary guide](https://www.germany.travel/en/experience-enjoy/german-cuisine.html) and its [regional food overview](https://www.deutschland.de/en/topic/life/culinary-regions-germany). Recipes are Suric-authored home adaptations rather than transcriptions of publisher recipes.

French regional selections were cross-checked against [Explore France's regional gastronomy guide](https://www.france.fr/en/list/gastronomy-and-wine/), [its Alsatian cuisine guide](https://www.france.fr/en/article/alsatian-cuisine/) and [its Burgundy cuisine guide](https://www.france.fr/en/article/burgundy-cuisine/). The collection spans regional dishes and common home cooking; recipes are Suric-authored adaptations and do not claim one definitive version.

Italian regional selections were cross-checked against [Italia.it's food and wine guide](https://www.italia.it/en/italy/things-to-do/food-and-wine), its overview of [Italian cuisine's regional heritage](https://www.italia.it/en/italy/things-to-do/italian-cuisine-unesco-heritage), and its [Lazio](https://www.italia.it/en/lazio/things-to-do/typical-food-and-dishes-in-lazio-italy) and [Sicily](https://www.italia.it/en/sicily/things-to-do/typical-sicilian-products-dishes) food guides. Recipes are Suric-authored home adaptations; seafood and raw-egg handling have recipe-specific safety notes.

The added potato/tomato blight and container-watering guides draw on [RHS blight advice](https://www.rhs.org.uk/disease/tomato-blight) and [RHS watering guidance](https://www.rhs.org.uk/vegetables/watering). Wall-drilling safety is cross-checked against [Electrical Safety First's DIY guidance](https://www.electricalsafetyfirst.org.uk/safety-advice/home-and-people/diy-ers/). Cleaner handling is cross-checked against [UK HSE cleaning-product guidance](https://www.hse.gov.uk/chemical-classification/labelling-packaging/cleaning-products-and-corrosive-pictogram.htm), with the product label controlling use. Phone recycling guidance references [WEEE Ireland's collection information](https://www.weeeireland.ie/household-recycling/where-can-i-recycle/).

## Storage and navigation

The app loads the two article JSON files, the base recipe catalogue, and the supplemental catalogues named in the version-1 manifest off the main thread. Each article has a stable ID, section, title, summary, revision, tags and full text. Recipes have separate canonical records and collection references. The global offline-search screen checks article text and recipe titles, origin, ingredients and tags and can filter by section or recipes. All six current cuisine collections open complete recipe details with measurements, directions, sides, substitutions, storage and food-safety notes. Back returns to the containing section. `suric://library/{id}` can open a matching bundled article in an installed Suric copy. Recipe-specific deep links and a verified HTTPS App Links domain are not configured.

**Copy to section** creates an independent encrypted personal entry. It does not alter the bundled article, and later edits/deletion affect only the personal copy. Personal entries can be moved among sections in their editor; the move retains the entry ID and encrypted store, and shopping-only retailer metadata is cleared when moved out of Shopping.

Outgoing private-entry sharing requires confirmation and uses Android Sharesheet with a versioned text envelope. The receiving app shows a preview and saves only after confirmation. The export contains only the selected title, section and body; it excludes the local entry ID, credentials and other records. The text copy is readable by the chosen receiving app. Image attachments and portable binary bundles are not implemented.

## Music, photos and AI

Music can select local audio through Android's document picker, retain its granted URI, save a queue entry, play/pause, seek and remember the last position. This implementation has no playback service or notification controls, and playback stops when the Music screen leaves composition. Online choices open through Android's chooser. **Open Naxi Radio** uses Naxi's radio page; the online music chooser can offer apps that handle the YouTube URL. NewPipe is not bundled or embedded; its official upstream releases page is provided separately.

Ask Suric's photo composer displays a thumbnail, the user message retains its URI for display/retry, and a zoomable preview opens over the conversation. The existing Responses request still sends the selected image only when Send is tapped. Message history and draft are saved in app-private AES-GCM encrypted preferences and restored after process recreation; the store surfaces read errors without replacing unreadable saved content. A confirmed Delete history action removes the saved messages and draft from this device. Image display after recreation depends on Android retaining access to the selected Photo Picker URI. Chat-history export is not implemented.

Chat history can be shared as plain text through Android Sharesheet and deleted from this device after confirmation. Export excludes image files and their local URI references. The assistant does not yet use typed function tools or perform app writes. Existing manual Add/Edit actions remain local. No whole-store context is uploaded automatically; the existing selected-note actions remain user initiated.

## Unimplemented content depth

The Irish, Argentinian, French, German, Italian and United States of America 30-entry cuisine quotas are complete. The remaining 18 cuisine groups, 100-entry BBQ collection and 100-entry Fish & Seafood collection have not been added, so the total is 180 of 920 collection entries. Gardening needs more Irish climate-aware crop and diagnosis guides; pottery, DIY, cleaning and Retro & Android need substantial topic coverage; Music has no bundled offline knowledge. There are no offline photos attached to the library articles. Shopping, bills and external firmware/radio/music information have the online-only limits described above.

No tests, lint, builds, emulator checks or GitHub Actions were run for this change, as requested.
