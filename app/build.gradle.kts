import groovy.json.JsonSlurper
import java.util.Properties

plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("com.google.dagger.hilt.android")
    id("com.google.devtools.ksp")
}

val localProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        localPropertiesFile.inputStream().use(::load)
    }
}

fun googleServicesApiKey(): String {
    val googleServicesFile = project.file("google-services.json")
    if (!googleServicesFile.exists()) return ""

    val config = JsonSlurper().parse(googleServicesFile) as? Map<*, *> ?: return ""
    val clients = config["client"] as? List<*> ?: return ""
    val firstClient = clients.firstOrNull() as? Map<*, *> ?: return ""
    val apiKeys = firstClient["api_key"] as? List<*> ?: return ""
    val firstApiKey = apiKeys.firstOrNull() as? Map<*, *> ?: return ""

    return firstApiKey["current_key"] as? String ?: ""
}

val mapsApiKey = providers.gradleProperty("MAPS_API_KEY")
    .orElse(localProperties.getProperty("MAPS_API_KEY") ?: googleServicesApiKey())

fun stringProperty(name: String): String? =
    providers.gradleProperty(name).orNull
        ?: System.getenv(name)
        ?: localProperties.getProperty(name)

fun intProperty(name: String): Int? = stringProperty(name)?.toIntOrNull()

val ciVersionCode = intProperty("CI_VERSION_CODE")
val ciVersionCodeBase = intProperty("CI_VERSION_CODE_BASE") ?: 10_000
val releaseStoreFilePath = stringProperty("ANDROID_RELEASE_STORE_FILE")
val releaseStorePassword = stringProperty("ANDROID_RELEASE_STORE_PASSWORD")
val releaseKeyAlias = stringProperty("ANDROID_RELEASE_KEY_ALIAS")
val releaseKeyPassword = stringProperty("ANDROID_RELEASE_KEY_PASSWORD")
val hasReleaseSigningConfig = listOf(
    releaseStoreFilePath,
    releaseStorePassword,
    releaseKeyAlias,
    releaseKeyPassword,
).all { !it.isNullOrBlank() }

android {
    namespace = "com.stefanchurch.ferryservicesandroid"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.stefanchurch.ferryservices"
        minSdk = 28
        targetSdk = 35
        versionCode = ciVersionCode?.let { ciVersionCodeBase + it } ?: 41
        versionName = "2.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = true

        buildConfigField("String", "API_BASE_URL", "\"https://scottishferryapp.com/\"")
        buildConfigField("String", "SUPPORT_EMAIL", "\"stefan.church@gmail.com\"")
        buildConfigField("String", "PLAY_STORE_URL", "\"https://play.google.com/store/apps/details?id=com.stefanchurch.ferryservices\"")
        manifestPlaceholders["MAPS_API_KEY"] = mapsApiKey.get()
    }

    signingConfigs {
        if (hasReleaseSigningConfig) {
            create("release") {
                storeFile = file(requireNotNull(releaseStoreFilePath))
                storePassword = requireNotNull(releaseStorePassword)
                keyAlias = requireNotNull(releaseKeyAlias)
                keyPassword = requireNotNull(releaseKeyPassword)
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            if (hasReleaseSigningConfig) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.activity:activity-compose:1.10.1")
    implementation("androidx.navigation:navigation-compose:2.8.9")
    implementation("androidx.compose.material3:material3:1.3.1")
    implementation("androidx.compose.foundation:foundation:1.8.3")
    implementation("androidx.compose.material:material-icons-extended:1.7.8")
    implementation("androidx.compose.ui:ui:1.8.3")
    implementation("androidx.compose.ui:ui-tooling-preview:1.8.3")
    debugImplementation("androidx.compose.ui:ui-tooling:1.8.3")
    implementation("com.google.android.material:material:1.12.0")

    implementation("com.google.dagger:hilt-android:2.56.1")
    ksp("com.google.dagger:hilt-compiler:2.56.1")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.10.1")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.0")

    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.jakewharton.retrofit:retrofit2-kotlinx-serialization-converter:1.0.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    implementation("androidx.datastore:datastore-preferences:1.1.3")

    implementation("com.google.android.gms:play-services-maps:19.1.0")
    implementation("com.google.maps.android:maps-compose:6.4.4")
    implementation("com.google.android.gms:play-services-location:21.3.0")

    implementation("com.google.firebase:firebase-messaging-ktx:24.1.1")
    implementation("io.sentry:sentry-android:6.17.0")

    implementation("androidx.browser:browser:1.8.0")
    implementation("androidx.webkit:webkit:1.12.1")

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.1")
    testImplementation("app.cash.turbine:turbine:1.2.0")
}
