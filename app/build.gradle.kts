import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.services)
}

val secretsFile = rootProject.file("secrets.properties")
val secrets = Properties().apply {
    if (secretsFile.exists()) load(secretsFile.inputStream())
}

android {
    namespace = "com.example.edulocker"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.edulocker"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        val apiKey = secrets.getProperty("MSG91_API_KEY", "")
        val templateId = secrets.getProperty("MSG91_TEMPLATE_ID", "")
        val isConfigured = apiKey.isNotEmpty() && !apiKey.startsWith("YOUR_")
                && templateId.isNotEmpty() && !templateId.startsWith("YOUR_")
        buildConfigField("String", "MSG91_API_KEY", "\"$apiKey\"")
        buildConfigField("String", "MSG91_TEMPLATE_ID", "\"$templateId\"")
        buildConfigField("String", "MSG91_SENDER_ID", "\"${secrets.getProperty("MSG91_SENDER_ID", "EDULKR")}\"")
        buildConfigField("boolean", "MSG91_CONFIGURED", "$isConfigured")
        buildConfigField("String", "CLOUDINARY_CLOUD_NAME", "\"${secrets.getProperty("CLOUDINARY_CLOUD_NAME", "")}\"")
        buildConfigField("String", "CLOUDINARY_UPLOAD_PRESET", "\"${secrets.getProperty("CLOUDINARY_UPLOAD_PRESET", "")}\"")
        buildConfigField("String", "NEWSDATA_API_KEY", "\"${secrets.getProperty("NEWSDATA_API_KEY", "")}\"")
    }

    buildFeatures {
        buildConfig = true
        viewBinding = true
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
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.recyclerview)
    implementation(libs.cardview)
    implementation(libs.viewpager2)
    implementation(libs.activity)
    implementation(libs.fragment)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)

    // Image loading
    implementation(libs.glide)
    annotationProcessor(libs.glide.compiler)

    // QR Code
    implementation(libs.zxing.android)

    // HTTP client for MSG91
    implementation(libs.okhttp)

    // Charts for government stats dashboard
    implementation(libs.mpandroidchart)

    // Circle image view for profile pictures
    implementation(libs.circleimageview)

    // Lottie for loading animations
    implementation(libs.lottie)
    implementation(libs.swiperefreshlayout)

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
