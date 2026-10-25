import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

val localProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        localPropertiesFile.inputStream().use { load(it) }
    }
}

fun getSecret(key: String, default: String? = null): String? {
    return System.getenv(key)
        ?: localProperties.getProperty(key)
        ?: (project.findProperty(key) as? String)
        ?: default
}

android {
    namespace = "com.gasolineras.app"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.gasolineras.app"
        minSdk = 26
        targetSdk = 36
        versionCode = 32
        versionName = "2.2.12"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
        // MapLibre ships ~11 MB of native code per ABI; x86/x86_64 only serve emulators.
        ndk {
            abiFilters += listOf("arm64-v8a", "armeabi-v7a")
        }
    }

    val keystorePath = getSecret("KEYSTORE_PATH", "keystore/release.keystore")
    val keystoreFile = keystorePath?.let { file(it) }
    val storePasswordVal = getSecret("KEYSTORE_PASSWORD")
    val keyAliasVal = getSecret("KEY_ALIAS")
    val keyPasswordVal = getSecret("KEY_PASSWORD")

    val hasSigningKeys = keystoreFile != null && keystoreFile.exists() &&
            !storePasswordVal.isNullOrBlank() &&
            !keyAliasVal.isNullOrBlank() &&
            !keyPasswordVal.isNullOrBlank()

    signingConfigs {
        if (hasSigningKeys) {
            create("release") {
                storeFile = keystoreFile
                storePassword = storePasswordVal
                keyAlias = keyAliasVal
                keyPassword = keyPasswordVal
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            if (hasSigningKeys) {
                signingConfig = signingConfigs.getByName("release")
            }
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            // Debug builds use the dedicated Android debug certificate.
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
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

    applicationVariants.all {
        outputs.all {
            val output = this as? com.android.build.gradle.internal.api.BaseVariantOutputImpl
            output?.outputFileName = "gasolineras.apk"
        }
    }
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation("androidx.core:core-ktx:1.18.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.10.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.10.0")
    implementation("androidx.activity:activity-compose:1.13.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.10.0")

    // Jetpack Compose BOM
    val composeBom = platform("androidx.compose:compose-bom:2025.08.01")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")

    // Retrofit & Gson for MITECO Carburantes REST API
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Google Play Services Location for GPS coordinates
    implementation("com.google.android.gms:play-services-location:21.4.0")

    // Maintained renderer; OSM raster source configured locally, without API keys.
    implementation("org.maplibre.gl:android-sdk-opengl:13.7.1")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.10.2")

    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}

// Refuse to ship a runtime graph that no longer matches the bundled license inventory.
val verifyRuntimeLicenses by tasks.registering {
    doLast {
        val actual = configurations.getByName("releaseRuntimeClasspath")
            .resolvedConfiguration.resolvedArtifacts.map {
                "${it.moduleVersion.id.group}:${it.moduleVersion.id.name}:${it.moduleVersion.id.version}"
            }.distinct().sorted()
        val manifest = file("src/main/assets/license-runtime.json")
        val recorded = groovy.json.JsonSlurper().parse(manifest) as List<*>
        check(actual == recorded) {
            "Runtime license inventory is stale. Export it with scripts/export-runtime-licenses.gradle, then run scripts/update-license-notices.py and review all new licenses."
        }
    }
}
tasks.named("preBuild") { dependsOn(verifyRuntimeLicenses) }
