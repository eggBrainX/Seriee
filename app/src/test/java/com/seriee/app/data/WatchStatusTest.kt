package com.seriee.app.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class WatchStatusTest {

    @Test
    fun `status names round-trip through persistence`() {
        WatchStatus.entries.forEach { status ->
            assertEquals(status, WatchStatus.valueOf(status.name))
        }
    }

    @Test
    fun `unknown persisted status falls back to watching`() {
        val parsed = runCatching { WatchStatus.valueOf("Paused") }
            .getOrDefault(WatchStatus.Watching)
        assertEquals(WatchStatus.Watching, parsed)
    }

    @Test
    fun `new statuses are present`() {
        assertNotNull(WatchStatus.valueOf("OnHold"))
        assertNotNull(WatchStatus.valueOf("Dropped"))
    }

    @Test
    fun `theme names round-trip and old names still load`() {
        AppTheme.entries.forEach { theme ->
            assertEquals(theme, AppTheme.valueOf(theme.name))
        }
        listOf("Dark", "Amoled", "Glass", "Light").forEach { old ->
            assertNotNull(AppTheme.valueOf(old))
        }
        assertNotNull(AppTheme.valueOf("FollowSystem"))
        assertNotNull(AppTheme.valueOf("LiquidGlass"))
        assertNotNull(AppTheme.valueOf("Custom"))
    }

    @Test
    fun `language lookup keeps old tags and adds system default`() {
        assertEquals(AppLanguage.English, AppLanguage.fromTag("en"))
        assertEquals(AppLanguage.Indonesian, AppLanguage.fromTag("in"))
        assertEquals(AppLanguage.System, AppLanguage.fromTag(""))
        assertEquals(AppLanguage.System, AppLanguage.fromTag(null))
    }
}
