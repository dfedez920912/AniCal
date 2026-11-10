package com.example.widget

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.widget.RemoteViews
import androidx.annotation.RequiresApi
import com.example.MainActivity
import cu.daniel.anical.R
import com.example.data.OfflineAnimeData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate

@SuppressLint("NewApi")
class AnimeCountdownWidget : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_REFRESH_WIDGET) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val thisWidget = ComponentName(context, AnimeCountdownWidget::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)
            for (appWidgetId in appWidgetIds) {
                updateAppWidget(context, appWidgetManager, appWidgetId)
            }
        }
    }

    companion object {
        const val ACTION_REFRESH_WIDGET = "com.example.widget.ACTION_REFRESH_WIDGET"

        fun sendUpdateBroadcast(context: Context) {
            val intent = Intent(context, AnimeCountdownWidget::class.java).apply {
                action = ACTION_REFRESH_WIDGET
                `package` = context.packageName
            }
            context.sendBroadcast(intent)
        }

        @RequiresApi(Build.VERSION_CODES.O)
        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val options = appWidgetManager.getAppWidgetOptions(appWidgetId)
                    val minWidth = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 250)
                    val isSmall = minWidth < 180

                    val layoutId = if (isSmall) R.layout.widget_anime_countdown_small else R.layout.widget_anime_countdown
                    val views = RemoteViews(context.packageName, layoutId)

                    val today = LocalDate.now()
                    val weekSchedule = try {
                        OfflineAnimeData.getCuratedWeekSchedule(today)
                    } catch (e: Exception) {
                        emptyList()
                    }
                    val todayReleases = weekSchedule.filter { it.getAiringLocalDate() == today }

                    if (isSmall) {
                        val countText = todayReleases.size.toString()
                        views.setTextViewText(R.id.widget_small_count_text, countText)
                    } else {
                        if (todayReleases.isNotEmpty()) {
                            val listBuilder = StringBuilder()
                            todayReleases.take(5).forEach { anime ->
                                val time = anime.getFormattedTime()
                                listBuilder.append("• ${anime.displayTitle} (Ep ${anime.episode}) - $time\n")
                            }
                            if (todayReleases.size > 5) {
                                listBuilder.append("... y ${todayReleases.size - 5} más")
                            }
                            views.setTextViewText(R.id.widget_anime_list_text, listBuilder.toString().trimEnd())
                        } else {
                            views.setTextViewText(R.id.widget_anime_list_text, "• No hay estrenos programados para hoy")
                        }
                    }

                    // Root click intent to launch MainActivity
                    val appIntent = Intent(context, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    }
                    val pendingAppIntent = PendingIntent.getActivity(
                        context,
                        0,
                        appIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    views.setOnClickPendingIntent(R.id.widget_root_container, pendingAppIntent)

                    if (!isSmall) {
                        // Refresh button click intent
                        val refreshIntent = Intent(context, AnimeCountdownWidget::class.java).apply {
                            action = ACTION_REFRESH_WIDGET
                        }
                        val pendingRefreshIntent = PendingIntent.getBroadcast(
                            context,
                            1,
                            refreshIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                        )
                        views.setOnClickPendingIntent(R.id.widget_refresh_button, pendingRefreshIntent)
                    }

                    appWidgetManager.updateAppWidget(appWidgetId, views)
                } catch (e: Exception) {
                    Log.e("AnimeCountdownWidget", "Error updating widget: ${e.message}")
                }
            }
        }
    }
}
