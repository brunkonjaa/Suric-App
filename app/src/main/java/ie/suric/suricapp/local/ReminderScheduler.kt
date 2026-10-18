package ie.suric.suricapp.local

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import ie.suric.suricapp.MainActivity
import ie.suric.suricapp.R
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

/** User-opt-in, local date reminders. Uses inexact alarms so no exact-alarm access is needed. */
object ReminderScheduler {
    const val CHANNEL_ID = "suric_due_dates"
    private const val ACTION_REMINDER = "ie.suric.suricapp.action.DUE_REMINDER"

    fun scheduleAll(context: Context, entries: List<PersonalEntry>) {
        entries.forEach { schedule(context, it) }
    }

    fun schedule(context: Context, entry: PersonalEntry) {
        cancel(context, entry.id)
        if (!entry.reminderEnabled || entry.completed || entry.dueDate.isNullOrBlank()) return
        val due = runCatching { LocalDate.parse(entry.dueDate) }.getOrNull() ?: return
        if (due.isBefore(LocalDate.now())) return
        val at = due.atTime(LocalTime.of(9, 0)).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            .let { if (it <= System.currentTimeMillis() && due == LocalDate.now()) System.currentTimeMillis() + 60_000 else it }
        val alarm = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarm.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pendingIntent(context, entry))
    }

    fun cancel(context: Context, id: String) {
        val intent = Intent(context, ReminderReceiver::class.java).setAction(ACTION_REMINDER).setData(Uri.parse("suric://reminder/$id"))
        val pending = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE)
        if (pending != null) {
            (context.getSystemService(Context.ALARM_SERVICE) as AlarmManager).cancel(pending)
            pending.cancel()
        }
    }

    private fun pendingIntent(context: Context, entry: PersonalEntry): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java)
            .setAction(ACTION_REMINDER)
            .setData(Uri.parse("suric://reminder/${entry.id}"))
            .putExtra("entry_id", entry.id)
            .putExtra("title", entry.title)
            .putExtra("section", entry.section)
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }

    internal fun show(context: Context, id: String, title: String, section: String) {
        if (Build.VERSION.SDK_INT >= 33 && context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) return
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= 26 && manager.getNotificationChannel(CHANNEL_ID) == null) {
            manager.createNotificationChannel(NotificationChannel(CHANNEL_ID, "Suric reminders", NotificationManager.IMPORTANCE_DEFAULT))
        }
        val openApp = PendingIntent.getActivity(context, id.hashCode(), Intent(context, MainActivity::class.java), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_reminder)
            .setContentTitle(title.ifBlank { "Suric reminder" })
            .setContentText("Due today · $section")
            .setContentIntent(openApp)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .build()
        runCatching { NotificationManagerCompat.from(context).notify(id.hashCode(), notification) }
    }
}

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != "ie.suric.suricapp.action.DUE_REMINDER") return
        ReminderScheduler.show(context, intent.getStringExtra("entry_id").orEmpty(), intent.getStringExtra("title").orEmpty(), intent.getStringExtra("section").orEmpty())
    }
}

class ReminderBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action !in setOf(Intent.ACTION_BOOT_COMPLETED, Intent.ACTION_TIME_CHANGED, Intent.ACTION_TIMEZONE_CHANGED)) return
        val pending = goAsync()
        Thread {
            try { ReminderScheduler.scheduleAll(context, PersonalEntryStore(context).allEntries()) }
            finally { pending.finish() }
        }.start()
    }
}
