package ie.suric.suricapp.local

import android.content.Context
import org.json.JSONArray

/** Small device-local index for bundled reading; it never changes bundled or encrypted personal content. */
class OfflineReadingState(context: Context) {
    private val preferences = context.getSharedPreferences("offline_reading", Context.MODE_PRIVATE)

    fun favourites(): Set<String> = preferences.getStringSet(KEY_FAVOURITES, emptySet()).orEmpty().toSet()

    fun isFavourite(id: String): Boolean = id in favourites()

    fun setFavourite(id: String, saved: Boolean) {
        require(id.matches(Regex("(?:article|recipe):[a-z0-9-]{1,120}"))) { "Invalid offline content ID." }
        val updated = favourites().toMutableSet().apply {
            if (saved) add(id) else remove(id)
        }
        preferences.edit().putStringSet(KEY_FAVOURITES, updated).apply()
    }

    fun recentlyViewed(): List<String> = runCatching {
        val json = JSONArray(preferences.getString(KEY_RECENT, "[]"))
        (0 until json.length()).map(json::getString).filter(::isContentId).distinct().take(RECENT_LIMIT)
    }.getOrDefault(emptyList())

    fun markViewed(id: String) {
        require(isContentId(id)) { "Invalid offline content ID." }
        val updated = (listOf(id) + recentlyViewed().filterNot { it == id }).take(RECENT_LIMIT)
        preferences.edit().putString(KEY_RECENT, JSONArray(updated).toString()).apply()
    }

    private fun isContentId(id: String): Boolean = id.matches(Regex("(?:article|recipe):[a-z0-9-]{1,120}"))

    private companion object {
        const val KEY_FAVOURITES = "favourites"
        const val KEY_RECENT = "recent"
        const val RECENT_LIMIT = 20
    }
}
