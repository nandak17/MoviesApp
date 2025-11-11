import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
    id("kotlin-parcelize")
}

android {
    namespace = "com.example.moviesapp"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.example.moviesapp"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        val properties =Properties()
        properties.load(project.rootProject.file("local.properties").inputStream())
        val apiKey : String = properties.getProperty("WATCHMODE_API_KEY") ?:""

        buildConfigField("String", "WATCHMODE_API_KEY", "\"$apiKey\"")
    }
    buildFeatures{
        buildConfig = true
        compose = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

        // Jetpack Compose
        implementation("androidx.compose.ui:ui:1.6.0")
        implementation("androidx.compose.material3:material3:1.2.0")
        implementation("androidx.compose.ui:ui-tooling-preview:1.6.0")
        implementation("androidx.activity:activity-compose:1.8.2")

        // Navigation
        implementation("androidx.navigation:navigation-compose:2.7.7")

        // ViewModel and Lifecycle
        implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
        implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")
        implementation("androidx.lifecycle:lifecycle-viewmodel-savedstate:2.7.0")

        // Retrofit for networking (REQUIREMENT #3)
        implementation("com.squareup.retrofit2:retrofit:2.9.0")
        implementation("com.squareup.retrofit2:converter-gson:2.9.0")
        implementation("com.squareup.retrofit2:adapter-rxjava3:2.9.0")

        // RxKotlin & RxAndroid for Single.zip (REQUIREMENT #3)
        implementation("io.reactivex.rxjava3:rxkotlin:3.0.1")
        implementation("io.reactivex.rxjava3:rxandroid:3.0.2")
        implementation("io.reactivex.rxjava3:rxjava:3.1.8")

        // Hilt for DI (REQUIREMENT #5)
        implementation("com.google.dagger:hilt-android:2.48")
        ksp("com.google.dagger:hilt-android-compiler:2.48")
        implementation("androidx.hilt:hilt-navigation-compose:1.2.0")


        // Shimmer effect (REQUIREMENT #1 & #2)
        implementation("com.valentinilk.shimmer:compose-shimmer:1.2.0")
    // Coil - Latest version
    implementation("io.coil-kt:coil-compose:2.7.0")

    // OkHttp for logging
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("androidx.paging:paging-runtime-ktx:3.3.4")
    implementation("androidx.paging:paging-compose:3.3.4")
    implementation("androidx.paging:paging-rxjava3:3.3.4")

    val roomVersion = "2.6.1"
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    implementation("androidx.room:room-rxjava3:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")
    implementation("androidx.room:room-paging:${roomVersion}")

    implementation("androidx.palette:palette-ktx:1.0.0")
    implementation("androidx.compose.foundation:foundation:1.6.0")
    }