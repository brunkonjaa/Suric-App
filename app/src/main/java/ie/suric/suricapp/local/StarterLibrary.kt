package ie.suric.suricapp.local

/** Immutable reference content shipped in the APK. Personal copies are ordinary encrypted entries. */
enum class StarterType { GUIDE, RECIPE, CHECKLIST, TEMPLATE }

data class StarterBlock(val heading: String, val lines: List<String>)

data class StarterItem(
    val id: String,
    val category: String,
    val title: String,
    val summary: String,
    val type: StarterType,
    val blocks: List<StarterBlock>,
    val imageKey: String? = null,
    val revision: Int = 1,
    val reviewed: String? = null,
    val sources: List<Pair<String, String>> = emptyList(),
    val destination: String
) {
    fun asPersonalText(): String = buildString {
        append(summary)
        blocks.forEach { block ->
            append("\n\n").append(block.heading).append('\n')
            block.lines.forEach { append("• ").append(it).append('\n') }
            if (endsWith("\n")) setLength(length - 1)
        }
        reviewed?.let { append("\n\nReference reviewed: ").append(it) }
        if (sources.isNotEmpty()) {
            append("\n\nSources / further reading:\n")
            sources.forEach { (label, url) -> append("• ").append(label).append(": ").append(url).append('\n') }
            if (endsWith("\n")) setLength(length - 1)
        }
    }
}

object StarterLibrary {
    const val REVIEWED = "17 October 2026"
    val categories = listOf("Gardening", "Cooking", "Cleaning", "DIY & Home", "Pottery", "Shopping", "Bills")

    private fun block(title: String, vararg lines: String) = StarterBlock(title, lines.toList())
    private fun guide(id: String, title: String, summary: String, vararg blocks: StarterBlock) = StarterItem(
        id, "Gardening", title, summary, StarterType.GUIDE, blocks.toList(), "suric_garden", reviewed = REVIEWED,
        sources = listOf(
            "Teagasc vegetable growing guide" to "https://teagasc.ie/crops/horticulture/vegetables/introduction-a-guide-to-vegetable-growing/",
            "Teagasc pest and disease guidance" to "https://teagasc.ie/crops/horticulture/nursery-stock/pest-and-disease/"
        ), destination = "Garden"
    )
    private fun recipe(id: String, title: String, summary: String, servings: String, prep: String, cook: String, ingredients: List<String>, steps: List<String>, variations: List<String>, storage: String,
        sources: List<Pair<String, String>> = listOf("Food Safety Authority of Ireland — cooking and reheating" to "https://www.fsai.ie/consumer-advice/food-safety-and-hygiene/cooking-and-reheating")) = StarterItem(
        id, "Cooking", title, summary, StarterType.RECIPE,
        listOf(block("Yield and timing", "Serves: $servings", "Preparation: $prep", "Cooking: $cook"), block("Ingredients", *ingredients.toTypedArray()), block("Method", *steps.toTypedArray()), block("Variations", *variations.toTypedArray()), block("Storage", storage)),
        "suric_cooking", reviewed = REVIEWED, sources = sources, destination = "Cooking"
    )
    private fun checklist(id: String, category: String, title: String, summary: String, vararg steps: String) = StarterItem(
        id, category, title, summary, StarterType.CHECKLIST, listOf(block("Checklist", *steps)),
        if (category == "Cleaning") "suric_cleaning" else null, reviewed = REVIEWED,
        sources = if (category == "Cleaning") listOf("HSE cleaning-substance guidance" to "https://www.hse.gov.uk/pubns/cais22.pdf") else emptyList(),
        destination = if (category == "Cleaning") "Cleaning" else "Garden"
    )
    private fun template(id: String, category: String, title: String, summary: String, vararg fields: String) = StarterItem(
        id, category, title, summary, StarterType.TEMPLATE, listOf(block("Record fields", *fields)),
        when (category) { "DIY & Home" -> "suric_diy"; "Pottery" -> "suric_pottery"; "Shopping" -> "suric_shopping"; else -> "suric_bills" },
        reviewed = REVIEWED, destination = when (category) { "DIY & Home" -> "DIY & Home"; "Pottery" -> "Pottery"; "Shopping" -> "Shopping"; else -> "Bills" }
    )

