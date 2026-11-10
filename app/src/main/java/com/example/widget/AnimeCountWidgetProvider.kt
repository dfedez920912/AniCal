package com.example.widget

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.MainActivity
import cu.daniel.anical.R
import com.example.data.OfflineAnimeData
import java.time.LocalDate

class AnimeCountWidgetProvider : AppWidgetProvider() {
    @SuppressLint("NewApi")
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            try {
                val views = RemoteViews(context.packageName, R.layout.widget_anime_count_1x1)
                val today = LocalDate.now()
                val schedule = try { OfflineAnimeData.getCuratedWeekSchedule(today) } catch (e: Exception) { emptyList() }
                val count = schedule.filter { it.getAiringLocalDate() == today }.size

                views.setTextViewText(R.id.widget_count_text, count.toString())

                val intent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
                views.setOnClickPendingIntent(R.id.widget_count_root, pendingIntent)

                appWidgetManager.updateAppWidget(appWidgetId, views)
            } catch (e: Exception) {
                // ignore
            }
        }
    }
}
