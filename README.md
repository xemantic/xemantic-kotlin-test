# xemantic-kotlin-test
The power-assert compatible assertions DSL and some other testing goodies - a Kotlin multiplatform testing library.

[<img alt="Maven Central Version" src="https://img.shields.io/maven-central/v/com.xemantic.kotlin/xemantic-kotlin-test">](https://central.sonatype.com/namespace/com.xemantic.kotlin)
[<img alt="GitHub Release Date" src="https://img.shields.io/github/release-date/xemantic/xemantic-kotlin-test">](https://github.com/xemantic/xemantic-kotlin-test/releases)
[<img alt="license" src="https://img.shields.io/github/license/xemantic/xemantic-kotlin-test?color=blue">](https://github.com/xemantic/xemantic-kotlin-test/blob/main/LICENSE)

[<img alt="GitHub Actions Workflow Status" src="https://img.shields.io/github/actions/workflow/status/xemantic/xemantic-kotlin-test/build-main.yml">](https://github.com/xemantic/xemantic-kotlin-test/actions/workflows/build-main.yml)
[<img alt="GitHub branch check runs" src="https://img.shields.io/github/check-runs/xemantic/xemantic-kotlin-test/main">](https://github.com/xemantic/xemantic-kotlin-test/actions/workflows/build-main.yml)
[<img alt="GitHub commits since latest release" src="https://img.shields.io/github/commits-since/xemantic/xemantic-kotlin-test/latest">](https://github.com/xemantic/xemantic-kotlin-test/commits/main/)
[<img alt="GitHub last commit" src="https://img.shields.io/github/last-commit/xemantic/xemantic-kotlin-test">](https://github.com/xemantic/xemantic-kotlin-test/commits/main/)

[<img alt="GitHub contributors" src="https://img.shields.io/github/contributors/xemantic/xemantic-kotlin-test">](https://github.com/xemantic/xemantic-kotlin-test/graphs/contributors)
[<img alt="GitHub commit activity" src="https://img.shields.io/github/commit-activity/t/xemantic/xemantic-kotlin-test">](https://github.com/xemantic/xemantic-kotlin-test/commits/main/)
[<img alt="GitHub code size in bytes" src="https://img.shields.io/github/languages/code-size/xemantic/xemantic-kotlin-test">]()
[<img alt="GitHub Created At" src="https://img.shields.io/github/created-at/xemantic/xemantic-kotlin-test">](https://github.com/xemantic/xemantic-kotlin-test/commit/39c1fa4c138d4c671868c973e2ad37b262ae03c2)
[<img alt="kotlin version" src="https://img.shields.io/badge/dynamic/toml?url=https%3A%2F%2Fraw.githubusercontent.com%2Fxemantic%2Fxemantic-kotlin-test%2Fmain%2Fgradle%2Flibs.versions.toml&query=versions.kotlin&label=kotlin">](https://kotlinlang.org/docs/releases.html)
[<img alt="discord users online" src="https://img.shields.io/discord/811561179280965673">](https://discord.gg/vQktqqN2Vn)
[![Bluesky](https://img.shields.io/badge/Bluesky-0285FF?logo=bluesky&logoColor=fff)](https://bsky.app/profile/xemantic.com)

## Why?

I am mostly using [kotest](https://kotest.io/) library for writing test assertions in my projects.
When [power-assert](https://kotlinlang.org/docs/power-assert.html) became the official Kotlin compiler plugin, I also realized that most of the kotest assertions can be replaced with something which suits my needs much better.
Instead of writing:

```kotlin
x shouldBeGreaterThanOrEqualTo 42
```

I could write:

```kotlin
assert(x >= 42)
```

And the power-assert will provide comprehensive error message breaking down the expression written in Kotlin into components, while displaying their values.

I am quite often asserting the state of hierarchical data structures, therefore I came up with such a syntax, following the natural language, and utilizing power-assert:

```kotlin
message should {
    have(id == 42)
    have(content.size == 2)
    content[0] should {
        be<Text>()
        have(type == "text")
        have("Hello" in text)
    }
    content[1] should {
        be<Image>()
        have(type == "image")
        have(width >= 800)
        have(height >= 600)
        mediaType should {
            have(type == "image/png")
        }
    }
}
```

Now, if the image `mediaType.type` is not `PNG`, it will show:

```text
Message(id=42, content=[Text(text=Hello there), Image(path=image.png, width=1024, height=768, mediaType=MediaType(type=image/jpeg))])
 containing:
Image(path=image.png, width=1024, height=768, mediaType=MediaType(type=image/jpeg))
 containing:
MediaType(type=image/jpeg)
 should:
have(type == "image/png")
     |    |
     |    false
     image/jpeg
```

In addition, the library supports:

* uniform access to project test files across non-browser platforms of a KMP project
* access to defined set of environment variables in browser platforms of a KMP project

## Usage

### Setting up Gradle

#### Setting up Gradle for Kotlin Multiplatform project

In your `build.gradle.kts`:

```kotlin
plugins {
    kotlin("multiplatform") version "2.1.0"
    kotlin("plugin.power-assert") version "2.1.0" // replace with the latest kotlin version
}

kotlin {

    sourceSets {

        commonTest {
            depencencies {
                implementation("com.xemantic.kotlin:xemantic-kotlin-test:1.8.10")
            }
        }

    }
}

powerAssert {
    functions = listOf(
        "com.xemantic.kotlin.test.assert",
        "com.xemantic.kotlin.test.have"
    )
}
```

#### Setting up Gradle for Kotlin JVM project

In your `build.gradle.kts`:

```kotlin
plugins {
    kotlin("jvm") version "2.1.0"
    kotlin("plugin.power-assert") version "2.1.0" // replace with the latest kotlin version
}

dependencies {
    testImplementation("com.xemantic.kotlin:xemantic-kotlin-test:1.8.10")
}

powerAssert {
    functions = listOf(
        "com.xemantic.kotlin.test.assert",
        "com.xemantic.kotlin.test.have"
    )
}
```

### Basic Assertions

```kotlin
assert(2 + 2 == 4)
```

> [!NOTE]
> The [assert](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/assert.html) function in Kotlin stdlib is providing `assert` only for `jvm` and `native` out of all the Kotlin multiplatform targets.
> The multiplatform `assert` function can be imported from `com.xemantic.kotlin.test.assert`.

### Asserting object properties

The library introduces [should](src/commonMain/kotlin/Assertions.kt) infix function, which allows you to chain assertions on an object:

```kotlin
someObject should {
    // assertions go here
}
```

### Type Assertions

You can assert the type of object using the [be](src/commonMain/kotlin/Assertions.kt) function:

```kotlin
someObject should {
    be<ExpectedType>()
}
```

> [!TIP]
> After calling `be` function with expected type, all the subsequent calls within `should {}` will have access to the properties of the expected type, like if `this`, representing `someObject`, was cast to the expected type.

### Condition Assertions

Use the `have` function to assert conditions:

```kotlin
someObject should {
    have(someProperty == expectedValue)
}
```

### Nested Assertions

You can nest assertions for complex objects:

```kotlin
complexObject should {
    have(property1 == expectedValue1)
    nestedObject should {
        have(nestedProperty == expectedValue2)
    }
}
```

### Test Context

You can obtain access to the test context like:

* Stable absolute path of the current gradle root dir, so that the test files can be used in tests of non-browser
  platforms of the KMP project.
* Environment variables, accessible on almost all the platforms, including access to predefined set of environment
  variables in tests of browser platforms (e.g. API keys) of the KMP project.

See [TextContext](src/commonMain/kotlin/TestContext.kt) for details.

You have to add to `build.gradle.kts`:

```kotlin
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
```

and specify environment variables you are interested in. The `SIMCTL_CHILD_` is used in tests running inside emulators.

To pass environment variables to browser tests, you have to create `webpack.confg.d` folder and drop this file named `env-config.js`:

```js
const webpack = require("webpack");
const envPlugin = new webpack.DefinePlugin({
    'process': {
        'env': {
            'FOO': JSON.stringify(process.env.FOO)
        }
    }
});
config.plugins.push(envPlugin);
```

Pick environment variables which should be provided to browser tests.

Then you can write test like:

```kotlin
class FooTest {

    @Test
    fun `Should test against test data`() {
        if (isBrowserPlatform) return // we don't have access to Gradle root dir
        val testData = Path(gradleRootDir, "test-data.txt")
        // ...
    }

    @Test
    fun `Should use predefined environment variable`() {
        val apiKey = getEnv("SOME_API_KEY")
        // ...
    }

}
```

## Development

Clone this project, and then run:

```shell
./gradlew build
```
