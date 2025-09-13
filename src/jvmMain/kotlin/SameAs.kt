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

import com.github.difflib.DiffUtils
import com.github.difflib.UnifiedDiffUtils

/**
 * Asserts that this string is the same as the [expected] string.
 * If they differ, throws an [AssertionError] with a unified diff format
 * showing the differences, making it easy for LLMs to understand the changes.
 *
 * @param expected the expected string.
 * @throws AssertionError if the strings are not equal, with unified diff output.
 */
public infix fun String?.sameAs(expected: String?) {

    if (this == expected) return
    requireNotNull(this)
    requireNotNull(expected)

    val actualLines = if (this.isEmpty()) listOf("(empty)") else this.lines()
    val expectedLines = if (expected.isEmpty()) listOf("(empty)") else expected.lines()

    val patch = DiffUtils.diff(expectedLines, actualLines)
    val unifiedDiff = UnifiedDiffUtils.generateUnifiedDiff(
        "expected",
        "actual",
        expectedLines,
        patch,
        0
    )

    throw AssertionError(unifiedDiff.joinToString("\n"))
}
