plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "it.armonia1403.tempio"
    compileSdk = 35

    defaultConfig {
        applicationId = "it.armonia1403.tempio"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            // Firmato con la chiave di debug: l'APK release si installa
            // direttamente sul telefono senza creare un keystore.
            signingConfig = signingConfigs.getByName("debug")
        }
    }

    // I file audio non vanno compressi: la riproduzione parte subito.
    androidResources {
        noCompress += listOf("mp3")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.webkit:webkit:1.12.1")
}