    val items: List<StarterItem> = listOf(
        guide("garden.spring-start", "Spring preparation and seed starting", "Get beds and containers ready, then sow at a pace you can care for.",
            block("Prepare", "Remove debris and perennial weeds while avoiding damage to emerging plants.", "Check containers have drainage holes; wash reused pots and trays.", "Use clean seed compost and follow each packet's depth, spacing and timing.", "Label each sowing with crop and date."),
            block("Sow and tend", "Sow indoors only where the crop and available light/space make this worthwhile.", "Keep compost evenly moist, not waterlogged; ventilate covered trays after germination.", "Harden off suitable seedlings gradually before planting outdoors.", "Use local conditions and the seed packet; do not rely on a fixed frost date.")),
        guide("garden.summer-care", "Summer watering and maintenance", "Check soil and plants rather than watering by calendar alone.",
            block("Routine", "Feel the soil below the surface; water when the root zone is becoming dry.", "Water the soil near roots, preferably in the cooler part of the day, and avoid repeated light sprinkling.", "Containers dry faster than beds; check them more often during warm, windy weather.", "Tie in climbing plants and harvest regularly where appropriate."),
            block("Use water carefully", "Prioritise newly planted, container-grown and visibly stressed plants.", "Use collected rainwater where suitable and permitted; keep water containers covered and safe.", "Do not assume a plant is thirsty from a brief midday droop alone; check again when cooler.")),
        guide("garden.autumn-care", "Autumn garden care", "Tidy selectively, protect soil and plan next season without removing every habitat.",
            block("Useful jobs", "Remove diseased plant material and compost healthy material only if your compost process is suitable.", "Harvest remaining crops and record varieties or sowing dates that worked.", "Add a suitable mulch to bare soil when conditions allow; keep it clear of plant stems.", "Bring tender potted plants under shelter if their care label requires it."),
            block("Leave some structure", "Keep some seed heads and stems where safe and useful for wildlife.", "Check local restrictions before cutting hedges or disturbing nesting wildlife.", "Clean and store tools dry; inspect pots and supports for damage.")),
        guide("garden.winter-protection", "Winter protection", "Protect vulnerable plants based on their needs and the conditions they face.",
            block("Before cold weather", "Check plant labels or a reliable reference for hardiness and overwintering needs.", "Move tender containers to a sheltered, bright place if appropriate.", "Raise pots slightly so drainage holes are not blocked; avoid leaving roots standing in water.", "Use breathable horticultural fleece only when needed and remove or ventilate it when conditions improve."),
            block("During winter", "Water evergreen containers sparingly when compost is dry and conditions are frost-free.", "After storms, check ties, supports, coverings and drainage.", "Do not prune or feed automatically; follow the plant's requirements.")),
        guide("garden.herbs-in-pots", "Growing herbs in pots", "A small group of well-drained pots can provide useful herbs close to the kitchen.",
            block("Set up", "Choose a pot with drainage holes and a suitable peat-free multipurpose or herb compost.", "Place sun-loving herbs where they receive good light; match each herb to its water needs.", "Use separate pots for herbs with different growth habits or watering preferences.", "Label plants, especially if varieties look alike."),
            block("Care and harvest", "Water when the compost begins to dry; do not leave the pot standing in water.", "Pinch or cut small amounts regularly, avoiding removal of most of the plant at once.", "Check leaf undersides for pests and remove affected leaves; identify a problem before using any treatment.", "Replace plants when they become woody or overcrowded.")),
        guide("garden.plant-inspection", "Routine plant inspection", "A short, regular check helps catch changes early without guessing at a diagnosis.",
            block("Check", "Look at new and older leaves, stems, soil surface and the underside of leaves.", "Note the plant, date, affected part, recent watering and any change in light or temperature.", "Check whether the soil is too wet or dry and whether drainage is clear.", "Photograph the same area over time to see whether the issue is spreading."),
            block("Respond carefully", "Remove only clearly dead or damaged material with clean tools.", "Identify the pest or disease before choosing a treatment; consider growing conditions first.", "Follow product labels and Irish rules if a plant-protection product is ever used; keep children and pets away as directed.", "Seek specialist advice if damage is severe or the cause remains uncertain.")),

        recipe("cook.sekeli-goulash", "Sekeli goulash with fresh bread and potato dumplings", "A hearty home-style Székely goulash with sauerkraut, served with bread and soft potato dumplings. Recipes vary; this is a practical household version.", "4", "25 minutes", "about 1 hour 45 minutes", listOf(
            "700 g pork shoulder, cut into 2–3 cm pieces", "1 tbsp neutral oil", "2 medium onions, finely chopped", "2 garlic cloves, chopped", "2 tbsp sweet paprika", "1 tsp caraway seeds (optional)", "250 ml stock or water", "500 g drained sauerkraut, rinsed if very sharp", "150 g sour cream, plus extra to serve", "Salt and black pepper", "To serve: fresh bread",
            "Dumplings: 500 g floury potatoes, peeled and cooked; 180–220 g plain flour; 1 egg; ½ tsp salt; extra flour for shaping"), listOf(
            "Cook potatoes for the dumplings until tender; drain very well and leave to steam dry. Mash smoothly and cool until warm, not hot.", "Heat oil in a heavy pot. Brown pork in batches; transfer to a plate.", "Soften onions in the pot for 6–8 minutes. Add garlic and caraway for 30 seconds. Take the pot off the heat, stir in paprika, then add a splash of stock.", "Return pork, add remaining stock, season lightly, cover and simmer gently until nearly tender, about 60–75 minutes. Add a little water if the pot becomes dry.", "Stir in sauerkraut and simmer uncovered for 20–30 minutes, until pork is tender and flavours combine.", "Mix mashed potato, egg and salt. Add flour gradually until a soft dough holds together; avoid overworking. Shape into 2 cm dumplings with floured hands.", "Cook dumplings in gently boiling salted water in batches for about 3–5 minutes after they rise; cut one open to check it is cooked through.", "Stir sour cream into the goulash off the boil or serve it on top. Taste and adjust seasoning. Serve hot with dumplings and fresh bread."), listOf("Use pork neck or shoulder; leaner cuts may cook drier.", "For a milder sauce, rinse the sauerkraut and add it gradually.", "If the dumpling dough is sticky, add flour a spoonful at a time; excess flour makes them dense."), "Cool leftovers promptly, refrigerate covered and use within 2 days. Reheat until steaming hot. Keep raw pork separate from ready-to-eat food and wash hands and utensils after handling."),
        recipe("cook.tomato-lentil-soup", "Tomato and red lentil soup", "A filling pantry soup that works for lunch or a light dinner.", "4", "10 minutes", "30 minutes", listOf("1 tbsp olive oil", "1 onion, diced", "2 carrots, diced", "2 garlic cloves", "150 g red lentils, rinsed", "1 × 400 g tin chopped tomatoes", "900 ml vegetable stock", "1 tsp ground cumin (optional)", "Salt, pepper and lemon juice"), listOf("Soften onion and carrot in oil for 6–8 minutes.", "Add garlic and cumin; stir for 30 seconds.", "Add lentils, tomatoes and stock. Simmer 20–25 minutes, stirring occasionally, until lentils are soft.", "Blend partly or fully if desired. Season with pepper, salt and a little lemon."), listOf("Add spinach near the end.", "Serve with yogurt or bread if suitable."), "Refrigerate promptly and use within 3 days; reheat until steaming hot."),
        recipe("cook.chicken-traybake", "Chicken and root-vegetable traybake", "A one-tray dinner with flexible vegetables and a clear doneness check.", "4", "15 minutes", "40–50 minutes", listOf("4 chicken thighs", "700 g potatoes, cut into chunks", "2 carrots, cut into batons", "1 red onion, wedges", "1 tbsp oil", "1 tsp dried thyme", "Black pepper and salt", "Optional: lemon wedges"), listOf("Heat oven to 200°C (180°C fan). Use a roasting tray large enough to avoid crowding.", "Toss vegetables with oil and thyme; arrange chicken among them. Season.", "Roast 40–50 minutes, turning vegetables once. Check the thickest part of each chicken piece reaches at least 75°C using a clean food thermometer. Do not rely on colour or juices alone.", "Rest briefly and serve."), listOf("Swap carrots for parsnips or peppers.", "Add delicate vegetables later so they do not overcook."), "Cool leftovers promptly, refrigerate at 0–5°C within 2 hours and use within 2–3 days; reheat thoroughly once."),
        recipe("cook.pasta-veg", "Quick vegetable tomato pasta", "A flexible midweek meal using vegetables that need using.", "4", "10 minutes", "20 minutes", listOf("300 g pasta", "1 tbsp olive oil", "1 onion, sliced", "1 courgette, diced", "1 pepper, diced", "1 × 400 g tin chopped tomatoes", "1 tsp dried oregano", "Grated cheese, optional"), listOf("Cook pasta in salted water according to its packet; reserve a mug of cooking water.", "Soften onion in oil, add courgette and pepper, and cook until beginning to soften.", "Add tomatoes and oregano; simmer 10–12 minutes.", "Toss with drained pasta, adding a splash of pasta water to loosen. Serve with optional cheese."), listOf("Add cooked beans for extra protein.", "Use frozen peas or spinach near the end."), "Refrigerate promptly and use within 3 days. Reheat until hot throughout."),
        recipe("cook.soda-bread", "Simple Irish soda bread", "A quick loaf leavened with bicarbonate of soda and buttermilk.", "1 loaf", "10 minutes", "35–40 minutes", listOf("450 g plain flour", "1 tsp bicarbonate of soda", "1 tsp salt", "350–400 ml buttermilk"), listOf("Heat oven to 220°C (200°C fan). Dust a tray with flour.", "Mix flour, soda and salt. Make a well and add most of the buttermilk.", "Bring together gently; add only enough remaining buttermilk for a soft, not sticky, dough.", "Shape quickly into a round. Place on tray and cut a deep cross on top.", "Bake 35–40 minutes until browned and the base sounds hollow when tapped. Cool on a rack."), listOf("Add a small handful of raisins for a sweeter loaf.", "If buttermilk is unavailable, use a recipe-specific tested substitute rather than guessing quantities."), "Cool fully. Keep wrapped for about 2 days or freeze sliced."),
        recipe("cook.apple-crumble", "Apple crumble", "A simple baked dessert with a crisp oat topping.", "6", "15 minutes", "35–45 minutes", listOf("700 g cooking apples, peeled and sliced", "1–2 tbsp sugar, to taste", "1 tbsp water", "150 g plain flour", "100 g cold butter, cubed", "75 g oats", "75 g brown sugar", "½ tsp cinnamon, optional"), listOf("Heat oven to 190°C (170°C fan). Put apples, water and a little sugar in a baking dish.", "Rub butter into flour until coarse crumbs form; stir in oats, sugar and optional cinnamon.", "Scatter topping over apples without pressing it down.", "Bake 35–45 minutes until the top is golden and fruit is bubbling. Rest before serving."), listOf("Mix cooking and eating apples.", "Serve with custard, cream or yogurt."), "Cool leftovers promptly, refrigerate covered and use within 3 days."),

        checklist("clean.daily-kitchen", "Cleaning", "Daily kitchen reset", "A short reset to leave the kitchen ready for its next use.", "Put away food and refrigerate perishables promptly.", "Load or wash dishes; clear the sink.", "Wipe food-contact surfaces with a suitable product used according to its label.", "Wipe hob only after it is cool.", "Empty food scraps and check the floor for spills.", "Replace cloths or leave them to dry; wash hands after handling waste."),
        checklist("clean.weekly-bathroom", "Cleaning", "Weekly bathroom clean", "Work from cleaner areas toward the toilet; follow product labels and ventilate as directed.", "Remove towels and loose items; ventilate the room.", "Clean mirror and sink with suitable products; rinse where the label requires.", "Clean shower or bath and fixtures; remove visible residue.", "Clean the toilet last with a dedicated cloth or brush.", "Wipe handles and touch points; allow surfaces to dry.", "Never mix cleaning products, especially bleach with acids or ammonia. Store products away from children."),
        checklist("clean.weekly-home", "Cleaning", "Weekly home reset", "A repeatable order for the shared spaces; adjust to the household's needs.", "Collect rubbish and recycling for the correct local collection.", "Gather laundry and start one manageable load.", "Clear surfaces and return items to their usual places.", "Dust high-to-low; use a suitable cloth.", "Vacuum or sweep floors, then mop hard floors if needed.", "Check supplies and choose the next small task rather than trying to deep-clean everything."),
        checklist("clean.appliance-monthly", "Cleaning", "Monthly appliance maintenance", "Use each appliance manual as the authority; isolate power where the manual requires it.", "Fridge: remove spoiled food, wipe shelves with a suitable method, and check door seals.", "Dishwasher: clean filter and check spray arms as described in the manual.", "Washing machine: wipe seal and detergent drawer; run the manufacturer's recommended care cycle if needed.", "Kettle or coffee maker: descale only using its instructions and a compatible product.", "Check cooker hood filters and clean or replace as specified.", "Do not remove guards or service electrical parts unless qualified."),
        checklist("clean.deep-clean", "Cleaning", "Periodic deeper-clean checklist", "Choose one area at a time and stop if a job needs specialist help.", "Choose the room and a realistic time limit; gather labelled products and gloves if directed.", "Move light items safely; do not lift heavy furniture alone.", "Clean overlooked touch points, skirting and reachable vents without disturbing wiring.", "Wash washable curtains or covers according to their care label.", "Inspect for damp, mould or damage; address the moisture source and seek advice for extensive problems.", "Dispose of products and waste according to the label and local rules. Never mix chemicals."),

        template("diy.project", "DIY & Home", "DIY project planner", "Blank planning sheet for a household project.", "Project name:", "Room / location:", "What I want to change:", "Measurements and constraints:", "Steps and order:", "Tools and materials:", "Budget estimate:", "Safety checks / when a qualified tradesperson is needed:", "Start date:", "Review / completion notes:"),
        template("diy.tools", "DIY & Home", "Tools and materials checklist", "List what is needed before starting and check condition and compatibility.", "Project:", "Tool / material:", "Quantity / size:", "Already available?", "Condition / compatible with task?", "Borrow / buy:", "PPE required by product or tool instructions:", "Notes:"),
        template("diy.measurements", "DIY & Home", "Measurements and project notes", "Keep measurements with units and note where each measurement was taken.", "Project / location:", "Date measured:", "Measurement A and unit:", "Measurement B and unit:", "Measurement C and unit:", "Reference point / sketch description:", "Material or surface:", "Tolerance / uncertainty:", "Notes / photo reference:"),
        template("diy.maintenance", "DIY & Home", "Home-maintenance inspection", "A periodic visual record; stop and seek a qualified professional for specialist work.", "Date and area inspected:", "Visible condition:", "Leaks, damp or unusual smells:", "Doors, windows and seals:", "Smoke / heat alarm check per manufacturer's instructions:", "Issue and urgency:", "Next action / qualified professional:", "Review date:"),

        template("pottery.project", "Pottery", "Pottery project template", "Record a piece from clay preparation through finishing.", "Project / piece ID:", "Form and intended use:", "Clay body and batch:", "Build / throwing method:", "Dimensions at leather-hard:", "Drying notes:", "Bisque firing reference:", "Glaze and application:", "Final firing schedule reference:", "Result and observations:"),
        template("pottery.glaze-test", "Pottery", "Glaze-testing log", "Track small glaze tests so results can be repeated or compared.", "Test ID and date:", "Clay body:", "Test tile shape / thickness:", "Glaze name, maker and batch:", "Application method / coats:", "Kiln and shelf position:", "Firing schedule and atmosphere:", "Observed colour, texture and defects:", "Photo / follow-up:"),
        template("pottery.kiln-log", "Pottery", "Kiln-firing log", "Document the actual firing and follow the kiln and material manufacturers' guidance.", "Firing ID and date:", "Kiln make / model:", "Load description:", "Clay and glaze references:", "Program / ramp / hold copied from approved schedule:", "Witness cones, where used:", "Actual start and finish:", "Cooling / unloading notes:", "Results, defects and next test:"),

        template("shopping.research", "Shopping", "Product research template", "Record details you personally observed; Suric does not fetch or verify live retail information.", "Product:", "Retailer:", "Product link (HTTPS):", "Price observed:", "Currency:", "Date checked:", "Notes / requirements:", "Availability and delivery not independently verified:"),
        template("shopping.comparison", "Shopping", "Product comparison template", "Compare options using the same criteria and check current details with the retailer.", "Product / need:", "Option A — retailer, link, price, currency, checked date:", "Option B — retailer, link, price, currency, checked date:", "Option C — retailer, link, price, currency, checked date:", "Features that matter:", "Warranty / returns details checked:", "Unknowns to verify:", "Decision and reason:"),
        template("bills.monthly", "Bills", "Monthly bill organiser", "Blank monthly organiser. Opening this item does not request calendar access.", "Month:", "Bill / payee:", "Amount and currency:", "Due date:", "Payment method / reminder:", "Status (update yourself):", "Notes:"),
        template("bills.irregular", "Bills", "Annual or irregular payment organiser", "Plan for annual, seasonal or one-off household costs without prefilled assumptions.", "Payment / purpose:", "Expected amount and currency:", "Expected month or date:", "How the estimate was set:", "Reminder lead time:", "Status (update yourself):", "Notes / review date:")
    )

    init {
        require(items.size == 28)
        require(items.map { it.id }.toSet().size == items.size)
        require(items.all { it.category in categories && it.blocks.isNotEmpty() && it.destination in PersonalEntryStore.SECTIONS })
    }

    fun search(query: String, category: String? = null): List<StarterItem> {
        val needle = query.trim()
        return items.filter { item ->
            (category == null || category == item.category) && (needle.isEmpty() || listOf(item.title, item.category, item.summary, item.blocks.joinToString(" ") { it.heading + " " + it.lines.joinToString(" ") }).any { it.contains(needle, ignoreCase = true) })
        }
    }

    fun byId(id: String): StarterItem? = items.firstOrNull { it.id == id }
}
