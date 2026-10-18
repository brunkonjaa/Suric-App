# Offline content inventory

This inventory describes the bundled JSON currently in `app/src/main/assets/`. The table below counts 35 article records with stable IDs and revision 1. The structured recipe catalogue is counted separately, so recipe entries do not inflate article totals.

The 28 bundled JSON files total **1,826,155 bytes**. Two AI-generated photorealistic WebP collection images add **471,028 bytes** for BBQ and Fish & Seafood; one recipe-specific WebP image adds **187,480 bytes**. These are source-file sizes, not the final APK's compressed or installed asset size.

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

The 11 Cooking article records remain available. Twenty-five recipe catalogue data assets contain 891 canonical, metric recipe records with stable IDs, country/region, timings, servings, difficulty, ingredients, numbered directions, sides, substitutions, storage, safety notes, tags and revision. A version-1 manifest registers supplemental catalogues without changing the loader. Four catalogue records link to existing article records (including the Irish stew and boxty); each also has structured recipe fields. A global offline-search screen filters all bundled guides and recipes by content and category. Recipe details offer Ask Suric, Android Sharesheet, an independent encrypted copy to Cooking, a local favourite toggle and an encrypted Shopping handoff that creates separately checkable ingredient rows and skips exact repeat additions for the same recipe. The collection picker contains 24 traditional cuisine collections, BBQ Around the World and Fish & Seafood Around the World. Offline articles and recipes also support a twenty-item recently viewed list.

## Cuisine and special-collection quotas

The requested 920 collection-entry quota is complete. The catalogue declares twenty-four traditional cuisine collections with exactly 30 entries each and two special collections with 100 entries each. There are 891 canonical recipe records; 53 are not assigned to a requested collection. Existing recipe records can be referenced by multiple collections without duplicating their canonical data.

| Requested cuisine group | Entries assigned |
| --- | ---: |
| African regional cuisines | 30 / 30 |
| Argentinian | 30 / 30 |
| Bosnian | 30 / 30 |
| Brazilian | 30 / 30 |
| Bulgarian | 30 / 30 |
| Chinese | 30 / 30 |
| Croatian | 30 / 30 |
| French | 30 / 30 |
| German | 30 / 30 |
| Greek | 30 / 30 |
| Hungarian | 30 / 30 |
| Indian | 30 / 30 |
| Irish | 30 / 30 |
| Italian | 30 / 30 |
| Japanese | 30 / 30 |
| Macedonian | 30 / 30 |
| Mexican | 30 / 30 |
| Moroccan | 30 / 30 |
| Nigerian | 30 / 30 |
| Persian | 30 / 30 |
| Romanian | 30 / 30 |
| Serbian | 30 / 30 |
| Spanish | 30 / 30 |
| United States of America | 30 / 30 |
| BBQ Around the World | 100 / 100 |
| Fish & Seafood Around the World | 100 / 100 |

**Assigned collection total: 920 / 920.** All requested quotas are complete. The 53 unassigned canonical records do not count toward the requested quotas.

## Recipe authorship and cultural references

