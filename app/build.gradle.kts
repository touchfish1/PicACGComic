plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("androidx.navigation.safeargs.kotlin")
    id("kotlinx-serialization")
    id("kotlin-kapt")
}

android {
    namespace = "projekt.cloud.piece.pic"
    compileSdk = 33

    defaultConfig {
        applicationId = "projekt.cloud.piece.pic"
        minSdk = 21
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        val release by getting {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
        val debug by getting {
            isMinifyEnabled = false
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
        viewBinding = true
        dataBinding = true
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.appcompat:appcompat:1.5.1")
    implementation("com.google.android.material:material:1.8.0-alpha02")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.navigation:navigation-fragment-ktx:2.5.3")
    implementation("androidx.navigation:navigation-ui-ktx:2.5.3")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.7.21")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json-jvm:1.4.1")
    implementation("com.fasterxml.uuid:java-uuid-generator:4.0.1")
    implementation("com.squareup.okhttp3:okhttp:5.0.0-alpha.10")
    kapt("androidx.databinding:databinding-compiler-common:7.3.1")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.4")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.0")
}