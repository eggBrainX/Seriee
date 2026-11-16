package com.seriee.app

import android.Manifest
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.ContextCompat
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.seriee.app.data.SerieeRepository
import com.seriee.app.ui.CustomThemeColors
import com.seriee.app.ui.SerieeApp
import com.seriee.app.ui.SerieeTheme
import com.seriee.app.ui.resolvePalette

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val savedTag = getSharedPreferences("seriee", MODE_PRIVATE).getString("seriee_lang", "") ?: ""
        val currentTags = AppCompatDelegate.getApplicationLocales().toLanguageTags()
        if (savedTag.isNotBlank() && currentTags != savedTag) {
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(savedTag))
        }
        super.onCreate(savedInstanceState)
        runCatching { applyWindowBackground() }
        enableEdgeToEdge()
        setContent {
            val vm: SerieeViewModel = viewModel()

            val permissionLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { _ -> }

            LaunchedEffect(Unit) {
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
                reduceTransparency = vm.reduceTransparency,
                batterySaverAmoled = vm.batterySaverAmoled
            ) {
                SerieeApp(vm)
            }
        }
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
                gradient = repo.loadCustomGradient()
            ),
            reduceTransparency = repo.loadReduceTransparency()
        )
        window.setBackgroundDrawable(ColorDrawable(palette.bg.toArgb()))
    }
}
