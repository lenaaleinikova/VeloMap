plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.example.velomap"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.velomap"
        minSdk = 24
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    packagingOptions {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            merges += "META-INF/LICENSE.md"
            merges += "META-INF/LICENSE-notice.md"
            excludes += "META-INF/DEPENDENCIES"
        }
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation("com.mapbox.maps:android:11.7.1")
    implementation("com.google.android.gms:play-services-location:21.3.0")
//    implementation("com.google.android.gms:play-services-maps:19.0.0")
//    implementation ("com.mapbox.maps:plugin-locationcomponent:10.10.0")
//    implementation ("com.mapbox.mapboxsdk:mapbox-android-core:6.1.0")
//    implementation("com.mapbox.maps:plugin-gestures:10.0.0")

    implementation ("com.google.maps.android:android-maps-utils:2.3.0")

    implementation ("com.google.api-client:google-api-client-android:1.33.0")
//    implementation("com.google.apis:google-api-services-sheets:v4-rev614-1.25.0")
    implementation(libs.google.api.client)
    implementation(libs.google.api.services.sheets)
    implementation(libs.google.api.client.gson)
    implementation(libs.jackson.databind)




    implementation (libs.google.api.client.gson)
    implementation (libs.kotlinx.coroutines.core)
    implementation ("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.7")

    implementation (libs.retrofit)
    implementation (libs.converter.gson)



}