United States regional selections were informed by the [Library of Congress community-cookbook guide](https://guides.loc.gov/community-cookbooks/by-place), which describes broad regional patterns and the way dishes travelled, and the [USDA regional recipes](https://www.fna.usda.gov/americasharvest/regional). The records are Suric-authored home-cooking adaptations; regional labels describe the selected style and do not claim a single definitive origin for every dish.

Recipe directions and quantities in the catalogue JSON assets are original Suric-authored adaptations, not copied publisher text. Irish dish selections and regional descriptions were cross-checked against [Tourism Ireland's traditional food guide](https://www.ireland.com/en/magazine/food-and-drink/traditional-irish-food/) and the [National Museum of Ireland's account of Samhain foods](https://source.museum.ie/en-IE/News/2023/Ghost-turnips%2C-pucas%2C-fortune-telling-and-evil-abd). Argentinian regional dish selection was checked against [Argentina's official food-by-province guide](https://www.argentina.gob.ar/jefatura/turismo-y-ambiente/turismo/viaja-por-argentina/que-comer) and [Visit Argentina's Northwest food guide](https://www.argentina.travel/en/news/flavors-of-the-north-a-gastronomic-guide-to-explore-northern-argentina). The homemade floury roll is labelled Waterford-inspired and explicitly does not claim to reproduce the protected Waterford Blaa specification listed in [Ireland's protected-food-name register](https://www.gov.ie/en/department-of-agriculture-food-and-the-marine/publications/pdopgitsg-protected-food-names/).

German regional selection was guided by [Germany Travel's culinary guide](https://www.germany.travel/en/experience-enjoy/german-cuisine.html) and its [regional food overview](https://www.deutschland.de/en/topic/life/culinary-regions-germany). Recipes are Suric-authored home adaptations rather than transcriptions of publisher recipes.

Bosnian selections were cross-checked against [Visit B&H's traditional cuisine overview](https://visitbih.ba/en/traditional-bosnian-cuisine/) and its reporting on regional [cevapi traditions](https://visitbih.ba/sarajevski-travnicki-tuzlanski-ili-banjalucki-koji-su-najbolji-cevapi/). Recipes are Suric-authored adaptations; styles vary by household and region.

Bulgarian regional selections were guided by [Bulgaria Travel's traditional cuisine overview](https://visitbulgaria.com/about-bulgaria/traditional-cuisine/) and its [regional dish references](https://www.visitsofia.bg/en/item/3549-explore-sofia%E2%80%99s-culinary-scene). The recipes are Suric-authored home adaptations.

Greek regional selections were cross-checked against the [Visit Greece cuisine guide](https://www.visitgreece.gr/en/experiences/architecture/local-architecture/local-flavours-of-the-greek-cuisine) and its [Cycladic cuisine overview](https://www.visitgreece.gr/experiences/gastronomy/traditional-cuisine/cycladic-cuisine). Recipes are Suric-authored adaptations; regional variations and the menu of dishes reflect home-cooking approaches.

Hungarian dish selection was guided by Visit Hungary's [overview of Hungarian cuisine](https://visithungary.com/articles/all-you-need-to-know-about-hungarian-cuisine) and its guide to [classic Hungarian dishes](https://visithungary.com/articles/the-classics-of-hungarian-cuisine). Recipes are Suric-authored home adaptations.

Indian regional selections follow Incredible India's [food and cuisine overview](https://www.prod.incredibleindia.gov.in/content/incredible-india-v2/en/experiences/food-and-cuisine.html), which describes regional variation across the north, east, south, northeast and west. Recipes are Suric-authored home adaptations.

Japanese regional selections were guided by the Japan National Tourism Organization's [local foods guide](https://www.japan.travel/en/local-specialities/local-foods/) and its [regional cuisine coverage](https://www.japan.travel/en/gastronomy/local-cuisine-eastern-japan/). The recipes are Suric-authored home adaptations, with cooked toppings and recipe-specific handling notes.

The African regional collection spans 15 countries and is intended as a sample, not a single continental cuisine. South African dishes were cross-checked against [South African Tourism's regional food overview](https://www.southafrica.net/gl/en/travel/article/iconic-south-african-foods-taste-all-the-flavours-of-the-rainbow-nation); Ethiopian selections were informed by [Visit Ethiopia's cuisine notes](https://visitethiopia.travel/downloads/AU_2025%20Brochure_English.pdf); and Moroccan selections by the [Moroccan National Tourist Office gastronomy guide](https://www.visitmorocco.com/en/discover-morocco/gastronomy). Recipes are Suric-authored home adaptations.

Brazilian regional selections were checked against [Fundaj's Brazilian cuisine guide](https://pesquisaescolar.fundaj.gov.br/en/artigo/brazilian-cuisine/), the [Brazil Ministry of Tourism's food guide](https://www.gov.br/turismo/pt-br/assuntos/noticias/culinaria-brasileira-conquista-o-12o-lugar-em-ranking-internacional), and the heritage notes for the [Baianas do Acaraje tradition](https://www.gov.br/cultura/pt-br/assuntos/noticias-defeso-eleitoral/oficio-das-baianas-de-acaraje-mantem-vivos-saberes-de-matriz-africana-transmitidos-entre-geracoes). The recipes are Suric-authored home adaptations.

Mexican regional selection follows the official [UNESCO description of traditional cuisine](https://ich.unesco.org/en/RL/traditional-mexican-cuisine-ancestral-ongoing-community-culture-the-michoacan-paradigm-00400) and the Mexican government's [traditional cooking overview](https://sic.cultura.gob.mx/ficha.php?table=gastronomia&table_id=12). Recipes are Suric-authored adaptations; regional labels do not claim a single definitive recipe.

Spanish regional selection was guided by Spain.info's official overview of [traditional dishes](https://www.spain.info/en/discover-spain/dishes-traditional-cuisine-spain/) and its [regional cuisine guide](https://www.spain.info/es/consulta/cocinas-regionales-espana/). Recipes are Suric-authored home adaptations.

French regional selections were cross-checked against [Explore France's regional gastronomy guide](https://www.france.fr/en/list/gastronomy-and-wine/), [its Alsatian cuisine guide](https://www.france.fr/en/article/alsatian-cuisine/) and [its Burgundy cuisine guide](https://www.france.fr/en/article/burgundy-cuisine/). The collection spans regional dishes and common home cooking; recipes are Suric-authored adaptations and do not claim one definitive version.

Italian regional selections were cross-checked against [Italia.it's food and wine guide](https://www.italia.it/en/italy/things-to-do/food-and-wine), its overview of [Italian cuisine's regional heritage](https://www.italia.it/en/italy/things-to-do/italian-cuisine-unesco-heritage), and its [Lazio](https://www.italia.it/en/lazio/things-to-do/typical-food-and-dishes-in-lazio-italy) and [Sicily](https://www.italia.it/en/sicily/things-to-do/typical-sicilian-products-dishes) food guides. Recipes are Suric-authored home adaptations; seafood and raw-egg handling have recipe-specific safety notes.

Chinese regional selections were informed by China Culture's overviews of the [eight traditional regional cuisines](https://en.chinaculture.org/chineseway/2013-04/17/content_455945_2.htm) and [Huaiyang cuisine](https://en.chinaculture.org/2018-01/01/content_1129489.htm). Croatian selections draw on Croatia's official tourism materials for [regional gastronomy](https://croatia.hr/cmsmedia/uputa/HTZ-ReleaseNotes-WEB.pdf) and [coastal and continental dishes](https://croatia.hr/cmsmedia/Aktivnost/Poslovni_turizam/Factsheets/Hotel_Zora_Congress_Brochure.pdf). North Macedonian dishes were cross-checked against [North Macedonia Tourism's food guide](https://northmacedoniatourism.com/plan/food-guide) and the [Tourism Macedonia traditional-tastes brochure](https://tourismmacedonia.gov.mk/wp-content/uploads/2018/09/1.-Skopje-A-WALK-THROUGH-THE-ETERNAL-CITY.pdf). Moroccan selection was guided by the [Moroccan National Tourist Office gastronomy overview](https://www.visitmorocco.com/en/discover-morocco/gastronomy) and its [food and drink guide](https://www.visitmorocco.com/en/travel-info/food-drinks). Nigerian regional selections draw on the [National Institute for Cultural Orientation's six-zone overview](https://nico.gov.ng/cuisines-in-nigeria/) and the [Federal Ministry of Information cuisine list](https://fmino.gov.ng/culture/cuisine/). Persian regional choices were informed by the FAO's note on [Iran's provincial food diversity](https://www.fao.org/fileadmin/templates/ess/CountrySTAT/ECO_Tehran/Docs/General_Information_Note_on_Iran_and_the_FAO_Representation_in_the_Country_-_July2016.pdf) and Visit Iran's [Sistan and Baluchestan Tabaheg profile](https://visitiran.ir/en/cuisine/tabaheg). Romanian selections were checked against the Romania Travel guides to [sarmale](https://www.turismorumania.com/en/food-in-romania/sarmale) and [Romanian cuisine](https://www.turismorumania.com/en/magazine/romanian-cuisine-traditional-flavors-and-a-rich-culinary-heritage). Serbian regional selections were guided by the [National Tourism Organisation of Serbia's cuisine guide](https://www.serbia.travel/en/taste-of-serbia/) and its [authentic food overview](https://www.serbia.travel/en/bilten/authentic-tastes-of-serbia/). Recipes are Suric-authored home adaptations; regional labels describe inspiration and do not claim one definitive version.

The barbecue collection includes US regional styles such as Texas brisket, Carolina vinegar pulled pork, Kansas City glazed ribs and Memphis dry-rub ribs, alongside traditions from other regions. Background selections were checked against [Visit the USA's regional barbecue guide](https://traveltrade.visittheusa.com/trip/low-slow-delicious-regional-guide-to-u-s-barbecue-styles/) and [Memphis Tourism's food guide](https://tourismmemphis.com/food). Global selections include Brazilian churrasco, Korean barbecue and other regional grilling traditions, cross-checked against [Visit Brasil's gastronomy guide](https://www.visitbrasil.com/en/experience/gastronomy) and [Korea Tourism Organization's food guide](https://english.visitkorea.or.kr/svc/contents/contentsView.do?menuSn=904&vcontsId=221309). Seafood selection references the [FDA's guidance on selecting and serving seafood safely](https://www.fda.gov/food/buy-store-serve-safe-food/selecting-and-serving-fresh-and-frozen-seafood-safely); meat and seafood cooking-temperature guidance follows the [USDA FSIS safe-temperature chart](https://www.fsis.usda.gov/food-safety/safe-food-handling-and-preparation/food-safety-basics/safe-temperature-chart). All recipes are Suric-authored home adaptations.

The added potato/tomato blight and container-watering guides draw on [RHS blight advice](https://www.rhs.org.uk/disease/tomato-blight) and [RHS watering guidance](https://www.rhs.org.uk/vegetables/watering). Wall-drilling safety is cross-checked against [Electrical Safety First's DIY guidance](https://www.electricalsafetyfirst.org.uk/safety-advice/home-and-people/diy-ers/). Cleaner handling is cross-checked against [UK HSE cleaning-product guidance](https://www.hse.gov.uk/chemical-classification/labelling-packaging/cleaning-products-and-corrosive-pictogram.htm), with the product label controlling use. Phone recycling guidance references [WEEE Ireland's collection information](https://www.weeeireland.ie/household-recycling/where-can-i-recycle/).

## Storage and navigation

The app loads the two article JSON files, the base recipe catalogue, and the supplemental catalogues named in the version-1 manifest off the main thread. Each article has a stable ID, section, title, summary, revision, tags and full text. Recipes have separate canonical records and collection references. The global offline-search screen checks article text and recipe titles, origin, ingredients and tags and can filter by section or recipes. All 26 current recipe collections open complete recipe details with measurements, directions, sides, substitutions, storage and food-safety notes. Back returns to the containing section. `suric://library/{id}` can open a matching bundled article in an installed Suric copy. Recipe-specific deep links and a verified HTTPS App Links domain are not configured.

**Copy to section** creates an independent encrypted personal entry. It does not alter the bundled article, and later edits/deletion affect only the personal copy. Personal entries can be moved among sections in their editor; the move retains the entry ID and encrypted store, and shopping-only retailer metadata is cleared when moved out of Shopping.

Outgoing private-entry sharing requires confirmation and uses Android Sharesheet with a versioned text envelope. The receiving app shows a preview and saves only after confirmation. The export contains only the selected title, section and body; it excludes the local entry ID, credentials and other records. The text copy is readable by the chosen receiving app. Image attachments and portable binary bundles are not implemented.

## Music, photos and AI

Music can select local audio through Android's document picker, retain its granted URI, save a queue entry, play/pause, seek and remember the last position. This implementation has no playback service or notification controls, and playback stops when the Music screen leaves composition. Online choices open through Android's chooser. **Open Naxi Radio** uses Naxi's radio page; the online music chooser can offer apps that handle the YouTube URL. NewPipe is not bundled or embedded; its official upstream releases page is provided separately.

Ask Suric's photo composer displays a thumbnail, the user message retains its URI for display/retry, and a zoomable preview opens over the conversation. The existing Responses request still sends the selected image only when Send is tapped. Message history and draft are saved in app-private AES-GCM encrypted preferences and restored after process recreation; the store surfaces read errors without replacing unreadable saved content. A confirmed Delete history action removes the saved messages and draft from this device. Image display after recreation depends on Android retaining access to the selected Photo Picker URI.

Chat history can be shared as plain text through Android Sharesheet and deleted from this device after confirmation. Export excludes image files and their local URI references. The assistant does not yet use typed function tools or perform app writes. Existing manual Add/Edit actions remain local. No whole-store context is uploaded automatically; the existing selected-note actions remain user initiated.

## Unimplemented content depth

All 24 traditional cuisine quotas and both 100-entry BBQ and Fish & Seafood quotas are complete: 920 of 920 requested collection entries. Gardening needs more Irish climate-aware crop and diagnosis guides; pottery, DIY, cleaning and Retro & Android need substantial topic coverage; Music has no bundled offline knowledge. There are no offline photos attached to the library articles. Shopping, bills and external firmware/radio/music information have the online-only limits described above.

The BBQ and Fish & Seafood collection lists display generated, bundled cover images when those collections are selected. The photorealistic images depict representative dishes for each collection; they do not count as recipe images. The recipe detail screen supports a horizontal gallery using offline image assets. One image is currently assigned to Texas smoked beef brisket. The requested target is five images for each of 891 unique canonical recipes (4,455 total); 4,454 more recipe images remain. The existing 920 collection memberships refer to those 891 unique recipe records.

No tests, lint, builds, emulator checks or GitHub Actions were run for this change, as requested.
