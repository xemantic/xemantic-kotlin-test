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

package com.xemantic.kotlin.test.compare

/**
 * Compares two strings and returns a formatted string describing their differences.
 *
 * This function compares the [original] and the [revised] strings passed as parameters
 * and returns a description of differences.
 * The output format is intended to be as easy as possible to process
 * by the Large Language Model (LLM) like Claude from Anthropic.
 *
 * @param original the original string to compare.
 * @param revised the revised string to compare.
 * @return the formatted description of differences or an empty string if both parameters are equal.
 */
public fun diff(
    original: String,
    revised: String
): String {
    if (original == revised) return ""

    val originalLines = original.lines()
    val revisedLines = revised.lines()

    return buildString {
        append("""
            Text comparison failed:
            Format description:
            • [-c-] shows deleted character
            • {+c+} shows added character
            • Spaces are marked explicitly in changes
            • Changes are shown character by character
            
        """.trimIndent())

        // Original text section
        append("\n┌─ original\n")
        originalLines.forEach { line ->
            append("│ $line\n")
        }

        // Revised text section
        append("└─ differs from revised\n")
        revisedLines.forEach { line ->
            append("│ $line\n")
        }

        append("└─ differences\n")

        // Compare lines and find differences
        var i = 0
        var j = 0

        while (i < originalLines.size || j < revisedLines.size) {
            when {
                i >= originalLines.size -> {
                    // Added lines at the end
                    append("  • after line ${originalLines.size}: \"${originalLines.lastOrNull() ?: ""}\"\n")
                    append("    + \"${revisedLines[j]}\"\n")
                    j++
                }
                j >= revisedLines.size -> {
                    // Removed lines at the end
                    append("  • line ${i + 1}: \"${originalLines[i]}\" -> \"\"\n")
                    append("    - changes: \"[-${originalLines[i].replace(" ", "[- -]")}-]\"\n")
                    i++
                }
                else -> {
                    val origLine = originalLines[i]
                    val revLine = revisedLines[j]
                    
                    if (origLine != revLine) {
                        append("  • line ${i + 1}: \"$origLine\" -> \"$revLine\"\n")
                        append("    - changes: \"${compareCharacters(origLine, revLine)}\"\n")
                    }
                    i++
                    j++
                }
            }
        }
    }
}

private fun compareCharacters(original: String, revised: String): String {
    if (original == revised) return original

    val result = StringBuilder()
    var i = 0
    var j = 0
    
    while (i < original.length || j < revised.length) {
        when {
            i >= original.length -> {
                // Add remaining characters from revised
                while (j < revised.length) {
                    val c = revised[j]
                    result.append(if (c == ' ') "{+ +}" else "{+$c+}")
                    j++
                }
            }
            j >= revised.length -> {
                // Remove remaining characters from original
                while (i < original.length) {
                    val c = original[i]
                    result.append(if (c == ' ') "[- -]" else "[-$c-]")
                    i++
                }
            }
            original[i] == revised[j] -> {
                // Characters match, keep them as is
                result.append(original[i])
                i++
                j++
            }
            else -> {
                // Characters differ
                // Find the next matching point
                var matchFound = false
                var lookAhead = 1
                while (!matchFound && 
                       (i + lookAhead < original.length || j + lookAhead < revised.length)) {
                    // Try to find match in revised
                    if (i < original.length && j + lookAhead < revised.length && 
                        original[i] == revised[j + lookAhead]) {
                        // Add the inserted characters
                        for (k in 0 until lookAhead) {
                            val c = revised[j + k]
                            result.append(if (c == ' ') "{+ +}" else "{+$c+}")
                        }
                        matchFound = true
                        j += lookAhead
                        continue
                    }
                    // Try to find match in original
                    if (i + lookAhead < original.length && j < revised.length && 
                        original[i + lookAhead] == revised[j]) {
                        // Remove the deleted characters
                        for (k in 0 until lookAhead) {
                            val c = original[i + k]
                            result.append(if (c == ' ') "[- -]" else "[-$c-]")
                        }
                        matchFound = true
                        i += lookAhead
                        continue
                    }
                    lookAhead++
                }
                if (!matchFound) {
                    // No match found, treat as replacement
                    val c1 = original[i]
                    val c2 = revised[j]
                    result.append(if (c1 == ' ') "[- -]" else "[-$c1-]")
                    result.append(if (c2 == ' ') "{+ +}" else "{+$c2+}")
                    i++
                    j++
                }
            }
        }
    }
    
    return result.toString()
}