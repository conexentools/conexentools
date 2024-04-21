// Top-level build file where you can add configuration options common to all sub-projects/modules.

plugins {
    id("com.android.application") version "8.2.1" apply false
    id("com.android.library") version "8.2.1" apply false
    id("org.jetbrains.kotlin.android") version "1.9.22" apply false
    id("com.google.dagger.hilt.android") version "2.51" apply false
    id("com.google.devtools.ksp") version "1.9.23-1.0.19" apply false
}

task("perica") {
    group = "other"
    doLast {
        println("2122222222222")
    }
}

tasks.register("lechuga",
) {
    doLast {
        println("2122222222222")
    }

//  dependsOn("generateVersionCode")
}