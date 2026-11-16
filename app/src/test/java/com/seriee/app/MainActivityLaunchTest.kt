package com.seriee.app

import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import com.seriee.app.data.AppThemeMode
import com.seriee.app.data.AppThemeStyle
import com.seriee.app.data.MediaItem
import com.seriee.app.data.MediaType
import com.seriee.app.data.SerieeRepository
import com.seriee.app.data.WatchStatus
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "w412dp-h915dp-xxhdpi")
class MainActivityLaunchTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun coldStart_rendersWithoutCrash() {
        composeRule.waitForIdle()
    }

    private fun recreateFresh() {
        composeRule.runOnUiThread { composeRule.activity.viewModelStore.clear() }
        composeRule.activityRule.scenario.recreate()
        composeRule.waitForIdle()
    }

    @Test
    fun switchAllTabsAndOpenAddDialog_rendersWithoutCrash() {
        val context = RuntimeEnvironment.getApplication()
        val repo = SerieeRepository(context)
        repo.saveLastSeenVersionCode(BuildConfig.VERSION_CODE)
        repo.saveItems(
            listOf(
                MediaItem(
                    id = "item-1",
                    title = "Arcane",
                    type = MediaType.Series,
                    status = WatchStatus.Watching,
                    poster = "",
                    season = 1,
                    currentEp = 3,
                    totalEp = 9,
                    rating = 5,
                    tags = listOf("Action", "Sci-Fi")
                ),
                MediaItem(
                    id = "item-2",
                    title = "Dune",
                    type = MediaType.Movie,
                    status = WatchStatus.ComingSoon,
                    poster = "",
                    releaseDate = System.currentTimeMillis() + 86_400_000L
                )
            )
        )
        recreateFresh()

        // Open item detail sheet
        composeRule.onNodeWithTag("progress_list").performScrollToNode(hasText("Arcane"))
        composeRule.onAllNodesWithText("Arcane").onFirst().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithContentDescription(context.getString(R.string.close)).performClick()
        composeRule.waitForIdle()

        // Switch through all pager tabs
        composeRule.onNodeWithTag("nav_tab_library").performClick()
        composeRule.waitForIdle()

        // Toggle Library list view
        composeRule.onNodeWithContentDescription(context.getString(R.string.list)).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("nav_tab_insights").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("nav_tab_progress").performClick()
        composeRule.waitForIdle()

        // Open Add Media bottom sheet
        composeRule.onNodeWithTag("fab_add_button").performClick()
        composeRule.waitForIdle()
    }

    @Test
    fun allThemeStylesAndModes_renderWithoutCrash() {
        val context = RuntimeEnvironment.getApplication()
        val repo = SerieeRepository(context)
        repo.saveLastSeenVersionCode(BuildConfig.VERSION_CODE)
        repo.saveItems(
            listOf(
                MediaItem(
                    id = "t-1",
                    title = "Severance",
                    type = MediaType.Series,
                    status = WatchStatus.Watching,
                    poster = "",
                    season = 1,
                    currentEp = 4,
                    totalEp = 9
                )
            )
        )

        for (mode in listOf(AppThemeMode.Light, AppThemeMode.Dark)) {
            for (style in AppThemeStyle.entries) {
                repo.saveTheme(mode, style)
                recreateFresh()
            }
        }
    }

    @Test
    fun overflowMenuAndDialogs_renderWithoutCrash() {
        val context = RuntimeEnvironment.getApplication()
        val repo = SerieeRepository(context)
        repo.saveLastSeenVersionCode(BuildConfig.VERSION_CODE)
        recreateFresh()

        // Open overflow menu -> Backup & Restore
        composeRule.onNodeWithContentDescription(context.getString(R.string.more_options)).performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText(context.getString(R.string.backup_restore)).performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithContentDescription(context.getString(R.string.close)).performClick()
        composeRule.waitForIdle()

        // Open overflow menu -> Import
        composeRule.onNodeWithContentDescription(context.getString(R.string.more_options)).performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText(context.getString(R.string.import_collection)).performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithContentDescription(context.getString(R.string.close)).performClick()
        composeRule.waitForIdle()

        // Open overflow menu -> About
        composeRule.onNodeWithContentDescription(context.getString(R.string.more_options)).performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText(context.getString(R.string.about_title)).performClick()
        composeRule.waitForIdle()
    }

    @Test
    fun duplicateAndBlankIdsInRepository_areSanitizedBeforeLazyListComposition() {
        val context = RuntimeEnvironment.getApplication()
        val repo = SerieeRepository(context)
        repo.saveLastSeenVersionCode(BuildConfig.VERSION_CODE)

        val arr = JSONArray()
        arr.put(JSONObject().apply {
            put("id", "dup-id")
            put("title", "Show One")
            put("status", "Watching")
            put("totalEp", 0)
        })
        arr.put(JSONObject().apply {
            put("id", "dup-id")
            put("title", "Show Duplicate")
            put("status", "Watching")
        })
        arr.put(JSONObject().apply {
            put("id", "")
            put("title", "Blank Id Show")
            put("status", "Watching")
        })
        val payload = JSONObject().apply { put("items", arr) }.toString()
        val parsed = repo.importBackupJson(payload)
        assertEquals(2, parsed.items.size)
        assertTrue(parsed.items.all { it.id.isNotBlank() && it.totalEp >= 1 })

        repo.saveItems(parsed.items)
        recreateFresh()
        composeRule.onNodeWithTag("progress_list").performScrollToNode(hasText("Show One"))
        composeRule.onAllNodesWithText("Show One").onFirst().assertExists()
    }
}
