plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
    //id("com.google.firebase.firebase-perf")
}

android {
    namespace = "com.example.nexusdoc"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.nexusdoc"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }


    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    // AndroidX Core
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    implementation(libs.activity)

    implementation("com.google.android.gms:play-services-base:18.4.0")
    implementation("com.google.android.gms:play-services-basement:18.4.0")

    implementation("com.google.android.gms:play-services-auth:21.1.1")
    implementation("com.google.android.gms:play-services-auth-api-phone:18.0.2")

    implementation ("androidx.recyclerview:recyclerview:1.3.2")
    implementation ("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")

    // Navigation Component
    implementation("androidx.navigation:navigation-fragment-ktx:2.8.3")
    implementation("androidx.navigation:navigation-ui-ktx:2.8.3")

    // Firebase (toutes les versions gérées par le BOM)
    implementation(platform("com.google.firebase:firebase-bom:32.7.0")) // Version stable récente
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-analytics-ktx")
    implementation("com.google.firebase:firebase-messaging-ktx")


    //Supabase
    implementation ("io.github.jan-tennert.supabase:postgrest-kt:1.4.0")
    implementation ("io.github.jan-tennert.supabase:storage-kt:1.4.0")


    //Retrofit
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation ("com.squareup.okhttp3:okhttp:4.10.0")

    implementation ("com.squareup.okhttp3:logging-interceptor:4.12.0")



    //implementation(platform("com.google.firebase:firebase-bom:33.4.1")) // Mise à jour
    //implementation("com.google.firebase:firebase-firestore-ktx")
    // Supprimez ces dépendances si non utilisées
    //implementation("com.google.firebase:firebase-auth")
    //implementation("com.google.firebase:firebase-analytics")
    //implementation("com.google.firebase:firebase-messaging")
    implementation("com.firebaseui:firebase-ui-firestore:8.0.2")

    implementation(libs.security.crypto)
    // Pour utiliser les fonctionnalités liées à MasterKeys (optionnel)
    implementation ("androidx.security:security-crypto-ktx:1.1.0-alpha06")


    // ML Kit Text Recognition
    //implementation("com.google.mlkit:text-recognition:16.0.0")
    // Supprimez si non utilisé
    //implementation("com.google.mlkit:image-labeling:17.0.7")

    // CameraX
    //implementation("androidx.camera:camera-core:1.3.4") // Mise à jour
    //implementation("androidx.camera:camera-camera2:1.3.4")
    //implementation("androidx.camera:camera-lifecycle:1.3.4")
    //implementation("androidx.camera:camera-view:1.3.4")

    implementation ("androidx.camera:camera-view:1.4.2")

    // OpenCV
    //implementation("org.opencv:opencv:4.8.0") // Mise à jour

    // PDFBox pour l'extraction et génération de PDF
   // implementation("com.tom-roush:pdfbox-android:2.0.27.0")

    // Apache POI pour DOCX
    //implementation("org.apache.poi:poi:5.2.5") // Ajout
    //implementation("org.apache.poi:poi-ooxml:5.2.5") // Mise à jour
    //implementation("org.apache.xmlbeans:xmlbeans:5.2.1") // Mise à jour

    // Kotlin Coroutines
    //implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
    //implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")

    // Glide pour le chargement d'images
    implementation("com.github.bumptech.glide:glide:4.16.0")
    implementation(libs.room.common.jvm)

    // Tests
   // testImplementation("junit:junit:4.13.2")
    //androidTestImplementation("androidx.test.ext:junit:1.2.1") // Mise à jour
   // androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1") // Mise à jour

    // Commons IO (conservé, mais vérifiez son utilisation)
   //implementation("commons-io:commons-io:2.15.1")
}