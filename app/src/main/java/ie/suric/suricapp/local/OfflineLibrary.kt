package ie.suric.suricapp.local

import android.content.Context
import org.json.JSONObject

/** Versioned, bundled knowledge. It is separate from encrypted personal copies. */
data class OfflineArticle(
    val id: String,
    val section: String,
    val title: String,
    val summary: String,
    val revision: Int,
    val tags: List<String>,
    val body: String
) {
    val searchableText: String get() = "$section $title $summary ${tags.joinToString(" ")} $body"
}

object OfflineLibrary {
    fun load(context: Context): List<OfflineArticle> {
        val root = context.assets.open("offline-library.json").bufferedReader().use { JSONObject(it.readText()) }
        require(root.optInt("version") == 1) { "This offline library version is not supported." }
        val expansion = context.assets.open("offline-library-expansion.json").bufferedReader().use { JSONObject(it.readText()) }
        require(expansion.optInt("version") == 1) { "This offline library expansion is not supported." }
        return sequenceOf(root, expansion).flatMap { source ->
            val items = source.getJSONArray("articles")
            (0 until items.length()).asSequence().map { index ->
                val item = items.getJSONObject(index)
                OfflineArticle(
                    id = item.getString("id"),
                    section = item.getString("section"),
                    title = item.getString("title"),
                    summary = item.getString("summary"),
                    revision = item.getInt("revision"),
                    tags = item.getJSONArray("tags").let { tags -> (0 until tags.length()).map(tags::getString) },
                    body = item.getString("body")
                )
            }
        }.toList().also { articles ->
            require(articles.map { it.id }.distinct().size == articles.size) { "Offline library contains duplicate IDs." }
            require(articles.all { it.id.isNotBlank() && it.revision > 0 && it.body.isNotBlank() }) { "Offline library contains an incomplete article." }
        }
    }
}
