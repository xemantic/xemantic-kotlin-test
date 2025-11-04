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

import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlin.test.fail

/**
 * Asserts that this JSON string is the same as the [expected] JSON.
 * If they differ, throws an [AssertionError] with a unified diff format
 * showing the differences, making it easy for LLMs to understand the changes.
 *
 * Note: this (actual) string will always be prettified before comparison.
 *
 * ```
 * """{"foo":"bar"}""" sameAsJson """
 *     {
 *       "foo": "bar"
 *     }
 * """.trimIndent()
 * ```
 *
 * @param expected the expected string.
 * @throws AssertionError if the strings are not equal, with unified diff output.
 * @throws IllegalArgumentException if the expected JSON is malformed.
 */
public infix fun String?.sameAsJson(expected: String) {

    if (this == null) {
        fail("The string is null, but expected to be: $expected")
    }

    // Validate expected JSON first - this is a programming error if invalid
    validateExpectedJson(expected)

    val prettified = prettifyJson(this)
    prettified sameAs expected

}

/**
 * JSON formatter instance configured for pretty printing with 2-space indentation.
 */
private val prettyJson = Json {
    prettyPrint = true
    @OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)
    prettyPrintIndent = "  "
}

/**
 * Validates that the expected JSON is well-formed.
 *
 * @throws IllegalArgumentException if the JSON is malformed
 */
private fun validateExpectedJson(json: String) {
    try {
        Json.parseToJsonElement(json)
    } catch (e: SerializationException) {
        throw IllegalArgumentException(
            formatJsonError("Invalid expected JSON", e.message)
        )
    }
}

/**
 * Prettifies a JSON string with 2-space indentation, preserving object key order.
 *
 * Uses kotlinx.serialization.json to parse and reformat JSON with:
 * - 2-space indentation
 * - Preserved object key order from the original JSON
 * - Proper formatting
 *
 * @throws AssertionError if the JSON is malformed
 */
private fun prettifyJson(json: String): String {
    try {
        val jsonElement = Json.parseToJsonElement(json)
        return prettyJson.encodeToString(
            JsonElement.serializer(), jsonElement
        )
    } catch (e: SerializationException) {
        fail(formatJsonError("Invalid JSON", e.message))
    }
}

/**
 * Formats a JSON error message with the appropriate prefix.
 * The errorMessage from kotlinx.serialization already contains the JSON input.
 */
private fun formatJsonError(
    prefix: String,
    errorMessage: String?
) = "$prefix: $errorMessage"
