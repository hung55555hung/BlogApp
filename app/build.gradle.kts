plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.bugbug.blogapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.bugbug.blogapp"
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures{
        viewBinding = true

    }
}

dependencies {

    implementation(libs.activity)
    implementation(libs.support.annotations)
    implementation(libs.annotation)
    implementation(libs.support.annotations)
    implementation(libs.annotation)
    implementation(libs.firebase.database)
    implementation(libs.firebase.storage)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.auth)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation ("com.google.android.material:material:1.12.0")
    implementation ("androidx.navigation:navigation-fragment-ktx:2.7.7")
    implementation ("androidx.navigation:navigation-ui-ktx:2.7.7")
    implementation("com.github.MrNouri:DynamicSizes:1.0")
    implementation ("de.hdodenhof:circleimageview:3.1.0")

    implementation ("androidx.appcompat:appcompat:1.6.1")
    implementation("com.github.florent37:diagonallayout:1.0.7")

    implementation("com.makeramen:roundedimageview:2.3.0")

    implementation("com.google.android.material:material:1.12.0")
    implementation("com.cloudinary:cloudinary-android:3.0.2")

    implementation ("com.squareup.picasso:picasso:2.8")

    implementation("com.github.marlonlom:timeago:4.1.0")

}