package com.seriee.app

import android.app.Application
import android.content.Intent
import android.os.Looper
import android.os.Process
import com.seriee.app.debug.AppLogger
import kotlin.system.exitProcess

class SerieeApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AppLogger.init(this)
        installUncaughtExceptionHandler()
        AppLogger.i("App", "SerieeApplication initialized (v${BuildConfig.VERSION_NAME})")
    }

    private fun installUncaughtExceptionHandler() {
        val previousHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            val isMainThread = thread === Looper.getMainLooper().thread
            runCatching {
                AppLogger.recordCrash(
                    threadName = thread.name,
                    throwable = throwable,
                    commitSync = isMainThread,
                )
            }

            if (isMainThread) {
                // On main UI thread exceptions the Looper can no longer pump frames in the broken task,
                // so relaunch MainActivity in a clean task to immediately present the user-friendly error dialog
                // instead of an abrupt OS force-close popup.
                val launched = runCatching {
                    val intent = Intent(this, MainActivity::class.java).apply {
                        addFlags(
                            Intent.FLAG_ACTIVITY_NEW_TASK or
                                Intent.FLAG_ACTIVITY_CLEAR_TASK or
                                Intent.FLAG_ACTIVITY_NO_ANIMATION
                        )
                        putExtra(EXTRA_SHOW_CRASH_DIALOG, true)
                    }
                    startActivity(intent)
                }.isSuccess

                if (launched) {
                    Process.killProcess(Process.myPid())
                    exitProcess(1)
                } else {
                    previousHandler?.uncaughtException(thread, throwable)
                }
            }
            // Background thread uncaught exceptions are captured in AppLogger.pendingCrash
            // so the active UI displays the user-friendly CrashErrorDialog without force closing.
        }
    }

    companion object {
        const val EXTRA_SHOW_CRASH_DIALOG = "com.seriee.app.EXTRA_SHOW_CRASH_DIALOG"
    }
}
