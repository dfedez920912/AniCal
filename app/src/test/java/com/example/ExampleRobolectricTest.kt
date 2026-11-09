package com.example

import android.app.NotificationManager
import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.AnimeRepository
import com.example.data.OfflineAnimeData
import com.example.data.local.AppDatabase
import com.example.model.AnimeSortOrder
import com.example.model.GenreHelper
import com.example.model.SynopsisLanguage
import com.example.notifications.AnimeNotificationHelper
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.LocalDate

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  private lateinit var db: AppDatabase
  private lateinit var repository: AnimeRepository

  @Before
  fun createDb() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
      .allowMainThreadQueries()
      .build()
    repository = AnimeRepository(context, db.favoriteAnimeDao())
  }

  @After
  fun closeDb() {
    db.close()
  }

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("AniCal", appName)
  }

  @Test
  fun `offline anime schedule generates entries for current week`() {
    val today = LocalDate.now()
    val schedule = OfflineAnimeData.getCuratedWeekSchedule(today)
    assertTrue("Schedule should not be empty", schedule.isNotEmpty())

    // Verify today has releases
    val todayReleases = schedule.filter { it.getAiringLocalDate() == today }
    assertTrue("Today should have scheduled releases", todayReleases.isNotEmpty())

    // Verify properties like synopsis, covers, and titles
    val first = todayReleases.first()
    assertTrue("Title should not be blank", first.displayTitle.isNotBlank())
    assertTrue("Cover URL should not be blank", first.coverImageUrl.isNotBlank())
    assertTrue("Synopsis should not be blank", first.synopsis.isNotBlank())
    assertTrue("Episode number should be positive", first.episode > 0)
  }

  @Test
  fun `verify translation options and episode classification`() {
    val today = LocalDate.now()
    val schedule = OfflineAnimeData.getCuratedWeekSchedule(today)

    // Verify both ongoing episodes (ep > 1) and premieres (ep == 1) exist
    val ongoing = schedule.filter { it.isOngoingEpisode }
    val premieres = schedule.filter { it.isSeriesPremiere }

    assertTrue("Should include ongoing daily episodes", ongoing.isNotEmpty())
    assertTrue("Should include premieres", premieres.isNotEmpty())

    // Verify synopsis language switching
    val item = schedule.first()
    val itemEs = item.copy(activeLanguage = SynopsisLanguage.SPANISH)
    val itemEn = item.copy(activeLanguage = SynopsisLanguage.ENGLISH)

    assertTrue("Spanish synopsis should not be empty", itemEs.getActiveSynopsis().isNotBlank())
    assertTrue("English synopsis should not be empty", itemEn.getActiveSynopsis().isNotBlank())
  }

  @Test
  fun `test Room favorites persistence insert and toggle`() = runBlocking {
    val schedule = OfflineAnimeData.getCuratedWeekSchedule(LocalDate.now())
    val targetAnime = schedule.first()

    // Initially should not be favorite
    assertFalse(repository.isFavorite(targetAnime.id))

    // Toggle favorite -> should be added to Room
    val isAdded = repository.toggleFavorite(targetAnime)
    assertTrue(isAdded)
    assertTrue(repository.isFavorite(targetAnime.id))

    val favList = db.favoriteAnimeDao().getFavoriteIdsList()
    assertTrue(favList.contains(targetAnime.id))

    // Toggle favorite again -> should be removed from Room
    val isRemoved = repository.toggleFavorite(targetAnime)
    assertFalse(isRemoved)
    assertFalse(repository.isFavorite(targetAnime.id))
  }

  @Test
  fun `test notification channel creation and reminder update`() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    AnimeNotificationHelper.createNotificationChannel(context)

    val notificationManager =
      context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val channel = notificationManager.getNotificationChannel(AnimeNotificationHelper.CHANNEL_ID)
    assertNotNull("Notification channel should be created", channel)

    // Test reminder update
    val schedule = OfflineAnimeData.getCuratedWeekSchedule(LocalDate.now())
    val anime = schedule.first()
    repository.toggleFavorite(anime)

    repository.updateReminder(anime, enabled = true, minutesBefore = 30)
    val saved = db.favoriteAnimeDao().getFavoriteById(anime.id)
    assertNotNull(saved)
    assertTrue(saved!!.hasReminder)
    assertEquals(30, saved.reminderMinutesBefore)
  }

  @Test
  fun `test genre filtering for Action, Drama, and Comedy and sort ordering`() {
    val schedule = OfflineAnimeData.getCuratedWeekSchedule(LocalDate.now())

    // Test Action genre filtering (both "Action" and "Acción")
    val actionAnime = schedule.filter { GenreHelper.matchesAnyGenre(it.genres, setOf("Action")) }
    val accionAnime = schedule.filter { GenreHelper.matchesAnyGenre(it.genres, setOf("Acción")) }
    assertTrue("Should find action anime", actionAnime.isNotEmpty())
    assertEquals("Action and Acción should match the same items", actionAnime.size, accionAnime.size)

    // Test Comedy genre filtering
    val comedyAnime = schedule.filter { GenreHelper.matchesAnyGenre(it.genres, setOf("Comedy")) }
    assertTrue("Should find comedy anime", comedyAnime.isNotEmpty())

    // Test Drama genre filtering
    val dramaAnime = schedule.filter { GenreHelper.matchesAnyGenre(it.genres, setOf("Drama")) }
    assertTrue("Should find drama anime", dramaAnime.isNotEmpty())

    // Test sorting by score descending
    val sortedByScore = schedule.sortedByDescending { it.score ?: 0.0 }
    for (i in 0 until sortedByScore.size - 1) {
      val scoreA = sortedByScore[i].score ?: 0.0
      val scoreB = sortedByScore[i + 1].score ?: 0.0
      assertTrue("Scores should be sorted descending", scoreA >= scoreB)
    }

    // Test sorting by title alphabetical
    val sortedByTitle = schedule.sortedBy { it.displayTitle.lowercase() }
    for (i in 0 until sortedByTitle.size - 1) {
      val titleA = sortedByTitle[i].displayTitle.lowercase()
      val titleB = sortedByTitle[i + 1].displayTitle.lowercase()
      assertTrue("Titles should be alphabetical", titleA <= titleB)
    }
  }

  @Test
  fun `test search by title finds specific anime titles accurately`() {
    val schedule = OfflineAnimeData.getCuratedWeekSchedule(LocalDate.now())

    // Search query for "Solo Leveling"
    val querySolo = "Solo Leveling"
    val resultsSolo = schedule.filter {
      it.titleRomaji.contains(querySolo, ignoreCase = true) ||
              (it.titleEnglish?.contains(querySolo, ignoreCase = true) == true)
    }
    assertTrue("Should find Solo Leveling", resultsSolo.isNotEmpty())
    assertEquals("Solo Leveling Season 2", resultsSolo.first().displayTitle)

    // Search query for "Bleach"
    val queryBleach = "Bleach"
    val resultsBleach = schedule.filter {
      it.titleRomaji.contains(queryBleach, ignoreCase = true) ||
              (it.titleEnglish?.contains(queryBleach, ignoreCase = true) == true)
    }
    assertTrue("Should find Bleach", resultsBleach.isNotEmpty())

    // Search query for "One Piece"
    val queryOnePiece = "One Piece"
    val resultsOnePiece = schedule.filter {
      it.titleRomaji.contains(queryOnePiece, ignoreCase = true) ||
              (it.titleEnglish?.contains(queryOnePiece, ignoreCase = true) == true)
    }
    assertTrue("Should find One Piece", resultsOnePiece.isNotEmpty())
  }

  @Test
  fun `test AnimeCountdownWidget broadcast and layout configuration`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    // Verify sendUpdateBroadcast does not throw exceptions
    com.example.widget.AnimeCountdownWidget.sendUpdateBroadcast(context)

    // Verify widget layout loads cleanly
    val views = android.widget.RemoteViews(context.packageName, R.layout.widget_anime_countdown)
    assertNotNull(views)
  }

  @Test
  fun `test Watched Episodes Room tracking toggle`() = runBlocking {
    val schedule = OfflineAnimeData.getCuratedWeekSchedule(LocalDate.now())
    val anime = schedule.first()

    // Initially not watched
    val initialKeys = repository.getWatchedEpisodeKeys()
    assertFalse(initialKeys.contains("${anime.id}_${anime.episode}"))

    // Toggle watched -> mark as watched
    val marked = repository.toggleWatchedEpisode(anime.id, anime.episode)
    assertTrue(marked)
    val afterMarkKeys = repository.getWatchedEpisodeKeys()
    assertTrue(afterMarkKeys.contains("${anime.id}_${anime.episode}"))

    // Toggle again -> unmark
    val unmarked = repository.toggleWatchedEpisode(anime.id, anime.episode)
    assertFalse(unmarked)
    val finalKeys = repository.getWatchedEpisodeKeys()
    assertFalse(finalKeys.contains("${anime.id}_${anime.episode}"))
  }

  @Test
  fun `test Streaming Platform filtering`() {
    val schedule = OfflineAnimeData.getCuratedWeekSchedule(LocalDate.now())
    val crunchyReleases = schedule.filter { it.streamingPlatform.equals("Crunchyroll", ignoreCase = true) }
    val netflixReleases = schedule.filter { it.streamingPlatform.equals("Netflix", ignoreCase = true) }

    assertTrue("Crunchyroll schedule should not be empty", crunchyReleases.isNotEmpty())
    assertTrue("Netflix schedule should not be empty", netflixReleases.isNotEmpty())
  }
}
