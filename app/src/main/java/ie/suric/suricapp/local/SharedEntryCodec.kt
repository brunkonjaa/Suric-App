package ie.suric.suricapp.local

import org.json.JSONObject

data class SharedEntryCopy(val title: String, val section: String, val body: String)

/** A portable, explicitly user-shared text envelope. It never contains tokens or local IDs. */
object SharedEntryCodec {
    private const val PREFIX = "SURIC_COPY_V1\n"

    fun encode(entry: PersonalEntry): String {
        require(entry.title.length <= 200 && entry.body.length <= 100_000) { "This item is too large to share as a text copy." }
        require(entry.section in PersonalEntryStore.SECTIONS) { "This item belongs to an unknown Suric section." }
        return PREFIX + JSONObject()
            .put("version", 1)
            .put("title", entry.title)
            .put("section", entry.section)
            .put("body", entry.body)
            .toString()
    }

    fun decodeOrNull(text: String): SharedEntryCopy? {
        if (!text.startsWith(PREFIX)) return null
        val payload = JSONObject(text.removePrefix(PREFIX))
        require(payload.optInt("version") == 1) { "This shared Suric copy uses an unsupported version." }
        val section = payload.optString("section")
        require(section in PersonalEntryStore.SECTIONS) { "This shared Suric copy has an unknown section." }
        val title = payload.optString("title").trim().take(200)
        val body = payload.optString("body").take(100_000)
        require(title.isNotBlank() || body.isNotBlank()) { "This shared Suric copy is empty." }
        return SharedEntryCopy(title, section, body)
    }

    fun isEnvelope(text: String): Boolean = text.startsWith(PREFIX)
}
