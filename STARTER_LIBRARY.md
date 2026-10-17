# Suric Starter Library

The Starter Library is immutable reference material compiled into the APK. It is available without connectivity and is not inserted into personal records on installation. A user must select **Copy to my space** to create a separate, editable entry in the existing encrypted personal-entry store.

All items currently use content revision `1` and show `17 October 2026` as the review date. Library content is held in `StarterLibrary.kt`; IDs are stable across releases. Catalogue changes must preserve existing IDs where meaning is unchanged, increment the revision for edited content, and must never rewrite personal copies.

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
| **Total** |  | **28** |

## Source and safety notes

- Gardening guides are practical summaries informed by Teagasc's [Introduction to Vegetable Growing](https://teagasc.ie/crops/horticulture/vegetables/introduction-a-guide-to-vegetable-growing/) and [Pest and Disease guidance](https://teagasc.ie/crops/horticulture/nursery-stock/pest-and-disease/). They avoid local forecast or fixed-frost-date claims and direct users to plant-specific instructions.
- Cleaning checklists use conservative product-label and chemical-separation advice. The cited [HSE safe-use sheet](https://www.hse.gov.uk/pubns/cais22.pdf) is written for hospitality workplaces; it supports the warning not to mix bleach products with acids or ammonia and is not presented as a household-cleaning standard.
- Cooking content is household recipe guidance. The chicken recipe states an internal temperature of at least 75°C and general leftover handling consistent with the [Food Safety Authority of Ireland cooking and reheating guidance](https://www.fsai.ie/consumer-advice/food-safety-and-hygiene/cooking-and-reheating). Follow package cooking instructions and current FSAI advice when they differ.
- Sekeli goulash is labelled a household version because recipes vary. It is served with fresh bread and potato dumplings as requested.
- DIY templates are blank planning fields. They do not instruct the user to perform gas, electrical or structural work.
- Pottery templates record project, glaze and kiln details. They do not prescribe firing temperatures; follow the clay, glaze and kiln manufacturer's instructions and the studio's approved firing process.
- Shopping items are blank user research templates. They do not claim live prices, stock or retailer verification and continue to use the existing HTTPS validation when saved in the Shopping section.
- Bill templates are blank and do not request calendar permission when opened.

## Offline imagery

Where available, the library reuses Suric's existing optimized WebP section photographs. No image is downloaded at runtime, no new image dependency is required, and the detail view remains usable if no image key is assigned.
