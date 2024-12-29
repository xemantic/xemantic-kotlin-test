# xemantic-kotlin-test

Kotlin multiplatform testing library providing power-assert compatible DSL and assertions and some other goodies.

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

Next to this, I am quite often asserting the state of hierarchical data
structures, therefore I came up with such a syntax:

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
* uniform access to project test files across non-browser platforms
* access to defined set of environment variables in browser platforms

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
        implementation("com.xemantic.kotlin:xemantic-kotlin-test:1.0")
      }
    }

  }
}

powerAssert {
  functions = listOf(
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
  testImplementation("com.xemantic.kotlin:xemantic-kotlin-test:1.0")
}

powerAssert {
  functions = listOf(
    "com.xemantic.kotlin.test.have"
  )
}
```

### Basic Assertions

```kotlin
assert(2 + 2 == 4)
```

> [!NOTE]
> The [assert](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/assert.html)
> function in Kotlin stdlib is providing `assert` only for `JVM` and `Native` out of all the Kotlin
> multiplatform targets. The multiplatform `assert` function can be
> imported from `com.xemantic.kotlin.test.assert`

### Asserting object properties

The library introduces the [should](src/commonMain/kotlin/Assertions.kt) infix function, which allows you to chain assertions on an object:

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
> After calling `be` function with expected type, all the subsequent calls within 
> `should {}` will have access to the properties of the expected type,
> like if `this`, representing `someObject`, was cast to the expected type.

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

You can obtain access to test context like:

* Stable absolute path of the current gradle root dir, so that the test files can be used in tests of non-browser platforms.
* Environment variables, accessible on almost all the platforms, including access to predefined set of environment variables in tests of browser platforms (e.g. API keys).

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

To pass environment variables to browser tests, you have to create `webpack.confg.d` folder and drop this file name `env-config.js`:

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
class TestContextTest {

  @Test
  fun `Should read gradleRootDir`() {
    if (isBrowserPlatform) return // we don't have access to Gradle root dir
    assert(gradleRootDir.isNotEmpty())
  }

  @Test
  fun `Should read predefined environment variable`() {
    assert(getEnv("FOO") == "bar")
  }

}
```

## Development

Clone this project, and then run:

```shell
./gradlew build
```
