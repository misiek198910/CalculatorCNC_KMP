import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import com.android.build.api.dsl.ApplicationExtension
import java.util.Properties
val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localPropertiesFile.inputStream().use { localProperties.load(it) }
}

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)

}

extensions.configure<ApplicationExtension> {
    namespace = "kalkulator.cnc"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "kalkulator.cnc"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 99
        versionName = "4.3.5"

        manifestPlaceholders["adMobAppId"] = ""
    }

    signingConfigs {
        create("release") {
            storeFile = file(localProperties.getProperty("MYAPP_RELEASE_STORE_FILE", "brak-sciezki"))
            storePassword = localProperties.getProperty("MYAPP_RELEASE_STORE_PASSWORD", "")
            keyAlias = localProperties.getProperty("MYAPP_RELEASE_KEY_ALIAS", "")
            keyPassword = localProperties.getProperty("MYAPP_RELEASE_KEY_PASSWORD", "")
        }
    }

    buildFeatures {
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    buildTypes {
        getByName("debug") {
            manifestPlaceholders["adMobAppId"] = "ca-app-pub-3940256099942544~3347511713"

            buildConfigField("String", "AD_BANNER_ID", "\"ca-app-pub-3940256099942544/6300978111\"")
            buildConfigField("String", "AD_START_UNIT_ID", "\"ca-app-pub-3940256099942544/9257395921\"")
            buildConfigField("String", "AD_BANNER_INLINE_ID", "\"ca-app-pub-3940256099942544/6300978111\"")
            buildConfigField("String", "ADMOB_FULLSCREEN_ID", "\"ca-app-pub-3940256099942544/1033173712\"")
            buildConfigField("String", "ADMOB_INLINE_ID", "\"ca-app-pub-3940256099942544/6300978111\"")
        }

        getByName("release") {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("release")

            val adMobAppId = localProperties.getProperty("ADMOB_APP_ID") ?: ""
            val bannerId = localProperties.getProperty("ADMOB_BANNER_ID") ?: ""
            val adStartId = localProperties.getProperty("ADMOB_ADSTART_ID") ?: ""
            val fullscreenId = localProperties.getProperty("ADMOB_FULLSCREEN_ID") ?: ""
            val inlineId = localProperties.getProperty("ADMOB_INLINE_ID") ?: ""

            manifestPlaceholders["adMobAppId"] = adMobAppId

            buildConfigField("String", "AD_BANNER_ID", "\"$bannerId\"")
            buildConfigField("String", "AD_BANNER_INLINE_ID", "\"$inlineId\"")
            buildConfigField("String", "AD_START_UNIT_ID", "\"$adStartId\"")
            buildConfigField("String", "ADMOB_FULLSCREEN_ID", "\"$fullscreenId\"")
            buildConfigField("String", "ADMOB_INLINE_ID", "\"$inlineId\"")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_11)
    }
}

dependencies {
    implementation(projects.shared)
    implementation(libs.room.runtime)
    implementation(libs.androidx.activity.compose)
    implementation(libs.play.services.ads)
    implementation(libs.compose.foundation)
    implementation(libs.compose.ui)
    implementation(libs.compose.material3)
    implementation(libs.play.services.ump)
    implementation(project.dependencies.platform(libs.firebase.bom))
    implementation(libs.play.app.update)
    implementation(libs.play.app.update.ktx)
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashlytics)
}