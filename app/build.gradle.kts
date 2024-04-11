/**
 * The first section in the build configuration applies the Android Gradle plugin
 * to this build and makes the android block available to specify
 * Android-specific build options.
 */

plugins {
  id("com.android.application")
  id("org.jetbrains.kotlin.android")
  id("com.google.dagger.hilt.android")
  id("com.google.devtools.ksp")
  id("kotlin-parcelize")
}

/**
 * Locate (and possibly download) a JDK used to build your kotlin
 * source code. This also acts as a default for sourceCompatibility,
 * targetCompatibility and jvmTarget. Note that this does not affect which JDK
 * is used to run the Gradle build itself, and does not need to take into
 * account the JDK version required by Gradle plugins (such as the
 * Android Gradle Plugin)
 */

kotlin {
  jvmToolchain(19)
}

/**
 * The android block is where you configure all your Android-specific
 * build options.
 */

android {

  /**
   * The app's namespace. Used primarily to access app resources.
   */

  namespace = "com.conexentools"

  /**
   * compileSdk specifies the Android API level Gradle should use to
   * compile your app. This means your app can use the API features included in
   * this API level and lower.
   */

  compileSdk = 34

  /**
   * The defaultConfig block encapsulates default settings and entries for all
   * build variants and can override some attributes in main/AndroidManifest.xml
   * dynamically from the build system. You can configure product flavors to override
   * these values for different versions of your app.
   */

  defaultConfig {
    // Uniquely identifies the package for publishing.
    applicationId = "com.conexentools"
    testNamespace = "com.conexentools.test"
    // Defines the minimum API level required to run the app.
    minSdk = 26
    // Specifies the API level used to test the app.
    targetSdk = 34
    // Defines the version number of your app.
    versionCode = 1
    // Defines a user-friendly version name for your app.
    versionName = "1.0"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

    android.buildFeatures.buildConfig = true
    buildConfigField("String", "RUNNER", "\"$testInstrumentationRunner\"")
    buildConfigField("String", "TEST_NAMESPACE", "\"$testNamespace\"")
    buildConfigField("String", "TESTED_TM_VERSION_CODE", "\"232583002\"")
    buildConfigField("String", "TESTED_TM_VERSION_NAME", "\"2.23.25.83\"")
    buildConfigField("String", "TESTED_WA_VERSION_NAME", "\"unknown\"")
    buildConfigField("String", "TESTED_WA_VERSION_CODE", "\"unknown\"")
    buildConfigField("String", "LOG_TAG", "\"<<CONEXEN>>\"")

    vectorDrawables {
      useSupportLibrary = true
    }
  }

  /**
   * The buildTypes block is where you can configure multiple build types.
   * By default, the build system defines two build types: debug and release. The
   * debug build type is not explicitly shown in the default build configuration,
   * but it includes debugging tools and is signed with the debug key. The release
   * build type applies ProGuard settings and is not signed by default.
   */

  buildTypes {

    /**
     * By default, Android Studio configures the release build type to enable code
     * shrinking, using minifyEnabled, and specifies the default ProGuard rules file.
     */

    release {
      isMinifyEnabled = false // Enables code shrinking for the release build type.
      proguardFiles(
        getDefaultProguardFile("proguard-android-optimize.txt"),
        "proguard-rules.pro"
      )
    }

    debug {
      isMinifyEnabled = false // Enables code shrinking for the release build type.
    }
//        debug { signingConfig = null }
  }

  /**
   * To override source and target compatibility (if different from the
   * toolchain JDK version), add the following. All of these
   * default to the same value as kotlin.jvmToolchain. If you're using the
   * same version for these values and kotlin.jvmToolchain, you can
   * remove these blocks.
   */

//  compileOptions {
//    sourceCompatibility = JavaVersion.VERSION_1_8
//    targetCompatibility = JavaVersion.VERSION_1_8
//  }
//  kotlinOptions {
//    jvmTarget = "19" //"1.8"
//  }
  composeOptions {
    kotlinCompilerExtensionVersion = "1.5.10"
  }
  buildFeatures {
    compose = true
    viewBinding = true
  }
  packaging {
    resources {
      excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
  }
}

/**
 * The dependencies block in the module-level build configuration file
 * specifies dependencies required to build only the module itself.
 * To learn more, go to https://developer.android.com/studio/build/dependencies.
 */

dependencies {

  // KotlinX Serialization
  implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

  // Dagger Hilt
  val daggerHiltVersion = "2.51.1"
  implementation("com.google.dagger:hilt-android:$daggerHiltVersion")
  ksp("com.google.dagger:hilt-compiler:$daggerHiltVersion")
  implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

  // For instrumentation tests
  androidTestImplementation("com.google.dagger:hilt-android-testing:$daggerHiltVersion")
  kspAndroidTest("com.google.dagger:hilt-compiler:$daggerHiltVersion")

  // For local unit tests
  testImplementation("com.google.dagger:hilt-android-testing:$daggerHiltVersion")
  kspTest("com.google.dagger:hilt-compiler:$daggerHiltVersion")

  // Room
  val roomVersion = "2.6.1"
  implementation("androidx.room:room-runtime:$roomVersion")
  ksp("androidx.room:room-compiler:$roomVersion")
  implementation("androidx.room:room-ktx:$roomVersion")
  implementation("androidx.room:room-paging:$roomVersion")

  // Paging Compose
  val pagingVersion = "3.2.1"
  implementation("androidx.paging:paging-compose:$pagingVersion")
  implementation("androidx.paging:paging-runtime-ktx:$pagingVersion")

  // Coil
  implementation("io.coil-kt:coil-compose:2.6.0")

  implementation("com.github.MshariAlsayari:Request-Permission-Compose:1.2.0")

  //Easy Permissions
  implementation("com.vmadalin:easypermissions-ktx:1.0.0")

  // Message App Bar
//  implementation("com.github.stevdza-san:MessageBarCompose:1.0.8")

//  implementation("com.github.BILLyTheLiTTle:LazyColumns:0.2.7")
  implementation("com.github.nanihadesuka:LazyColumnScrollbar:1.10.0")

  // Libphonenumber
  implementation("com.googlecode.libphonenumber:libphonenumber:8.13.34")

  implementation("me.saket.swipe:swipe:1.3.0")

  // Third-party Contacts API
  implementation("com.github.vestrel00.contacts-android:core:0.3.1")

  implementation("androidx.core:core-ktx:1.12.0")
  implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
//  implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")

  implementation("androidx.activity:activity-compose:1.8.2")
  implementation(platform("androidx.compose:compose-bom:2024.03.00"))
//    implementation("androidx.test:core-ktx:1.5.0")

  androidTestImplementation(platform("androidx.compose:compose-bom:2024.03.00"))
  debugImplementation("androidx.compose.ui:ui-tooling")

  implementation("com.google.guava:guava:33.0.0-jre")
  implementation("androidx.constraintlayout:constraintlayout:2.1.4")

  // Preferences DataStore
  implementation("androidx.datastore:datastore-preferences:1.0.0")

//    implementation("com.codinguser.android:contactpicker:3.0.0@aar")
//    implementation("com.1gravity:android-contactpicker:1.4.0")
  // optional - RxJava2 support
//    implementation("androidx.datastore:datastore-preferences-rxjava2:1.0.0")
  // optional - RxJava3 support
//    implementation("androidx.datastore:datastore-preferences-rxjava3:1.0.0")

  // Material Design 3
  implementation("androidx.compose.material3:material3")
  // Android Studio Preview support
  implementation("androidx.compose.ui:ui")
  implementation("androidx.compose.ui:ui-graphics")
  implementation("androidx.compose.ui:ui-tooling-preview")
  debugImplementation("androidx.compose.ui:ui-tooling")
  // Optional - Add full set of material icons
  //noinspection GradleDependency
  implementation("androidx.compose.material:material-icons-extended:1.6.4")
  // Optional - Add window size utils
  implementation("androidx.compose.material3:material3-window-size-class:1.2.1")
  // Optional - Integration with activities
  implementation("androidx.activity:activity-compose:1.8.2")
  // Optional - Integration with ViewModels
  implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
  // Optional - Integration with LiveData
  implementation("androidx.compose.runtime:runtime-livedata:1.6.4")
  // Optional - Integration with RxJava
  implementation("androidx.compose.runtime:runtime-rxjava2:1.6.4")
  implementation("androidx.wear.compose:compose-material:1.3.0")

  // UI Tests
  androidTestImplementation("androidx.compose.ui:ui-test-junit4")
  debugImplementation("androidx.compose.ui:ui-test-manifest:1.7.0-alpha05")
  // Instrumented Tests
  androidTestImplementation("androidx.test:core:1.6.0-alpha05")
  androidTestImplementation("androidx.test.ext:junit:1.2.0-alpha03")
  androidTestImplementation("androidx.test:runner:1.6.0-alpha06")
  testImplementation("org.testng:testng:7.9.0")
  testImplementation("androidx.test.espresso:espresso-core:3.6.0-alpha03")
  testImplementation("junit:junit:4.13.2")
  // UiAutomator
  androidTestImplementation("androidx.test.uiautomator:uiautomator:2.3.0")
  androidTestImplementation("org.hamcrest:hamcrest-integration:1.3")

  implementation("androidx.appcompat:appcompat:1.6.1")
  implementation("com.google.android.material:material:1.11.0")
  implementation("androidx.constraintlayout:constraintlayout:2.1.4")
  implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.7.0")
  implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
  implementation("androidx.navigation:navigation-fragment-ktx:2.7.7")
  implementation("androidx.navigation:navigation-ui-ktx:2.7.7")
}

//kapt {
//  correctErrorTypes = true
//}