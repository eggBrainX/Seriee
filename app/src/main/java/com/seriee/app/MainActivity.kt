package com.seriee.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.seriee.app.debug.AppLogger
import com.seriee.app.ui.SerieeApp
import com.seriee.app.ui.SerieeTheme

class MainActivity : AppCompatActivity() {
    private val viewModel: SerieeViewModel by viewModels()

    private val requestNotifPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ -> }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        AppLogger.i(
            "MainActivity",
            "onCreate (crashDialogExtra=${intent?.getBooleanExtra(SerieeApplication.EXTRA_SHOW_CRASH_DIALOG, false) == true})"
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) {
                requestNotifPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        setContent {
            SerieeTheme(
                mode = viewModel.themeMode,
                style = viewModel.themeStyle,
                custom = viewModel.customTheme,
                reduceTransparency = viewModel.reduceTransparency,
                batteryAmoled = viewModel.batteryAmoled,
            ) {
                SerieeApp(viewModel = viewModel)
            }
        }
    }
}
