import java.util.Properties


plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
}

val signingProperties = Properties().apply {
    val signingFile = rootProject.file("keystore.properties")
    if (signingFile.exists()) signingFile.inputStream().use { load(it) }
}


android {
    namespace = "uz.meduza.anime"
    compileSdk = 36

    defaultConfig {
        applicationId = "uz.meduza.anime"
        minSdk = 24
        targetSdk = 36
        versionCode = 7
        versionName = "1.1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        manifestPlaceholders["appLabel"] = "@string/app_name"
    }

    signingConfigs {
        create("release") {
            storeFile = signingProperties.getProperty("storeFile")?.let { rootProject.file(it) }
            storePassword = signingProperties.getProperty("storePassword")
            keyAlias = signingProperties.getProperty("keyAlias")
            keyPassword = signingProperties.getProperty("keyPassword")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            val releaseConfig = signingConfigs.getByName("release")
            signingConfig = if (releaseConfig.storeFile?.exists() == true) {
                releaseConfig
            } else {
                signingConfigs.getByName("debug")
            }
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            ndk {
                debugSymbolLevel = "FULL"
            }
        }
        debug {
            applicationIdSuffix = ".preview"
            manifestPlaceholders["appLabel"] = "Meduza Preview"
            versionNameSuffix = "-preview"
            // Keep the published app and its user session intact on test devices.
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }

    lint {
        abortOnError = true
        checkReleaseBuilds = true
        explainIssues = true
        showAll = true
        textReport = true
        textOutput = file("stdout")
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "META-INF/INDEX.LIST"
        }
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    compilerOptions {
        allWarningsAsErrors.set(false)
        verbose.set(true)
        freeCompilerArgs.addAll(
            "-opt-in=kotlin.RequiresOptIn",
            "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api"
        )
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.addAll(listOf("-Xlint:unchecked", "-Xlint:deprecation"))
}

dependencies {
    testImplementation("junit:junit:4.13.2")
    implementation(libs.timber)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)

    // Jetpack Compose BOM
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.navigation.compose)
    implementation("androidx.emoji2:emoji2:1.5.0")
    implementation("androidx.emoji2:emoji2-views:1.5.0")

    // DataStore Preferences
    implementation(libs.androidx.datastore.preferences)

    // Room Database & Caching
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // WorkManager
    implementation(libs.androidx.work.runtime.ktx)

    // Networking (Retrofit & OkHttp)
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.gson)

    // Coil Image Loading (Async images)
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)

    // Media3 / ExoPlayer (HLS, DASH, MP4 Streaming)
    implementation(libs.media3.exoplayer)
    implementation(libs.media3.exoplayer.hls)
    implementation(libs.media3.exoplayer.dash)
    implementation(libs.media3.ui)
    implementation(libs.media3.session)

    // Coroutines
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    // Kotlin Multiplatform Shared Auth & UI
    implementation(project(":composeApp"))

    debugImplementation(libs.androidx.ui.tooling)
}
