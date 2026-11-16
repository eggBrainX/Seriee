package com.seriee.app.data

data class ThemePreset(
    val mode: AppThemeMode,
    val style: AppThemeStyle,
    val accentArgb: Long,
    val bgStartArgb: Long,
    val bgEndArgb: Long,
    val useGradient: Boolean,
)

object ThemePresetCodec {
    private const val PREFIX = "seriee-theme:1;"

    fun encode(preset: ThemePreset): String {
        fun hex(v: Long) = (v and 0xFFFFFFFFL).toString(16).padStart(8, '0').lowercase()
        return PREFIX + listOf(
            "mode=${preset.mode.name.lowercase()}",
            "style=${preset.style.name.lowercase()}",
            "accent=${hex(preset.accentArgb)}",
            "bg=${hex(preset.bgStartArgb)}",
            "bg2=${hex(preset.bgEndArgb)}",
            "grad=${if (preset.useGradient) "1" else "0"}",
        ).joinToString(";")
    }

    fun decode(raw: String): ThemePreset? {
        val trimmed = raw.trim()
        if (!trimmed.startsWith(PREFIX)) return null
        val map = HashMap<String, String>()
        for (part in trimmed.removePrefix(PREFIX).split(";")) {
            val eq = part.indexOf('=')
            if (eq > 0) {
                map[part.substring(0, eq).trim().lowercase()] = part.substring(eq + 1).trim()
            }
        }
        val mode = when (map["mode"]) {
            "light" -> AppThemeMode.Light
            "dark" -> AppThemeMode.Dark
            else -> AppThemeMode.System
        }
        val style = AppThemeStyle.entries.firstOrNull { it.name.lowercase() == map["style"] }
            ?: AppThemeStyle.Custom
        val accent = map["accent"]?.toLongOrNull(16) ?: return null
        val bg1 = map["bg"]?.toLongOrNull(16) ?: return null
        val bg2 = map["bg2"]?.toLongOrNull(16) ?: return null
        return ThemePreset(
            mode = mode,
            style = style,
            accentArgb = accent,
            bgStartArgb = bg1,
            bgEndArgb = bg2,
            useGradient = map["grad"] == "1",
        )
    }
}
