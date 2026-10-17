package ie.suric.suricapp.local

import android.content.Context

/** Canonical IDs are the existing section identifiers, never user-facing labels. */
object SectionVisibility {
    val sectionIds = listOf("Garden", "Pottery", "Shopping", "Bills", "DIY & Home", "Cooking", "Cleaning", "Retro & Android", "Notes")
    fun sanitize(stored: Set<String>): Set<String> = stored.intersect(sectionIds.toSet())
    fun filterInDefaultOrder<T>(items: List<T>, id: (T) -> String, visible: Set<String>): List<T> =
        items.filter { id(it) in sanitize(visible) }
}

class SectionVisibilityStore(context: Context) {
    private val preferences = context.getSharedPreferences("suric_preferences", Context.MODE_PRIVATE)

    @Synchronized
    fun visibleSections(): Set<String> {
        if (!preferences.contains(KEY)) return SectionVisibility.sectionIds.toSet()
        return SectionVisibility.sanitize(preferences.getStringSet(KEY, emptySet()).orEmpty())
    }

    @Synchronized
    fun saveVisibleSections(sections: Set<String>) {
        check(preferences.edit().putStringSet(KEY, SectionVisibility.sanitize(sections)).commit()) {
            "Could not save Home section visibility on this device."
        }
    }

    @Synchronized
    fun restoreDefaults() = saveVisibleSections(SectionVisibility.sectionIds.toSet())

    companion object { private const val KEY = "visible_home_sections_v1" }
}
