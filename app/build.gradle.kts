import org.gradle.api.tasks.testing.Test
import org.gradle.testing.jacoco.tasks.JacocoReport

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.ksp)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.hilt)
    jacoco
}

jacoco {
    toolVersion = libs.versions.jacoco.get()
}

android {
    namespace = "com.zhuji.note"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.zhuji.note"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"
        testInstrumentationRunner = "com.zhuji.note.HiltTestRunner"
        vectorDrawables { useSupportLibrary = true }
    }

    buildTypes {
        debug { enableUnitTestCoverage = true; enableAndroidTestCoverage = true }
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
    buildFeatures { compose = true }
    composeOptions { kotlinCompilerExtensionVersion = "1.5.14" }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "META-INF/LICENSE.md"
            excludes += "META-INF/LICENSE-notice.md"
        }
    }
    testOptions {
        animationsDisabled = true
        unitTests.isIncludeAndroidResources = true
        unitTests.isReturnDefaultValues = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.splashscreen)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.animation)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    implementation(libs.androidx.navigation.compose)

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    implementation(libs.androidx.datastore)
    implementation(libs.androidx.security.crypto)

    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.android)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)

    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)

    implementation(libs.coil.compose)
    implementation(libs.compose.markdown)
    implementation(libs.lottie.compose)

    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
    testImplementation(libs.truth)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.test.ext.junit)
    testImplementation(libs.androidx.test.core.ktx)
    testImplementation(libs.androidx.arch.core.testing)
    testImplementation(libs.okhttp.mockwebserver)
    testImplementation(libs.androidx.room.testing)

    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.rules)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.espresso.contrib)
    androidTestImplementation(libs.espresso.intents)
    androidTestImplementation(libs.ui.automator)
    androidTestImplementation(libs.hilt.android.testing)
    kspAndroidTest(libs.hilt.compiler)
    androidTestImplementation(libs.androidx.navigation.testing)
    androidTestImplementation(libs.mockk.android)
    androidTestImplementation(libs.kotlinx.coroutines.test)
}

// ============ JaCoCo Three-Stage Coverage ============
val coverageStages = listOf("stage1", "stage2", "stage3")
val coveragePackages = mapOf(
    "stage1" to "com.zhuji.note.stage1.*",
    "stage2" to "com.zhuji.note.stage2.*",
    "stage3" to "com.zhuji.note.stage3.*",
)
val coverageExcludes = listOf(
    "**/R.class", "**/R\$*.class", "**/BuildConfig.*", "**/Manifest*.*",
    "**/*Hilt_*.*", "**/Hilt_*.*", "**/dagger/hilt/**", "**/hilt_aggregated_deps/**",
    "**/*_HiltModules.*", "**/*_HiltModules*", "**/*_Factory.*", "**/*_MembersInjector.*",
    "**/*_Provide*Factory*.*", "**/DataBinderMapperImpl.*", "**/*_Impl.*", "**/*_Impl\$*.*",
    "**/*ComposableSingletons*.*", "**/ComposableSingletons*.*",
    "**/di/**", "**/*\$\$serializer.*", "**/ZhujiNoteApp*.*",
    "**/MainActivity*.*"
)

afterEvaluate {
    val unitTestTask = tasks.named<Test>("testDebugUnitTest").get()
    coverageStages.forEach { stage ->
        val capitalized = stage.replaceFirstChar { it.uppercaseChar() }
        val execFile = layout.buildDirectory.file("jacoco/test${capitalized}.exec")

        val stageTest = tasks.register<Test>("test${capitalized}") {
            description = "Run $stage tests"
            group = "verification"
            testClassesDirs = unitTestTask.testClassesDirs
            classpath = unitTestTask.classpath
            useJUnit()
            filter { includeTestsMatching(coveragePackages.getValue(stage)) }
            extensions.configure(JacocoTaskExtension::class.java) {
                destinationFile = execFile.get().asFile
                includes = listOf("com.zhuji.note.*")
                isIncludeNoLocationClasses = true
                excludes = listOf("jdk.internal.*")
            }
        }

        tasks.register<JacocoReport>("jacoco${capitalized}Report") {
            description = "Generate $stage JaCoCo report"
            group = "reporting"
            dependsOn(stageTest)
            reports {
                xml.required.set(true)
                html.required.set(true)
                csv.required.set(false)
                xml.outputLocation.set(layout.buildDirectory.file("reports/coverage/$stage/coverage.xml"))
                html.outputLocation.set(layout.buildDirectory.dir("reports/coverage/$stage/html"))
            }
            sourceDirectories.setFrom(files("$projectDir/src/main/java", "$projectDir/src/main/kotlin"))
            classDirectories.setFrom(
                fileTree("${layout.buildDirectory.get().asFile}/tmp/kotlin-classes/debug") { exclude(coverageExcludes) },
                fileTree("${layout.buildDirectory.get().asFile}/intermediates/javac/debug/classes") { exclude(coverageExcludes) }
            )
            executionData.setFrom(execFile)
        }
    }

    tasks.register<JacocoReport>("jacocoCumulativeReport") {
        description = "Cumulative JaCoCo coverage from all 3 stages"
        group = "reporting"
        dependsOn(coverageStages.map { tasks.named("test${it.replaceFirstChar { c -> c.uppercaseChar() }}") })
        reports {
            xml.required.set(true)
            html.required.set(true)
            xml.outputLocation.set(layout.buildDirectory.file("reports/coverage/cumulative/coverage.xml"))
            html.outputLocation.set(layout.buildDirectory.dir("reports/coverage/cumulative/html"))
        }
        sourceDirectories.setFrom(files("$projectDir/src/main/java", "$projectDir/src/main/kotlin"))
        classDirectories.setFrom(
            fileTree("${layout.buildDirectory.get().asFile}/tmp/kotlin-classes/debug") { exclude(coverageExcludes) },
            fileTree("${layout.buildDirectory.get().asFile}/intermediates/javac/debug/classes") { exclude(coverageExcludes) }
        )
        executionData.setFrom(coverageStages.map { layout.buildDirectory.file("jacoco/test${it.replaceFirstChar { c -> c.uppercaseChar() }}.exec") })
    }
}
