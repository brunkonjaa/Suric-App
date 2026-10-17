package ie.suric.suricapp.local

import android.content.ContentResolver
import android.provider.CalendarContract
import java.util.Calendar

data class LocalCalendarEvent(val title: String, val startsAtMillis: Long, val notes: String?)

/** Reads a short upcoming window only after the user grants calendar permission. */
object CalendarEvents {
    fun upcoming(resolver: ContentResolver, nowMillis: Long = System.currentTimeMillis()): List<LocalCalendarEvent> {
        val start = Calendar.getInstance().apply { timeInMillis = nowMillis; set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0) }.timeInMillis
        val end = Calendar.getInstance().apply { timeInMillis = start; add(Calendar.DAY_OF_YEAR, 90) }.timeInMillis
        val builder = CalendarContract.Instances.CONTENT_URI.buildUpon()
        android.content.ContentUris.appendId(builder, start)
        android.content.ContentUris.appendId(builder, end)
        val projection = arrayOf(CalendarContract.Instances.TITLE, CalendarContract.Instances.BEGIN, CalendarContract.Instances.DESCRIPTION)
        return resolver.query(builder.build(), projection, null, null, "${CalendarContract.Instances.BEGIN} ASC")?.use { cursor ->
            val titleColumn = cursor.getColumnIndex(CalendarContract.Instances.TITLE)
            val beginColumn = cursor.getColumnIndex(CalendarContract.Instances.BEGIN)
            val descriptionColumn = cursor.getColumnIndex(CalendarContract.Instances.DESCRIPTION)
            buildList {
                while (cursor.moveToNext() && size < 100) {
                    val title = cursor.getString(titleColumn)?.take(160)?.trim().orEmpty()
                    if (title.isNotBlank()) add(LocalCalendarEvent(title, cursor.getLong(beginColumn), cursor.getString(descriptionColumn)?.take(500)))
                }
            }
        }.orEmpty()
    }
}
