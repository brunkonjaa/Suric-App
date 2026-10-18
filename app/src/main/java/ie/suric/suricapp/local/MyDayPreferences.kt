package ie.suric.suricapp.local

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class MyDayPreferences(
    val visibleSections: Set<String> = SECTIONS.toSet(),
    val order: List<String> = SECTIONS
) {
    companion object { val SECTIONS = listOf("tasks", "projects", "bills") }
}

/** Dashboard layout only; personal records remain in the encrypted entry store. */
class MyDayPreferencesStore(context: Context) {
    private val preferences = context.getSharedPreferences("suric_my_day", Context.MODE_PRIVATE)

    @Synchronized
    fun load(): MyDayPreferences {
        val raw = preferences.getString(KEY, null) ?: return MyDayPreferences()
        val json = runCatching { JSONObject(raw) }.getOrNull() ?: return MyDayPreferences()
        val visible = json.optJSONArray("visible")?.let { array -> (0 until array.length()).map { array.optString(it) }.toSet() }.orEmpty()
        val order = json.optJSONArray("order")?.let { array -> (0 until array.length()).map { array.optString(it) } }.orEmpty()
        return MyDayPreferences(
            visibleSections = visible.intersect(MyDayPreferences.SECTIONS.toSet()),
            order = (order.filter { it in MyDayPreferences.SECTIONS }.distinct() + MyDayPreferences.SECTIONS.filterNot { it in order }).take(MyDayPreferences.SECTIONS.size)
        )
    }

    @Synchronized
    fun save(value: MyDayPreferences) {
        val order = (value.order.filter { it in MyDayPreferences.SECTIONS }.distinct() + MyDayPreferences.SECTIONS.filterNot { it in value.order }).take(MyDayPreferences.SECTIONS.size)
        val visible = value.visibleSections.intersect(MyDayPreferences.SECTIONS.toSet())
        val json = JSONObject().put("visible", JSONArray(MyDayPreferences.SECTIONS.filter { it in visible })).put("order", JSONArray(order))
        check(preferences.edit().putString(KEY, json.toString()).commit()) { "Could not save My Day layout on this device." }
    }

    private companion object { const val KEY = "layout_v1" }
}
