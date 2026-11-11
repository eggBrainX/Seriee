package com.seriee.app

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.LaunchedEffect
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.seriee.app.ui.SerieeApp
import com.seriee.app.ui.SerieeTheme

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val savedTag = getSharedPreferences("seriee", MODE_PRIVATE).getString("seriee_lang", "") ?: ""
        val currentTags = AppCompatDelegate.getApplicationLocales().toLanguageTags()
        if (savedTag.isNotBlank() && currentTags != savedTag) {
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(savedTag))
        }
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val vm: SerieeViewModel = viewModel()

            val permissionLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { _ -> }

            LaunchedEffect(Unit) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }

            SerieeTheme(
                theme = vm.theme,
                customAccent = vm.customAccent,
                customGradient = vm.customGradient
            ) {
                SerieeApp(vm)
            }
        }
    }
}
