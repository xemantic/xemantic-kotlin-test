/*
 * Copyright 2024-2025 Kazimierz Pogoda / Xemantic
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

    @Test
    fun `Should not read undefined environment variable`() {
        assert(getEnv("BAR") == null)
    }

}
