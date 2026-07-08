import com.android.build.gradle.LibraryExtension
import org.gradle.kotlin.dsl.implementation
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.room)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlinSerialization)

}

room {
    schemaDirectory("$projectDir/schemas")
}
val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(FileInputStream(localPropertiesFile))
}

extensions.configure<LibraryExtension> {
    namespace = "kalkulator.cnc.shared"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        buildConfig = true
    }

    buildTypes {
        getByName("debug") {
            buildConfigField("String", "AD_BANNER_ID", "\"ca-app-pub-3940256099942544/6300978111\"")
            buildConfigField("String", "AD_START_UNIT_ID", "\"ca-app-pub-3940256099942544/9257395921\"")
            buildConfigField("String", "AD_BANNER_INLINE_ID", "\"ca-app-pub-3940256099942544/6300978111\"")
            buildConfigField("String", "ADMOB_FULLSCREEN_ID", "\"ca-app-pub-3940256099942544/1033173712\"")
            buildConfigField("String", "ADMOB_INLINE_ID", "\"ca-app-pub-3940256099942544/6300978111\"")
        }

        getByName("release") {
            val bannerId = localProperties.getProperty("ADMOB_BANNER_ID") ?: ""
            val adStartId = localProperties.getProperty("ADMOB_ADSTART_ID") ?: ""
            val fullscreenId = localProperties.getProperty("ADMOB_FULLSCREEN_ID") ?: ""
            val inlineId = localProperties.getProperty("ADMOB_INLINE_ID") ?: ""

            buildConfigField("String", "AD_BANNER_ID", "\"$bannerId\"")
            buildConfigField("String", "AD_BANNER_INLINE_ID", "\"$inlineId\"")
            buildConfigField("String", "AD_START_UNIT_ID", "\"$adStartId\"")
            buildConfigField("String", "ADMOB_FULLSCREEN_ID", "\"$fullscreenId\"")
            buildConfigField("String", "ADMOB_INLINE_ID", "\"$inlineId\"")
        }
    }
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Shared"
            isStatic = true
        }
    }

    sourceSets {
        androidMain.dependencies {
            implementation(libs.ktor.client.okhttp)
            implementation(libs.billing.ktx)
            implementation(libs.play.services.ads)
            implementation(project.dependencies.platform(libs.firebase.bom))
            implementation(libs.play.app.update)
            implementation(libs.play.app.update.ktx)
        }
        commonMain.dependencies {
            implementation(compose.ui)
            implementation(libs.compose.material.icons.extended)
            implementation(libs.coil.compose)
            implementation(libs.coil.network.ktor)
            implementation(libs.kotlinx.datetime)
            implementation(libs.room.runtime)
            implementation(libs.sqlite.bundled)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.components.resources)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.firebase.analytics)
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

dependencies {
    add("kspCommonMainMetadata", libs.room.compiler)
    add("kspAndroid", libs.room.compiler)
    add("kspIosArm64", libs.room.compiler)
    add("kspIosSimulatorArm64", libs.room.compiler)
}