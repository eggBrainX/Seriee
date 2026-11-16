package com.seriee.app

import android.app.Application
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import com.seriee.app.data.PosterStore
import com.seriee.app.debug.AppLogger
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class AppLoggerAndCrashTest {
    private lateinit var app: Application

    @Before
    fun setup() {
        app = ApplicationProvider.getApplicationContext()
        AppLogger.init(app)
        AppLogger.clearLogs()
        AppLogger.dismissPendingCrash()
    }

    @Test
    fun crashCapture_tracksStreakAndFormatsLogs() {
        val first = AppLogger.recordCrash("main", IllegalStateException("Boom 1"), commitSync = true)
        assertEquals(1, first.consecutiveCount)
        assertEquals("IllegalStateException", first.exceptionType)
        assertTrue(first.stackTrace.contains("Boom 1"))

        val second = AppLogger.recordCrash("worker", RuntimeException("Boom 2"), commitSync = true)
        assertEquals(2, second.consecutiveCount)

        val logText = AppLogger.formatAllLogs()
        assertTrue(logText.contains("Boom 1"))
        assertTrue(logText.contains("Boom 2"))

        val shareFile = AppLogger.writeShareableLogFile(app)
        assertNotNull(shareFile)
        assertTrue(shareFile!!.exists())
        assertTrue(shareFile.readText().contains("Boom 2"))

        AppLogger.dismissPendingCrash()
        assertNull(AppLogger.pendingCrash.value)

        val afterReset = AppLogger.recordCrash("main", IllegalArgumentException("Boom 3"), commitSync = true)
        assertEquals(1, afterReset.consecutiveCount)
    }

    @Test
    fun posterStorePruneUnused_removesOrphanedFiles() {
        val dir = File(app.filesDir, "posters").apply { mkdirs() }
        val active = File(dir, "keep.jpg").apply { writeText("ok") }
        val stale = File(dir, "orphan.jpg").apply { writeText("old") }

        PosterStore.pruneUnused(app, setOf(active.absolutePath))

        assertTrue(active.exists())
        assertTrue(!stale.exists())
    }

    @Test
    fun mainActivity_launchesCleanly() {
        val controller = Robolectric.buildActivity(MainActivity::class.java, Intent(app, MainActivity::class.java))
        controller.setup()
        assertNotNull(controller.get())
    }
}
