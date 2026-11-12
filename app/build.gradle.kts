import java.io.FileInputStream
import java.util.Properties
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

// Release signing credentials live in <root>/keystore.properties (git-ignored).
// See keystore.properties.example. Debug builds work without it; release builds
// stay unsigned until the file exists — create it once and back up both files.
val releasePropsFile = rootProject.file("keystore.properties")
val releaseProps = Properties().apply {
    if (releasePropsFile.exists()) {
        FileInputStream(releasePropsFile).use { load(it) }
    }
}
val hasReleaseSigning = releasePropsFile.exists() &&
    !releaseProps.getProperty("storeFile").isNullOrBlank()

android {
    namespace = "com.seriee.app"
    compileSdk = 36

    signingConfigs {
        create("release") {
            if (hasReleaseSigning) {
                keyAlias = releaseProps.getProperty("keyAlias")
                keyPassword = releaseProps.getProperty("keyPassword")
                storeFile = rootProject.file(releaseProps.getProperty("storeFile"))
                storePassword = releaseProps.getProperty("storePassword")
            }
        }
    }

    defaultConfig {
        // Renamed from the AI Studio default (com.aistudio.seriee.twvpmz) so the
        // package is owned by this project. This changes the installed app identity:
        // export a backup before installing 0.3.0 over an older build.
        applicationId = "com.seriee.app"
        minSdk = 26
        targetSdk = 36
        versionCode = 13
        versionName = "0.4.1"
    }

    buildTypes {
        release {
            if (hasReleaseSigning) {
                signingConfig = signingConfigs.getByName("release")
            }
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
        // Expose versionCode/versionName to app code via BuildConfig.
        // Single source of truth: defaultConfig below — never hardcode
        // version fallbacks in Kotlin code.
        buildConfig = true
    }
}

// `kotlinOptions` was removed from the Kotlin Android plugin; configure the
// Kotlin compiler through the canonical `compilerOptions` API instead.
kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.8.0")
    implementation("androidx.activity:activity-compose:1.9.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.6")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.6")

    // Pinned to the last BOM whose Compose runtime works on AGP 8.x.
    // Compose 1.10+ (BOM 2025.xx and later) requires AGP 9.1 and compileSdk 37.
    implementation(platform("androidx.compose:compose-bom:2024.09.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")

    implementation("io.coil-kt:coil-compose:2.7.0")

    testImplementation("junit:junit:4.13.2")
}
