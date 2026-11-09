package com.example.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class AnimeReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val animeTitle = intent.getStringExtra(AnimeNotificationHelper.EXTRA_ANIME_TITLE) ?: "Anime Favorito"
        val episode = intent.getIntExtra(AnimeNotificationHelper.EXTRA_EPISODE, 1)
        val airingTime = intent.getStringExtra(AnimeNotificationHelper.EXTRA_AIRING_TIME) ?: "18:00"
        val minutesBefore = intent.getIntExtra(AnimeNotificationHelper.EXTRA_MINUTES_BEFORE, 15)

        Log.d("AnimeReminderReceiver", "Alarm received for $animeTitle ep $episode at $airingTime")

        AnimeNotificationHelper.showImmediateNotification(
            context = context,
            animeTitle = animeTitle,
            episode = episode,
            airingTime = airingTime,
            minutesBefore = minutesBefore
        )
    }
}
