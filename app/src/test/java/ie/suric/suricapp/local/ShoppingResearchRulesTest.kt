package ie.suric.suricapp.local

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ShoppingResearchRulesTest {
    @Test
    fun blankSourceLinkIsOmittedAndHttpsLinkIsNormalized() {
        assertEquals(null, ShoppingResearchRules.cleanSourceUrl("  "))
        assertEquals("https://shop.example/item", ShoppingResearchRules.cleanSourceUrl(" https://shop.example/item "))
    }

    @Test
    fun insecureOrCredentialBearingLinksAreRejected() {
        listOf("http://shop.example/item", "https:///missing-host", "https://user:pass@shop.example/item").forEach { url ->
            val result = runCatching { ShoppingResearchRules.cleanSourceUrl(url) }
            assertTrue("Expected rejection for $url", result.isFailure)
        }
    }

    @Test
    fun comparisonPromptNamesLimitsAndSuppliedEvidence() {
        val entry = PersonalEntry("one", "Shopping", "Kettle", "€39.99 delivery included", 42L,
            sourceUrl = "https://shop.example/kettle", sourceCheckedAt = "17 October")
        val prompt = ShoppingResearchRules.comparisonPrompt(listOf(entry))
        assertTrue(prompt.contains("https://shop.example/kettle"))
        assertTrue(prompt.contains("17 October"))
        assertTrue(prompt.contains("€39.99 delivery included"))
        assertTrue(prompt.contains("cannot open or verify"))
        assertTrue(prompt.contains("current price and availability are unverified"))
    }
}
