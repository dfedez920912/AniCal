package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.data.OfflineAnimeData
import com.example.data.local.AppDatabase
import com.example.model.AnimeRelease
import com.example.model.CountdownStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

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

        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val views = RemoteViews(context.packageName, R.layout.widget_anime_countdown)

                    val db = AppDatabase.getDatabase(context)
                    val favoriteIds = db.favoriteAnimeDao().getFavoriteIdsList().toSet()

                    val today = LocalDate.now()
                    val weekSchedule = OfflineAnimeData.getCuratedWeekSchedule(today)
                    val nowSeconds = System.currentTimeMillis() / 1000

                    // Find closest episode among favorites, or fallback to closest upcoming episode
                    val favoriteReleases = weekSchedule.filter { favoriteIds.contains(it.id) }

                    val nextAnime: AnimeRelease? = if (favoriteReleases.isNotEmpty()) {
                        // Look for closest upcoming favorite
                        val upcoming = favoriteReleases.filter { it.airingAtSeconds >= nowSeconds - 3600 }
                            .minByOrNull { it.airingAtSeconds }
                        upcoming ?: favoriteReleases.first()
                    } else {
                        // Fallback: closest upcoming anime in general
                        val upcoming = weekSchedule.filter { it.airingAtSeconds >= nowSeconds - 3600 }
                            .minByOrNull { it.airingAtSeconds }
                        upcoming ?: weekSchedule.firstOrNull()
                    }

                    if (nextAnime != null) {
                        val isFavorite = favoriteIds.contains(nextAnime.id)
                        val formattedTime = nextAnime.getFormattedTime()
                        val localDate = nextAnime.getAiringLocalDate()
                        val dayName = when {
                            localDate == today -> "Hoy"
                            localDate == today.plusDays(1) -> "Mañana"
                            else -> localDate.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale("es"))
                                .replaceFirstChar { it.uppercase() }
                        }

                        // Countdown calculation
                        val diff = nextAnime.airingAtSeconds - nowSeconds
                        val countdownString = when {
                            diff < -3600 -> "Emitido hoy"
                            diff <= 0 -> "¡Estrenando ahora!"
                            diff < 3600 -> "⏳ En ${(diff / 60).coerceAtLeast(1)}m"
                            diff < 86400 -> {
                                val h = diff / 3600
                                val m = (diff % 3600) / 60
                                "⏳ En ${h}h ${m}m"
                            }
                            else -> "⏳ En ${diff / 86400} días"
                        }

                        val statusLabel = when (nextAnime.getCountdownStatus()) {
                            is CountdownStatus.AiringNow -> "¡En emisión!"
                            is CountdownStatus.Aired -> "Emitido"
                            is CountdownStatus.Upcoming -> if (nextAnime.isSeriesPremiere) "Gran Estreno" else "Próximo estreno"
                        }

                        views.setTextViewText(R.id.widget_anime_title, nextAnime.displayTitle)
                        views.setTextViewText(
                            R.id.widget_anime_episode_time,
                            "Episodio ${nextAnime.episode} • $dayName a las $formattedTime"
                        )
                        views.setTextViewText(R.id.widget_countdown_text, countdownString)
                        views.setTextViewText(R.id.widget_status_text, statusLabel)

                        if (isFavorite) {
                            views.setTextViewText(R.id.widget_favorite_tag, "⭐ Favorito en seguimiento")
                            views.setTextColor(R.id.widget_favorite_tag, 0xFFFFB800.toInt())
                        } else {
                            views.setTextViewText(R.id.widget_favorite_tag, "Toca para seguir tus animes favoritos")
                            views.setTextColor(R.id.widget_favorite_tag, 0xFFA5A3C2.toInt())
                        }
                    } else {
                        views.setTextViewText(R.id.widget_anime_title, "AniCal - Calendario Anime")
                        views.setTextViewText(R.id.widget_anime_episode_time, "Abre la app para consultar los estrenos")
                        views.setTextViewText(R.id.widget_countdown_text, "AniCal")
                        views.setTextViewText(R.id.widget_favorite_tag, "Sin estrenos próximos")
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

                    appWidgetManager.updateAppWidget(appWidgetId, views)
                } catch (e: Exception) {
                    Log.e("AnimeCountdownWidget", "Error updating widget: ${e.message}")
                }
            }
        }
    }
}
