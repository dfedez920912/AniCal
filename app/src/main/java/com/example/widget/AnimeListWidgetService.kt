package com.example.widget

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import cu.daniel.anical.R
import com.example.data.OfflineAnimeData
import com.example.model.AnimeRelease
import java.time.LocalDate

class AnimeListWidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        return AnimeListWidgetFactory(applicationContext)
    }
}

class AnimeListWidgetFactory(private val context: Context) : RemoteViewsService.RemoteViewsFactory {
    private var animeList = listOf<AnimeRelease>()

    override fun onCreate() {
        loadData()
    }

    @SuppressLint("NewApi")
    override fun onDataSetChanged() {
        loadData()
    }

    @SuppressLint("NewApi")
    private fun loadData() {
        try {
            val today = LocalDate.now()
            val weekSchedule = OfflineAnimeData.getCuratedWeekSchedule(today)
            animeList = weekSchedule.filter { it.getAiringLocalDate() == today }
        } catch (e: Exception) {
            animeList = emptyList()
        }
    }

    override fun onDestroy() {
        animeList = emptyList()
    }

    override fun getCount(): Int = animeList.size

    @SuppressLint("NewApi")
    override fun getViewAt(position: Int): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.widget_anime_list_item)
        if (position in animeList.indices) {
            val anime = animeList[position]
            views.setTextViewText(R.id.item_anime_title, "${anime.displayTitle} (Ep ${anime.episode})")
            views.setTextViewText(R.id.item_anime_time, anime.getFormattedTime())

            val fillInIntent = Intent().apply {
                putExtra("anime_id", anime.id)
            }
            views.setOnClickFillInIntent(R.id.item_anime_title, fillInIntent)
        }
        return views
    }

    override fun getLoadingView(): RemoteViews? = null

    override fun getViewTypeCount(): Int = 1

    override fun getItemId(position: Int): Long = position.toLong()

    override fun hasStableIds(): Boolean = true
}
