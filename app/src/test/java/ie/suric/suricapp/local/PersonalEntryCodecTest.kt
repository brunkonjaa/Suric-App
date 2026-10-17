package ie.suric.suricapp.local

import org.junit.Assert.assertEquals
import org.junit.Test

class PersonalEntryCodecTest {
    @Test
    fun encodeDecodePreservesNotesAndCompletionState() {
        val entries = listOf(
            PersonalEntry("one", "Shopping", "Rosemary compost", "One bag", 42L, completed = true),
            PersonalEntry("two", "Garden", "Water seedlings", "Before noon", 84L)
        )
        assertEquals(entries, PersonalEntryStore.decode(PersonalEntryStore.encode(entries)))
    }

    @Test
    fun oldEntriesWithoutCompletionRemainOpen() {
        val oldJson = """[{"id":"old","section":"Bills","title":"Electricity","body":"Due Friday","updated":12}]"""
        val decoded = PersonalEntryStore.decode(oldJson).single()
        assertEquals(false, decoded.completed)
        assertEquals(null, decoded.sourceUrl)
        assertEquals(null, decoded.sourceCheckedAt)
    }

    @Test
    fun shoppingSourceDetailsSurviveEncoding() {
        val entry = PersonalEntry("one", "Shopping", "Kettle", "€39.99, 2 year warranty", 42L,
            sourceUrl = "https://shop.example/kettle", sourceCheckedAt = "17 October")
        assertEquals(entry, PersonalEntryStore.decode(PersonalEntryStore.encode(listOf(entry))).single())
    }

    @Test(expected = Exception::class)
    fun malformedJsonIsReportedInsteadOfBecomingAnEmptyList() {
        PersonalEntryStore.decode("not-json")
    }

    @Test(expected = IllegalArgumentException::class)
    fun unknownSectionCannotEnterStoredData() {
        PersonalEntryStore.decode("""[{"id":"bad","section":"Unknown","title":"x","body":"","updated":1}]""")
    }
}
