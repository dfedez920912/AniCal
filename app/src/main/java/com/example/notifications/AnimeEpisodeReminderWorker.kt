package com.example.notifications

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.model.AnimeRelease
import java.util.concurrent.TimeUnit

/**
 * WorkManager CoroutineWorker for delivering episode notifications reliably
 * even after app is closed, battery optimization, or process death.
 */
class AnimeEpisodeReminderWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val animeTitle = inputData.getString(KEY_ANIME_TITLE) ?: return Result.failure()
        val episode = inputData.getInt(KEY_EPISODE, 1)
        val airingTime = inputData.getString(KEY_AIRING_TIME) ?: ""
        val minutesBefore = inputData.getInt(KEY_MINUTES_BEFORE, 15)

        Log.d(TAG, "Executing notification work for anime: $animeTitle (Ep $episode)")

        AnimeNotificationHelper.showImmediateNotification(
            context = applicationContext,
            animeTitle = animeTitle,
            episode = episode,
            airingTime = airingTime,
            minutesBefore = minutesBefore
        )

        return Result.success()
    }

    companion object {
        private const val TAG = "AnimeEpisodeWorker"
        const val KEY_ANIME_ID = "key_anime_id"
        const val KEY_ANIME_TITLE = "key_anime_title"
        const val KEY_EPISODE = "key_episode"
        const val KEY_AIRING_TIME = "key_airing_time"
        const val KEY_MINUTES_BEFORE = "key_minutes_before"

        private fun getWorkManagerSafe(context: Context): WorkManager? {
            return try {
                WorkManager.getInstance(context)
            } catch (e: IllegalStateException) {
                try {
                    val config = androidx.work.Configuration.Builder()
                        .setMinimumLoggingLevel(android.util.Log.DEBUG)
                        .build()
                    WorkManager.initialize(context, config)
                    WorkManager.getInstance(context)
                } catch (ex: Exception) {
                    null
                }
            } catch (e: Exception) {
                null
            }
        }

        fun scheduleReminderWithWorkManager(
            context: Context,
            anime: AnimeRelease,
            minutesBefore: Int = 15
        ) {
            val workManager = getWorkManagerSafe(context) ?: return

            val triggerTimeMillis = (anime.airingAtSeconds * 1000) - (minutesBefore * 60 * 1000)
            val nowMillis = System.currentTimeMillis()

            val delayMillis = if (triggerTimeMillis <= nowMillis) {
                if (anime.airingAtSeconds * 1000 > nowMillis) {
                    3000L // Episode is airing very soon (within notification window)
                } else if (nowMillis - (anime.airingAtSeconds * 1000) < 15 * 60 * 1000) {
                    // Newly released in the last 15 minutes
                    1500L
                } else {
                    return // Episode has already aired
                }
            } else {
                triggerTimeMillis - nowMillis
            }

            val inputData = Data.Builder()
                .putInt(KEY_ANIME_ID, anime.id)
                .putString(KEY_ANIME_TITLE, anime.displayTitle)
                .putInt(KEY_EPISODE, anime.episode)
                .putString(KEY_AIRING_TIME, anime.getFormattedTime())
                .putInt(KEY_MINUTES_BEFORE, minutesBefore)
                .build()

            val workRequest = OneTimeWorkRequestBuilder<AnimeEpisodeReminderWorker>()
                .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
                .setInputData(inputData)
                .addTag(getWorkTag(anime.id))
                .addTag("anime_episode_reminder")
                .build()

            val uniqueWorkName = "anime_reminder_${anime.id}"
            workManager.enqueueUniqueWork(
                uniqueWorkName,
                ExistingWorkPolicy.REPLACE,
                workRequest
            )

            Log.d(TAG, "Enqueued WorkManager reminder for ${anime.displayTitle} with delay ${delayMillis}ms")
        }

        fun scheduleImmediateTest(context: Context, anime: AnimeRelease) {
            val workManager = getWorkManagerSafe(context) ?: return
            val inputData = Data.Builder()
                .putInt(KEY_ANIME_ID, anime.id)
                .putString(KEY_ANIME_TITLE, anime.displayTitle)
                .putInt(KEY_EPISODE, anime.episode)
                .putString(KEY_AIRING_TIME, anime.getFormattedTime())
                .putInt(KEY_MINUTES_BEFORE, 0)
                .build()

            val workRequest = OneTimeWorkRequestBuilder<AnimeEpisodeReminderWorker>()
                .setInitialDelay(1500, TimeUnit.MILLISECONDS)
                .setInputData(inputData)
                .addTag("anime_immediate_test")
                .build()

            workManager.enqueue(workRequest)
            Log.d(TAG, "Enqueued immediate test WorkManager notification for ${anime.displayTitle}")
        }

        fun cancelReminderWithWorkManager(context: Context, animeId: Int) {
            val workManager = getWorkManagerSafe(context) ?: return
            workManager.cancelUniqueWork("anime_reminder_$animeId")
            Log.d(TAG, "Cancelled WorkManager reminder for animeId=$animeId")
        }

        fun getWorkTag(animeId: Int): String = "anime_work_tag_$animeId"
    }
}
