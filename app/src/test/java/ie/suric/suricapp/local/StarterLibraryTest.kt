package ie.suric.suricapp.local

import org.junit.Assert.*
import org.junit.Test

class StarterLibraryTest {
    @Test fun bundledCatalogueHasUniqueIdsAndExpectedCategoryCounts() {
        assertEquals(28, StarterLibrary.items.size)
        assertEquals(28, StarterLibrary.items.map { it.id }.toSet().size)
        assertEquals(mapOf("Gardening" to 6, "Cooking" to 6, "Cleaning" to 5, "DIY & Home" to 4, "Pottery" to 3, "Shopping" to 2, "Bills" to 2), StarterLibrary.items.groupingBy { it.category }.eachCount())
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
}
