# xemantic-kotlin-test

Kotlin multiplatform testing library providing power-assert compatible assertions

## Why?

I am mostly using [kotest](https://kotest.io/) library for writing test assertions
in my projects. When [power-assert](https://kotlinlang.org/docs/power-assert.html)
became the official Kotlin compiler plugin, I also realized that most of the kotest
assertions can be replaced with something which suits my purposes better.
Instead of writing:

```kotlin
x shouldBeGreaterThanOrEqualTo 42
```

I could write:

```kotlin
assert(x >= 5)
```

Unfortunately the [assert](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/assert.html)
function is supported at the moment only for `JVM` and `Native` out of all the Kotlin
multiplatform targets. So for my multiplatform libraries it would rather be
[assertTrue](https://kotlinlang.org/api/core/kotlin-test/kotlin.test/assert-true.html), but
... it is becoming too verbose.

Quite often I am asserting the state of hierarchical data
structures, therefore I came up with this syntax:

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
        implementation("com.xemantic.kotlin:xemantic-kotlin-test:0.1.1")
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
  testImplementation("com.xemantic.kotlin:xemantic-kotlin-test:0.1.1")
}

powerAssert {
  functions = listOf(
    "com.xemantic.kotlin.test.have"
  )
}
```

### Basic Assertions

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

## Nested Assertions

You can nest assertions for complex objects:

```kotlin
complexObject should {
  have(property1 == expectedValue1)
  nestedObject should {
    have(nestedProperty == expectedValue2)
  }
}
```
