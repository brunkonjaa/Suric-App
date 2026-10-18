package ie.suric.suricapp.local

import org.junit.Assert.*
import org.junit.Test

class StarterLibraryTest {
    @Test fun bundledCatalogueHasUniqueIdsAndExpectedCategoryCounts() {
        assertEquals(38, StarterLibrary.items.size)
        assertEquals(38, StarterLibrary.items.map { it.id }.toSet().size)
        assertEquals(mapOf("Gardening" to 6, "Cooking" to 6, "Cleaning" to 5, "DIY & Home" to 4, "Pottery" to 3, "Shopping" to 2, "Bills" to 2, "Retro & Android" to 10), StarterLibrary.items.groupingBy { it.category }.eachCount())
        assertTrue(StarterLibrary.items.all { it.blocks.isNotEmpty() && it.destination in PersonalEntryStore.SECTIONS })
    }

    @Test fun searchIsLocalCaseInsensitiveAndCategoryScoped() {
        assertEquals("Sekeli goulash with fresh bread and potato dumplings", StarterLibrary.search("  SEKELI  ").single().title)
        assertTrue(StarterLibrary.search("seKeli", "Gardening").isEmpty())
        assertTrue(StarterLibrary.search("unmatched phrase").isEmpty())
    }

    @Test fun sekeliRecipeContainsMeasuredIngredientsAndUsableMethod() {
        val item = StarterLibrary.byId("cook.sekeli-goulash")!!
        assertEquals(StarterType.RECIPE, item.type)
        assertTrue(item.blocks.single { it.heading == "Yield and timing" }.lines.any { it == "Serves: 4" })
        assertTrue(item.blocks.single { it.heading == "Ingredients" }.lines.any { it.contains("700 g pork shoulder") })
        assertTrue(item.blocks.single { it.heading == "Method" }.lines.size >= 6)
        assertTrue(item.asPersonalText().contains("fresh bread"))
        assertTrue(item.asPersonalText().contains("potato dumplings"))
    }

    @Test fun personalCopyIsDeterministicAndOriginalRemainsImmutable() {
        val original = StarterLibrary.byId("shopping.research")!!
        val copyText = original.asPersonalText()
        assertTrue(copyText.contains("Product link (HTTPS)"))
        assertTrue(copyText.contains("Suric does not fetch"))
        assertEquals("Shopping", original.destination)
        assertEquals(original, StarterLibrary.byId(original.id))
    }

    @Test fun retroLibraryCoversAndroidAndEveryRequestedPlayStationGeneration() {
        val expectedIds = setOf(
            "retro.start-here", "retro.android-version-map", "retro.android-root-roms", "retro.ps1-homebrew",
            "retro.ps2-homebrew", "retro.ps3-homebrew", "retro.ps4-homebrew", "retro.ps5-homebrew",
            "retro.homebrew-catalogues", "retro.modded-warfare"
        )
        val retroItems = StarterLibrary.items.filter { it.category == "Retro & Android" }
        assertEquals(expectedIds, retroItems.map { it.id }.toSet())
        assertTrue(retroItems.all { it.destination == "Retro & Android" && it.sources.isNotEmpty() && it.reviewed == "18 October 2026" })
        assertTrue(StarterLibrary.byId("retro.android-version-map")!!.blocks.flatMap { it.lines }.any { it.contains("Android 1.0 — API 1") })
        assertTrue(StarterLibrary.byId("retro.android-version-map")!!.blocks.flatMap { it.lines }.any { it.contains("Android 17 — API 37") })
        assertTrue(listOf("ps1", "ps2", "ps3", "ps4", "ps5").all { suffix -> StarterLibrary.byId("retro.$suffix-homebrew") != null })
        assertTrue(retroItems.flatMap { it.sources }.all { (_, url) -> url.startsWith("https://") })
    }

    @Test fun retroGuidesCanBeSearchedAndCopiedWithTheirSources() {
        val ps5 = StarterLibrary.search("PS5", "Retro & Android").first { it.id == "retro.ps5-homebrew" }
        assertTrue(ps5.blocks.flatMap { it.lines }.any { it.contains("firmware", ignoreCase = true) })
        assertTrue(ps5.asPersonalText().contains("https://github.com/etaHEN/etaHEN"))
        assertTrue(StarterLibrary.search("Android 17", "Retro & Android").any { it.id == "retro.android-version-map" })
    }
}
