plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")

    id("com.google.firebase.crashlytics")
    id("com.google.firebase.firebase-perf")

}

android {
    namespace = "ru.xdxasoft.xdxanotes"
    compileSdk = 35

    applicationVariants.all {
        outputs.all {

            if (this is com.android.build.gradle.internal.api.ApkVariantOutputImpl) {
                this.outputFileName = "XDXANotes_${versionName}.apk"
            }
        }
    }

    defaultConfig {
        applicationId = "ru.xdxasoft.xdxanotes"
        minSdk = 29
        targetSdk = 34
        versionCode = 1
        versionName = "0.0.1"

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
    // Core dependencies
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.play.services.safetynet)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // Firebase dependencies
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))

    // УБЕРИТЕ ВЕРСИИ со всех Firebase зависимостей!
    implementation("com.google.firebase:firebase-messaging")
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth") // убрана версия
    implementation("com.google.firebase:firebase-database") // убрана версия
    implementation("com.google.firebase:firebase-firestore") // убрана версия
    implementation("com.google.firebase:firebase-inappmessaging-display")
    implementation("com.google.firebase:firebase-config")
    implementation("com.google.firebase:firebase-crashlytics")
    implementation("com.google.firebase:firebase-perf")

    // AndroidX и остальные зависимости без изменений
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.1")
    implementation("androidx.activity:activity:1.9.2")
    implementation("androidx.annotation:annotation:1.6.0")
    implementation("androidx.biometric:biometric:1.2.0-alpha05")
    implementation("androidx.work:work-runtime:2.7.1")
    implementation("com.google.android.material:material")

    // Play Services
    implementation("com.google.android.gms:play-services-base:18.1.0")

    implementation("androidx.room:room-runtime:2.6.1")
    annotationProcessor("androidx.room:room-compiler:2.6.1")
    implementation("com.github.bumptech.glide:glide:4.12.0")
    implementation("com.squareup.okhttp3:okhttp:4.9.1")
    implementation("com.google.code.gson:gson:2.8.8")
}