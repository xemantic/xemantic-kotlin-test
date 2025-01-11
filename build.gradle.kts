@file:OptIn(ExperimentalWasmDsl::class, ExperimentalKotlinGradlePluginApi::class)

import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import org.jetbrains.kotlin.gradle.swiftexport.ExperimentalSwiftExportDsl
import org.jetbrains.kotlin.gradle.targets.js.testing.KotlinJsTest
import org.jetbrains.kotlin.gradle.targets.jvm.tasks.KotlinJvmTest
import org.jetbrains.kotlin.gradle.targets.native.tasks.KotlinNativeTest
import org.jreleaser.model.Active
import java.time.LocalDate

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.plugin.power.assert)
    alias(libs.plugins.kotlinx.binary.compatibility.validator)
    alias(libs.plugins.dokka)
    alias(libs.plugins.versions)
    `maven-publish`
    signing
    alias(libs.plugins.jreleaser)
}

data class Settings(
    val description: String,
    val gitHubAccount: String,
    val copyright: String
)

val settings = Settings(
    description = "The power-assert compatible assertions DSL and some other testing goodies - a Kotlin multiplatform testing library",
    gitHubAccount = "xemantic",
    copyright = "(c) ${LocalDate.now().year} Xemantic"
)

val javaTarget = libs.versions.javaTarget.get()
val kotlinTarget = KotlinVersion.fromVersion(libs.versions.kotlinTarget.get())

val isReleaseBuild = !project.version.toString().endsWith("-SNAPSHOT")
val githubActor: String? by project
val githubToken: String? by project
val signingKey: String? by project
val signingPassword: String? by project

println(
"""
+--------------------------------------------  
| Project: ${project.name}
| Version: ${project.version}
| Release build: $isReleaseBuild
+--------------------------------------------
"""
)

repositories {
    mavenCentral()
}

val gradleRootDir: String = rootDir.absolutePath
val fooValue = "bar"

tasks.withType<KotlinJvmTest>().configureEach {
    environment("GRADLE_ROOT_DIR", gradleRootDir)
    environment("FOO", fooValue)
}

tasks.withType<KotlinJsTest>().configureEach {
    environment("GRADLE_ROOT_DIR", gradleRootDir)
    environment("FOO", fooValue)
}

tasks.withType<KotlinNativeTest>().configureEach {
    environment("GRADLE_ROOT_DIR", gradleRootDir)
    environment("SIMCTL_CHILD_GRADLE_ROOT_DIR", gradleRootDir)
    environment("FOO", fooValue)
    environment("SIMCTL_CHILD_FOO", fooValue)
}

kotlin {

    explicitApi()

    compilerOptions {
        apiVersion = kotlinTarget
        languageVersion = kotlinTarget
        freeCompilerArgs.add("-Xmulti-dollar-interpolation")
        extraWarnings.set(true)
        progressiveMode = true
    }

    jvm {
        // set up according to https://jakewharton.com/gradle-toolchains-are-rarely-a-good-idea/
        compilerOptions {
            apiVersion = kotlinTarget
            languageVersion = kotlinTarget
            jvmTarget = JvmTarget.fromTarget(javaTarget)
            freeCompilerArgs.add("-Xjdk-release=$javaTarget")
            progressiveMode = true
        }
    }

    js {
        browser()
        nodejs()
        binaries.library()
    }

    wasmJs {
        browser()
        nodejs()
        //d8()
        binaries.library()
    }

    wasmWasi {
        nodejs()
        binaries.library()
    }

    // native, see https://kotlinlang.org/docs/native-target-support.html
    // tier 1
    macosX64()
    macosArm64()
    iosSimulatorArm64()
    iosX64()
    iosArm64()

    // tier 2
    linuxX64()
    linuxArm64()
    watchosSimulatorArm64()
    watchosX64()
    watchosArm32()
    watchosArm64()
    tvosSimulatorArm64()
    tvosX64()
    tvosArm64()

    // tier 3
    androidNativeArm32()
    androidNativeArm64()
    androidNativeX86()
    androidNativeX64()
    mingwX64()
    watchosDeviceArm64()

    @OptIn(ExperimentalSwiftExportDsl::class)
    swiftExport {}

    sourceSets {

        commonMain {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }

    }

}

// skip tests which require XCode components to be installed
tasks.named("tvosSimulatorArm64Test") { enabled = false }
tasks.named("watchosSimulatorArm64Test") { enabled = false }
// skip tests for which system environment variable retrival is not implemented at the moment
tasks.named("wasmWasiNodeTest") { enabled = false }
// skip tests which for some reason stale
tasks.named("wasmJsBrowserTest") { enabled = false }

