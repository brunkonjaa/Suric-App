package ie.suric.suricapp.local

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.net.URI
import java.security.MessageDigest

/** Public reference bookmarks are kept separately from encrypted personal notes and records. */
data class ExternalLibraryResource(
    val id: String,
    val title: String,
    val url: String,
    val addedAtEpochMillis: Long
)

data class PersonalLibraryState(
    val favourites: Set<String> = emptySet(),
    val favouriteSavedAt: Map<String, Long> = emptyMap(),
    val recentlyViewedIds: List<String> = emptyList(),
    val externalResources: List<ExternalLibraryResource> = emptyList()
)

class PersonalLibraryStore(context: Context) {
    private val preferences = context.getSharedPreferences("suric_reference_library", Context.MODE_PRIVATE)

    @Synchronized
    fun load(): PersonalLibraryState {
        val raw = preferences.getString(KEY, null) ?: return PersonalLibraryState()
        return decode(raw)
    }

    @Synchronized
    fun toggleFavourite(id: String): PersonalLibraryState {
        require(id.isNotBlank())
        val state = load()
        val now = System.currentTimeMillis()
        val updated = if (id in state.favourites) state.copy(
            favourites = state.favourites - id,
            favouriteSavedAt = state.favouriteSavedAt - id
        ) else state.copy(
            favourites = state.favourites + id,
            favouriteSavedAt = state.favouriteSavedAt + (id to now)
        )
        save(updated)
        return updated
    }

    @Synchronized
    fun recordViewed(id: String): PersonalLibraryState {
        require(id.isNotBlank())
        val state = load()
        val updated = state.copy(recentlyViewedIds = (listOf(id) + state.recentlyViewedIds.filterNot { it == id }).take(MAX_RECENT))
        save(updated)
        return updated
    }

    /** Adds or returns a canonical HTTPS resource. Re-sharing an existing URL does not duplicate it. */
    @Synchronized
    fun addExternalResource(title: String?, rawUrl: String): Pair<ExternalLibraryResource, PersonalLibraryState> {
        val url = cleanHttpsUrl(rawUrl) ?: throw IllegalArgumentException("Share a complete HTTPS link to save it in My Library.")
        val state = load()
        val id = "external:${sha256(url)}"
        val existing = state.externalResources.firstOrNull { it.id == id }
        val resource = existing ?: ExternalLibraryResource(
            id = id,
            title = title?.trim()?.takeIf(String::isNotEmpty)?.take(MAX_TITLE)
                ?: "Shared link · ${URI(url).host.orEmpty().removePrefix("www.")}",
            url = url,
            addedAtEpochMillis = System.currentTimeMillis()
        )
        val updated = state.copy(
            favourites = state.favourites + id,
            favouriteSavedAt = state.favouriteSavedAt + (id to (state.favouriteSavedAt[id] ?: System.currentTimeMillis())),
            externalResources = if (existing != null) state.externalResources else listOf(resource) + state.externalResources
        )
        save(updated)
        return resource to updated
    }

    @Synchronized
    fun recordExternalViewed(id: String): PersonalLibraryState = recordViewed(id)

    @Synchronized
    fun removeExternalResource(id: String): PersonalLibraryState {
        val state = load()
        val updated = state.copy(
            favourites = state.favourites - id,
            favouriteSavedAt = state.favouriteSavedAt - id,
            recentlyViewedIds = state.recentlyViewedIds.filterNot { it == id },
            externalResources = state.externalResources.filterNot { it.id == id }
        )
        save(updated)
        return updated
    }

    @Synchronized
    private fun save(state: PersonalLibraryState) {
        val json = JSONObject()
            .put("version", 1)
            .put("favourites", JSONArray(state.favourites.sorted()))
            .put("favourite_saved_at", JSONObject().apply { state.favouriteSavedAt.forEach { (id, time) -> put(id, time) } })
            .put("recent", JSONArray(state.recentlyViewedIds))
            .put("external", JSONArray().apply {
                state.externalResources.forEach { resource ->
                    put(JSONObject().put("id", resource.id).put("title", resource.title).put("url", resource.url).put("added", resource.addedAtEpochMillis))
                }
            })
        check(preferences.edit().putString(KEY, json.toString()).commit()) { "Could not save My Library on this device." }
    }

    private fun decode(raw: String): PersonalLibraryState {
        val json = JSONObject(raw)
        require(json.optInt("version") == 1) { "My Library data has an unsupported version and was left unchanged." }
        val favourites = json.optJSONArray("favourites") ?: JSONArray()
        val savedAt = json.optJSONObject("favourite_saved_at") ?: JSONObject()
        val recent = json.optJSONArray("recent") ?: JSONArray()
        val external = json.optJSONArray("external") ?: JSONArray()
        val resources = (0 until external.length()).map { index ->
            val item = external.optJSONObject(index) ?: error("My Library contains an invalid resource and was left unchanged.")
            val url = cleanHttpsUrl(item.optString("url")) ?: error("My Library contains an invalid link and was left unchanged.")
            val id = item.optString("id")
            require(id == "external:${sha256(url)}") { "My Library contains an invalid resource ID and was left unchanged." }
            ExternalLibraryResource(id, item.optString("title").take(MAX_TITLE), url, item.optLong("added"))
        }
        val favouriteIds = (0 until favourites.length()).mapNotNull { favourites.optString(it).takeIf(String::isNotBlank) }.toSet()
        return PersonalLibraryState(
            favourites = favouriteIds,
            favouriteSavedAt = favouriteIds.associateWith { savedAt.optLong(it, 0L) },
            recentlyViewedIds = (0 until recent.length()).mapNotNull { recent.optString(it).takeIf(String::isNotBlank) }.distinct().take(MAX_RECENT),
            externalResources = resources.distinctBy { it.id }
        )
    }

    companion object {
        private const val KEY = "library_state_v1"
        private const val MAX_RECENT = 50
        private const val MAX_TITLE = 120

        fun cleanHttpsUrl(raw: String): String? {
            val value = raw.trim()
            if (value.isEmpty()) return null
            val uri = runCatching { URI(value) }.getOrNull() ?: return null
            if (!uri.scheme.equals("https", ignoreCase = true) || uri.host.isNullOrBlank() || uri.userInfo != null) return null
            return uri.normalize().toASCIIString()
        }

        private fun sha256(value: String): String = MessageDigest.getInstance("SHA-256")
            .digest(value.toByteArray(Charsets.UTF_8)).joinToString("") { "%02x".format(it) }
    }
}
