plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.lotteryeventsystem"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.example.lotteryeventsystem"
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
}

dependencies {
    implementation("com.google.android.material:material:1.12.0")
    implementation("com.google.zxing:core:3.5.3")
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")
    implementation(platform("com.google.firebase:firebase-bom:34.3.0"))
    implementation("com.google.firebase:firebase-firestore")
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")
    implementation("com.google.zxing:core:3.5.3")
    implementation(libs.firebase.firestore)
    implementation(libs.ext.junit)
    testImplementation(libs.junit)
    androidTestImplementation("androidx.test.ext:junit:1.1.6")
    androidTestImplementation("androidx.test:core:1.5.0")
    androidTestImplementation("androidx.test:rules:1.5.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.fragment:fragment-testing:1.8.5")
    debugImplementation("androidx.fragment:fragment-testing:1.8.5")
    // firebase
    implementation(platform("com.google.firebase:firebase-bom:33.8.0"))
    implementation("com.google.firebase:firebase-firestore:25.1.1")
}
