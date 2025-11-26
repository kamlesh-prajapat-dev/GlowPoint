plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.navigation.safeargs)
    id("org.jetbrains.kotlin.plugin.parcelize")

    id("com.google.gms.google-services")

    // HILT Dependency
    alias(libs.plugins.kotlinAndroidKsp)
    alias(libs.plugins.hiltAndroid)
}

android {
    namespace = "com.example.glowpoint"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.glowpoint"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        dataBinding = true
        viewBinding = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.legacy.support.v4)
    implementation(libs.androidx.fragment.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)


    // Location
    implementation(libs.play.services.location)

    // Firebase Bom
    implementation(platform("com.google.firebase:firebase-bom:34.5.0"))

    // Firebase Realtime Database library
    implementation(libs.firebase.database)

    // Firebase Authentication library
    implementation(libs.firebase.auth)

    // Firebase Firestore database
    implementation(libs.firebase.firestore)

    // Hilt dependency
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // Navigation
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    // ViewModel and LiveData
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)

    implementation("androidx.compose.material:material-icons-extended:1.7.8")

    // GeoFire
    implementation("com.firebase:geofire-android:3.2.0")
    implementation(libs.play.services.maps)

    // Gson
    implementation("com.google.code.gson:gson:2.10.1")
}