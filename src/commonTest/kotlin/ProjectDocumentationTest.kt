/*
 * Copyright 2025 Kazimierz Pogoda / Xemantic
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.xemantic.kotlin.test

import kotlin.test.Test

/**
 * A documentation fixture, not a real test.
 *
 * Both test cases are meant to fail, to showcase the AI-friendly failure
 * reporting described in the
 * [README](https://github.com/xemantic/xemantic-kotlin-test#test-failure-reporting-designed-for-ai-agents),
 * which quotes this class together with the output it produces.
 *
 * The failure messages are rendered by the power-assert compiler plugin,
 * and the XML wrapping comes from the
 * [xemantic-conventions](https://github.com/xemantic/xemantic-conventions)
 * gradle plugin, so both can change with a Kotlin or a plugin upgrade.
 * When they do, the README snippets have to be regenerated - see
 * [DEVELOPMENT.md](https://github.com/xemantic/xemantic-kotlin-test/blob/main/DEVELOPMENT.md#documentation-snippets).
 *
 * Excluded from the regular build by a test filter in
 * [build.gradle.kts](https://github.com/xemantic/xemantic-kotlin-test/blob/main/build.gradle.kts),
 * so that the build stays green, and no `@Ignore` pollutes this class,
 * which is quoted in the README as-is.
 */
class ProjectDocumentationTest {

    @Test
    @Suppress("SimplifyBooleanWithConstants")
    fun `foo equals bar`() {
        assert("foo" == "bar")
    }

    @Test
    fun `foo sameAs bar`() {
        "foo" sameAs "bar"
    }

}
