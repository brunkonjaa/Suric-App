package ie.suric.suricapp.local

import org.junit.Assert.*
import org.junit.Test

class SectionVisibilityTest {
    @Test fun defaultsContainEveryCanonicalSection() = assertEquals(9, SectionVisibility.sanitize(SectionVisibility.sectionIds.toSet()).size)

    @Test fun filterReflowsByKeepingOnlyVisibleItemsInOriginalOrder() {
        val cards = listOf("Garden", "Pottery", "Shopping", "Bills")
        assertEquals(listOf("Garden", "Bills"), SectionVisibility.filterInDefaultOrder(cards, { it }, setOf("Bills", "Garden", "obsolete")))
    }

    @Test fun allHiddenIsValidAndUnknownIdsAreDiscarded() {
        assertTrue(SectionVisibility.filterInDefaultOrder(SectionVisibility.sectionIds, { it }, emptySet()).isEmpty())
        assertEquals(setOf("Notes"), SectionVisibility.sanitize(setOf("Notes", "future-section")))
    }
}
