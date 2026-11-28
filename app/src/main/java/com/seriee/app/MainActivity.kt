package com.seriee.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.ContextCompat
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.seriee.app.data.SerieeRepository
import com.seriee.app.ui.CustomThemeColors
import com.seriee.app.ui.SerieeApp
import com.seriee.app.ui.SerieeSplash
import com.seriee.app.ui.SerieeTheme
import com.seriee.app.ui.WelcomeScreen
import com.seriee.app.ui.resolvePalette

class MainActivity : AppCompatActivity() {

    /** Item the widget asked us to open, consumed once by [SerieeApp]. */
    private val pendingWidgetItem = mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        val savedTag = getSharedPreferences("seriee", MODE_PRIVATE).getString("seriee_lang", "") ?: ""
        val currentTags = AppCompatDelegate.getApplicationLocales().toLanguageTags()
        if (savedTag.isNotBlank() && currentTags != savedTag) {
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(savedTag))
        }
        super.onCreate(savedInstanceState)
        pendingWidgetItem.value = widgetItemFrom(intent)
        runCatching { applyWindowBackground() }
        enableEdgeToEdge()
        setContent {
            val vm: SerieeViewModel = viewModel()

            val permissionLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { _ -> }

            // Wait until the welcome slides are done before asking for permissions.
            LaunchedEffect(vm.showWelcome) {
                if (vm.showWelcome) return@LaunchedEffect
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                    ContextCompat.checkSelfPermission(
                        this@MainActivity,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }

            SerieeTheme(
                mode = vm.themeMode,
                style = vm.themeStyle,
                customAccent = vm.customAccent,
                customBackgroundStart = vm.customBackgroundStart,
                customBackgroundEnd = vm.customBackgroundEnd,
                customGradient = vm.customGradient,
                customGlass = vm.customGlass,
                customHighContrast = vm.customHighContrast,
                customExtras = vm.customExtras,
                reduceTransparency = vm.reduceTransparency,
                batterySaverAmoled = vm.batterySaverAmoled
            ) {
                var splashDone by rememberSaveable { mutableStateOf(false) }
                Box(Modifier.fillMaxSize()) {
                    Crossfade(targetState = vm.showWelcome, label = "welcome") { welcome ->
                        if (welcome) {
                            WelcomeScreen(onFinish = vm::finishWelcome)
                        } else {
                            SerieeApp(
                                vm = vm,
                                openItemId = pendingWidgetItem.value,
                                onOpenItemHandled = { pendingWidgetItem.value = null },
                            )
                        }
                    }
                    AnimatedVisibility(
                        visible = !splashDone,
                        enter = EnterTransition.None,
                        exit = fadeOut(tween(240))
                    ) {
                        SerieeSplash(onFinished = { splashDone = true })
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        pendingWidgetItem.value = widgetItemFrom(intent)
    }

    /** Reads a `seriee://item/<id>` deep link from a widget row tap. */
    private fun widgetItemFrom(intent: Intent?): String? {
        val data = intent?.data ?: return null
        if (data.scheme != "seriee" || data.host != "item") return null
        return data.pathSegments.firstOrNull()?.takeIf { it.isNotBlank() }
    }

    override fun onResume() {
        super.onResume()
        // The widget writes straight to storage, so re-read it on return.
        runCatching { ViewModelProvider(this)[SerieeViewModel::class.java].refreshFromStorage() }
    }

    /**
     * Paints the persisted background before the first Compose frame so light
     * themes no longer flash the dark window colour from themes.xml.
     */
    private fun applyWindowBackground() {
        val repo = SerieeRepository(this)
        val systemDark = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
            Configuration.UI_MODE_NIGHT_YES
        val palette = resolvePalette(
            mode = repo.loadThemeMode(),
            style = repo.loadThemeStyle(),
            systemDark = systemDark,
            custom = CustomThemeColors(
                accent = Color(repo.loadCustomAccent()),
                backgroundStart = Color(repo.loadCustomBackgroundStart()),
                backgroundEnd = Color(repo.loadCustomBackgroundEnd()),
                gradient = repo.loadCustomGradient(),
                glass = repo.loadCustomGlass(),
                highContrast = repo.loadCustomHighContrast(),
                extras = repo.loadCustomExtras()
            ),
            reduceTransparency = repo.loadReduceTransparency()
        )
        window.setBackgroundDrawable(ColorDrawable(palette.bg.toArgb()))
    }
}
