# xemantic-kotlin-test

An AX-first (AI/Agent Experience) Kotlin multiplatform testing library with power-assert compatible assertions DSL and other testing goodies.

[<img alt="Maven Central Version" src="https://img.shields.io/maven-central/v/com.xemantic.kotlin/xemantic-kotlin-test">](https://central.sonatype.com/artifact/com.xemantic.kotlin/xemantic-kotlin-test)
[<img alt="GitHub Release Date" src="https://img.shields.io/github/release-date/xemantic/xemantic-kotlin-test">](https://github.com/xemantic/xemantic-kotlin-test/releases)
[<img alt="license" src="https://img.shields.io/github/license/xemantic/xemantic-kotlin-test?color=blue">](https://github.com/xemantic/xemantic-kotlin-test/blob/main/LICENSE)

[<img alt="GitHub Actions Workflow Status" src="https://img.shields.io/github/actions/workflow/status/xemantic/xemantic-kotlin-test/build-main.yml">](https://github.com/xemantic/xemantic-kotlin-test/actions/workflows/build-main.yml)
[<img alt="GitHub branch check runs" src="https://img.shields.io/github/check-runs/xemantic/xemantic-kotlin-test/main">](https://github.com/xemantic/xemantic-kotlin-test/actions/workflows/build-main.yml)
[<img alt="GitHub commits since latest release" src="https://img.shields.io/github/commits-since/xemantic/xemantic-kotlin-test/latest">](https://github.com/xemantic/xemantic-kotlin-test/commits/main/)
[<img alt="GitHub last commit" src="https://img.shields.io/github/last-commit/xemantic/xemantic-kotlin-test">](https://github.com/xemantic/xemantic-kotlin-test/commits/main/)

[<img alt="GitHub contributors" src="https://img.shields.io/github/contributors/xemantic/xemantic-kotlin-test">](https://github.com/xemantic/xemantic-kotlin-test/graphs/contributors)
[<img alt="GitHub commit activity" src="https://img.shields.io/github/commit-activity/t/xemantic/xemantic-kotlin-test">](https://github.com/xemantic/xemantic-kotlin-test/commits/main/)
[<img alt="GitHub code size in bytes" src="https://img.shields.io/github/languages/code-size/xemantic/xemantic-kotlin-test">]()
[<img alt="GitHub Created At" src="https://img.shields.io/github/created-at/xemantic/xemantic-kotlin-test">](https://github.com/xemantic/xemantic-kotlin-test/commits)
[<img alt="kotlin version" src="https://img.shields.io/badge/dynamic/toml?url=https%3A%2F%2Fraw.githubusercontent.com%2Fxemantic%2Fxemantic-kotlin-test%2Fmain%2Fgradle%2Flibs.versions.toml&query=versions.kotlin&label=kotlin">](https://kotlinlang.org/docs/releases.html)
[<img alt="discord users online" src="https://img.shields.io/discord/811561179280965673">](https://discord.gg/vQktqqN2Vn)
[![Bluesky](https://img.shields.io/badge/Bluesky-0285FF?logo=bluesky&logoColor=fff)](https://bsky.app/profile/xemantic.com)

## Why?

If you're writing code with AI agents, you've likely discovered that LLMs produce their best work when we reduce cognitive load. When meaning can be conveyed concisely in a semi-natural language flow, results improve dramatically. Markdown typically outperforms even minimal HTML because it's less cluttered with style and formatting noise. LLMs struggle when forced to multitask—like generating code while simultaneously escaping it for JSON. This library is AX-first: designed to minimize cognitive load in test cases, which also makes them excellent as model evals.

My typical workflow looks like this:
1. Write a single test case using this library
2. Describe the problem domain and ask an AI agent to generate a comprehensive set of test cases following the same conventions
3. Review, refine, and add edge cases until coverage is satisfactory
4. In a fresh context window, have the agent implement the functionality

Sometimes it takes minutes, sometimes it takes hours. An agent can produce thousands of lines of code, run tests, and try to fix errors in a long feedback loop session. Quite often I don't even look much at the implementation, trusting our shared TDD approach.

In agentic loops, error reporting needs to be spot-on—providing maximum precision with minimal tokens. Typical unit test assertion libraries are designed for humans, not AI agents. It's easy for us to interpret a standard `assertEquals` failure rendered nicely in IntelliJ, but LLMs don't process this output the same way. This is why the library uses unified diff-based output when assertions fail, giving LLMs precise information on how to correct themselves.

In the past I've been mostly using [kotest](https://kotest.io/) library for writing test assertions in my projects.
When [power-assert](https://kotlinlang.org/docs/power-assert.html) became the official Kotlin compiler plugin, I also realized that most of the kotest assertions can be replaced with something which suits my needs much better, while being even easier for machines to digest. Instead of writing:

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
        have("Hello" in text)
    }
    content[1] should {
        be<Image>()
        have(width >= 800 && height >= 600)
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
    kotlin("multiplatform") version "2.2.20"
    kotlin("plugin.power-assert") version "2.2.20" // replace with the latest kotlin version
}

kotlin {

    sourceSets {

        commonTest {
            depencencies {
                implementation("com.xemantic.kotlin:xemantic-kotlin-test:1.14.1")
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
    kotlin("jvm") version "2.2.20"
    kotlin("plugin.power-assert") version "2.2.20" // replace with the latest kotlin version
}

dependencies {
    testImplementation("com.xemantic.kotlin:xemantic-kotlin-test:1.14.1")
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

### String Comparison with Unified Diff

For comparing strings, especially multiline text, use the `sameAs` function which provides unified diff output when strings don't match:

```kotlin
actualString sameAs expectedString
```

When the strings differ, `sameAs` produces clear diff output similar to git diff:

```text
--- expected
+++ actual
@@ -1,3 +1,3 @@
 line1
-expected line2
+actual line2
 line3
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
    fun `should test against test data`() {
        if (isBrowserPlatform) return // we don't have access to Gradle root dir
        val testData = Path(gradleRootDir, "test-data.txt")
        // ...
    }

    @Test
    fun `should use predefined environment variable`() {
        val apiKey = getEnv("SOME_API_KEY")
        // ...
    }

}
```

## Test failure reporting designed for AI agents

An AI-friendly test failure reporting can be configured with the [xemantic-conventions](https://github.com/xemantic/xemantic-conventions) gradle plugin, designed to work together with this library. AI-first asserts, together with XML-wrapped failure reporting, allow an autonomous AI agent to perform Test Driven Development (TDD) in a feedback loop, retaining maximal context while avoidng context rot with minimal noise.

The [ProjectDocumentationTest](src/commonTest/kotlin/ProjectDocumentationTest.kt):

```kotlin
package com.xemantic.kotlin.test

import kotlin.test.Test

class ProjectDocumentationTest {

    @Test
    fun `foo equals bar`() {
        assert("foo" == "bar")
    }

    @Test
    fun `foo sameAs bar`() {
        "foo" sameAs "bar"
    }

}
```

when run, will produce:

```
> Task :jvmTest FAILED
<test-failure test="com.xemantic.kotlin.test.ProjectDocumentationTest.foo sameAs bar()" platform="jvm">
<message>
--- expected
+++ actual
@@ -1,1 +1,1 @@
-bar
\ No newline at end of file
+foo
\ No newline at end of file
</message>
<stacktrace>
  at app//org.junit.jupiter.api.AssertionUtils.fail(AssertionUtils.java:38)
  at app//org.junit.jupiter.api.Assertions.fail(Assertions.java:138)
  at app//kotlin.test.junit5.JUnit5Asserter.fail(JUnitSupport.kt:56)
  at app//kotlin.test.AssertionsKt__AssertionsKt.fail(Assertions.kt:562)
  at app//kotlin.test.AssertionsKt.fail(Unknown Source)
  at app//com.xemantic.kotlin.test.SameAsKt.sameAs(SameAs.kt:37)
  at app//com.xemantic.kotlin.test.ProjectDocumentationTest.foo sameAs bar(ProjectDocumentationTest.kt:30)
  at java.base@24.0.2/java.lang.reflect.Method.invoke(Method.java:565)
  at java.base@24.0.2/java.util.ArrayList.forEach(ArrayList.java:1604)
  at java.base@24.0.2/java.util.ArrayList.forEach(ArrayList.java:1604)
</stacktrace>
</test-failure>
ProjectDocumentationTest[jvm] > foo sameAs bar()[jvm] FAILED
<test-failure test="com.xemantic.kotlin.test.ProjectDocumentationTest.foo equals bar()" platform="jvm">
<message>
assert("foo" == "bar")
             |
             false
</message>
<stacktrace>
  at app//org.junit.jupiter.api.AssertionUtils.fail(AssertionUtils.java:38)
  at app//org.junit.jupiter.api.Assertions.fail(Assertions.java:138)
  at app//kotlin.test.junit5.JUnit5Asserter.fail(JUnitSupport.kt:56)
  at app//kotlin.test.Asserter.assertTrue(Assertions.kt:694)
  at app//kotlin.test.junit5.JUnit5Asserter.assertTrue(JUnitSupport.kt:30)
  at app//kotlin.test.Asserter.assertTrue(Assertions.kt:704)
  at app//kotlin.test.junit5.JUnit5Asserter.assertTrue(JUnitSupport.kt:30)
  at app//com.xemantic.kotlin.test.AssertionsKt.assert(Assertions.kt:32)
  at app//com.xemantic.kotlin.test.ProjectDocumentationTest.foo equals bar(ProjectDocumentationTest.kt:25)
  at java.base@24.0.2/java.lang.reflect.Method.invoke(Method.java:565)
  at java.base@24.0.2/java.util.ArrayList.forEach(ArrayList.java:1604)
  at java.base@24.0.2/java.util.ArrayList.forEach(ArrayList.java:1604)
</stacktrace>
</test-failure>
ProjectDocumentationTest[jvm] > foo equals bar()[jvm] FAILED
```

For the `wasmJs` platform it will produce:

```
com.xemantic.kotlin.test.ProjectDocumentationTest.foo equals bar[wasmJs, node] FAILED
com.xemantic.kotlin.test.ProjectDocumentationTest.foo sameAs bar[wasmJs, node] FAILED
> Task :wasmJsNodeTest FAILED
<test-failure test="com.xemantic.kotlin.test.ProjectDocumentationTest.foo equals bar" platform="wasmJsNode">
<message>

assert("foo" == "bar")
             |
             false
</message>
<stacktrace>
  at kotlin.createJsError(file:///Users/morisil/git/xemantic/xemantic-kotlin-test/build/wasm/packages/xemantic-kotlin-test-test/kotlin/xemantic-kotlin-test-test.uninstantiated.mjs:19)
  at com.xemantic.kotlin:xemantic-kotlin-test_test.kotlin.createJsError__externalAdapter(wasm://wasm/com.xemantic.kotlin:xemantic-kotlin-test_test-006452c2)
  at com.xemantic.kotlin:xemantic-kotlin-test_test.kotlin.Throwable.<init>(wasm://wasm/com.xemantic.kotlin:xemantic-kotlin-test_test-006452c2)
  at com.xemantic.kotlin:xemantic-kotlin-test_test.kotlin.Error.<init>(wasm://wasm/com.xemantic.kotlin:xemantic-kotlin-test_test-006452c2)
  at com.xemantic.kotlin:xemantic-kotlin-test_test.kotlin.AssertionError.<init>(wasm://wasm/com.xemantic.kotlin:xemantic-kotlin-test_test-006452c2)
  at com.xemantic.kotlin:xemantic-kotlin-test_test.kotlin.test.DefaultWasmAsserter.assertTrue(wasm://wasm/com.xemantic.kotlin:xemantic-kotlin-test_test-006452c2)
  at com.xemantic.kotlin:xemantic-kotlin-test_test.kotlin.test.DefaultWasmAsserter.assertTrue(wasm://wasm/com.xemantic.kotlin:xemantic-kotlin-test_test-006452c2)
  at com.xemantic.kotlin:xemantic-kotlin-test_test.com.xemantic.kotlin.test.assert(wasm://wasm/com.xemantic.kotlin:xemantic-kotlin-test_test-006452c2)
  at com.xemantic.kotlin:xemantic-kotlin-test_test.com.xemantic.kotlin.test.ProjectDocumentationTest.foo equals bar(wasm://wasm/com.xemantic.kotlin:xemantic-kotlin-test_test-006452c2)
  at ref.invoke(wasm://wasm/com.xemantic.kotlin:xemantic-kotlin-test_test-006452c2)
</stacktrace>
</test-failure>
<test-failure test="com.xemantic.kotlin.test.ProjectDocumentationTest.foo sameAs bar" platform="wasmJsNode">
<message>
--- expected
+++ actual
@@ -1,1 +1,1 @@
-bar
\ No newline at end of file
+foo
\ No newline at end of file
</message>
<stacktrace>
  at kotlin.createJsError(file:///Users/morisil/git/xemantic/xemantic-kotlin-test/build/wasm/packages/xemantic-kotlin-test-test/kotlin/xemantic-kotlin-test-test.uninstantiated.mjs:19)
  at com.xemantic.kotlin:xemantic-kotlin-test_test.kotlin.createJsError__externalAdapter(wasm://wasm/com.xemantic.kotlin:xemantic-kotlin-test_test-006452c2)
  at com.xemantic.kotlin:xemantic-kotlin-test_test.kotlin.Throwable.<init>(wasm://wasm/com.xemantic.kotlin:xemantic-kotlin-test_test-006452c2)
  at com.xemantic.kotlin:xemantic-kotlin-test_test.kotlin.Error.<init>(wasm://wasm/com.xemantic.kotlin:xemantic-kotlin-test_test-006452c2)
  at com.xemantic.kotlin:xemantic-kotlin-test_test.kotlin.AssertionError.<init>(wasm://wasm/com.xemantic.kotlin:xemantic-kotlin-test_test-006452c2)
  at com.xemantic.kotlin:xemantic-kotlin-test_test.kotlin.test.DefaultWasmAsserter.fail(wasm://wasm/com.xemantic.kotlin:xemantic-kotlin-test_test-006452c2)
  at com.xemantic.kotlin:xemantic-kotlin-test_test.kotlin.test.DefaultWasmAsserter.fail(wasm://wasm/com.xemantic.kotlin:xemantic-kotlin-test_test-006452c2)
  at com.xemantic.kotlin:xemantic-kotlin-test_test.kotlin.test.fail(wasm://wasm/com.xemantic.kotlin:xemantic-kotlin-test_test-006452c2)
  at com.xemantic.kotlin:xemantic-kotlin-test_test.com.xemantic.kotlin.test.sameAs(wasm://wasm/com.xemantic.kotlin:xemantic-kotlin-test_test-006452c2)
  at com.xemantic.kotlin:xemantic-kotlin-test_test.com.xemantic.kotlin.test.ProjectDocumentationTest.foo sameAs bar(wasm://wasm/com.xemantic.kotlin:xemantic-kotlin-test_test-006452c2)
</stacktrace>
</test-failure>
2 tests completed, 2 failed
```

## Development

Clone this project, and then run:

```shell
./gradlew build
```
