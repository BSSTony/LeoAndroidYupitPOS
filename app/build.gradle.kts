plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.LeoNet.leonetscaner"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.LeoNet.leonetscaner"
        minSdk = 24
        targetSdk = 35
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
}

dependencies {


    // CameraX
    implementation ("androidx.camera:camera-camera2:1.3.2")
    implementation ("androidx.camera:camera-lifecycle:1.3.2")
    implementation ("androidx.camera:camera-view:1.3.2")

    implementation ("com.journeyapps:zxing-android-embedded:4.3.0")
    implementation ("com.squareup.okhttp3:okhttp:4.12.0")
//    implementation ("com.google.mlkit:barcode-scanning:18.0.3")
//    implementation ("com.google.mlkit:common:18.0.2")
    implementation ("com.github.bumptech.glide:glide:4.16.0")
    implementation(libs.documentfile)
    implementation(libs.vision.common)
    implementation(libs.play.services.mlkit.barcode.scanning)
    annotationProcessor ("com.github.bumptech.glide:compiler:4.16.0")

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}