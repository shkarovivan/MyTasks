import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.hilt)
    alias(libs.plugins.room)
}

android {
    namespace = "com.shkarov.mytasks"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.shkarov.mytasks"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        // Читаем токен из local.properties (для отладки)
        val localProperties = Properties()
        localProperties.load(
            project.rootProject.file("local.properties").readText().byteInputStream()
        )
        val token = localProperties["proxy.token"] as? String ?: ""
        buildConfigField("String", "API_TOKEN", "\"$token\"")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    buildFeatures {
        buildConfig = true
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.6.10"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    room {
        schemaDirectory("$projectDir/schemas")
    }
}

kotlin {
    jvmToolchain(21)

    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
        freeCompilerArgs.add("-XXLanguage:+PropertyParamAnnotationDefaultTargetMode")
    }
}

dependencies {
    // Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)

    // Lifecycle
    implementation(libs.bundles.lifecycle)

    implementation(libs.androidx.activity.compose)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.bundles.compose)

    // ViewModel Compose
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // Navigation
    implementation(libs.bundles.navigation)

    //Room
    implementation(libs.bundles.room)
    ksp(libs.androidx.room.compiler)

    //Timber
    implementation(libs.timber)

    //Data store
    implementation(libs.androidx.data.store)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.common)
    implementation(libs.androidx.hilt.work)
    ksp(libs.androidx.hilt.compiler)
    implementation(libs.hilt.navigation.compose)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)

    // Retrofit
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)

    // OkHttp
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging.interceptor)

    //Gson
    implementation(libs.gson)

    //WorkManager
    implementation(libs.work.manager)

    // Debug
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

}