tasks.withType<Test> {
    testLogging {
        events(
            TestLogEvent.SKIPPED,
            TestLogEvent.FAILED
        )
        showStackTraces = true
        exceptionFormat = TestExceptionFormat.FULL
    }
}

powerAssert {
    functions = listOf(
        "com.xemantic.kotlin.test.assert",
        "com.xemantic.kotlin.test.have"
    )
}

// https://kotlinlang.org/docs/dokka-migration.html#adjust-configuration-options
dokka {
    pluginsConfiguration.html {
        footerMessage.set(settings.copyright)
    }
}

val javadocJar by tasks.registering(Jar::class) {
    archiveClassifier.set("javadoc")
    from(tasks.dokkaGeneratePublicationHtml)
}

val stagingDeployDir = layout.buildDirectory.dir("staging-deploy").get().asFile

publishing {
    repositories {
        if (isReleaseBuild) {
            maven {
                url = stagingDeployDir.toURI()
            }
        } else {
            maven {
                name = "GitHubPackages"
                setUrl("https://maven.pkg.github.com/${settings.gitHubAccount}/${rootProject.name}")
                credentials {
                    username = githubActor
                    password = githubToken
                }
            }
        }
    }
    publications {
        withType<MavenPublication> {
            artifact(javadocJar)
            pom { setUpPomDetails() }
        }
    }
}

if (isReleaseBuild) {

    tasks.named("jreleaserDeploy").configure {
        mustRunAfter("publish")
    }

    stagingDeployDir.mkdirs()

    // fixes https://github.com/jreleaser/jreleaser/issues/1292
    layout.buildDirectory.dir("jreleaser").get().asFile.mkdir()

    // workaround for KMP/gradle signing issue
    // https://github.com/gradle/gradle/issues/26091
    tasks {
        withType<PublishToMavenRepository> {
            dependsOn(withType<Sign>())
        }
    }

    // Resolves issues with .asc task output of the sign task of native targets.
    // See: https://github.com/gradle/gradle/issues/26132
    // And: https://youtrack.jetbrains.com/issue/KT-46466
    tasks.withType<Sign>().configureEach {
        val pubName = name.removePrefix("sign").removeSuffix("Publication")

        // These tasks only exist for native targets, hence findByName() to avoid trying to find them for other targets

        // Task ':linkDebugTest<platform>' uses this output of task ':sign<platform>Publication' without declaring an explicit or implicit dependency
        tasks.findByName("linkDebugTest$pubName")?.let {
            mustRunAfter(it)
        }
        // Task ':compileTestKotlin<platform>' uses this output of task ':sign<platform>Publication' without declaring an explicit or implicit dependency
        tasks.findByName("compileTestKotlin$pubName")?.let {
            mustRunAfter(it)
        }
    }

    signing {
        useInMemoryPgpKeys(
            signingKey,
            signingPassword
        )
        sign(publishing.publications)
    }

    jreleaser {
        project {
            description = settings.description
            copyright = settings.copyright
        }
        deploy {
            maven {
                mavenCentral {
                    create("maven-central") {
                        applyMavenCentralRules = false // Already checked
                        verifyPom = false
                        active = Active.ALWAYS
                        url = "https://central.sonatype.com/api/v1/publisher"
                        maxRetries = 240
                        stagingRepository(stagingDeployDir.path)
                    }
                }
            }
        }
        release {
            github {
                // we are releasing through GitHub UI
                skipRelease = true
                skipTag = true
            }
        }
    }

}

fun MavenPom.setUpPomDetails() {
    name = rootProject.name
    description = settings.description
    url = "https://github.com/${settings.gitHubAccount}/${rootProject.name}"
    inceptionYear = "2024"
    organization {
        name = "Xemantic"
        url = "https://xemantic.com"
    }
    licenses {
        license {
            name = "The Apache Software License, Version 2.0"
            url = "http://www.apache.org/licenses/LICENSE-2.0.txt"
            distribution = "repo"
        }
    }
    scm {
        url = "https://github.com/${settings.gitHubAccount}/${rootProject.name}"
        connection = "scm:git:git:github.com/${settings.gitHubAccount}/${rootProject.name}.git"
        developerConnection = "scm:git:https://github.com/${settings.gitHubAccount}/${rootProject.name}.git"
    }
    ciManagement {
        system = "GitHub"
        url = "https://github.com/${settings.gitHubAccount}/${rootProject.name}/actions"
    }
    issueManagement {
        system = "GitHub"
        url = "https://github.com/${settings.gitHubAccount}/${rootProject.name}/issues"
    }
    developers {
        developer {
            id = "morisil"
            name = "Kazik Pogoda"
            email = "morisil@xemantic.com"
        }
    }
}
