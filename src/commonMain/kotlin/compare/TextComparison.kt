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
 * Extension function for string comparison with detailed difference reporting.
 */
public infix fun String.shouldEqual(expected: String) {
    if (this == expected) return

    val actualLines = this.lines()
    val expectedLines = expected.lines()

    val differences = mutableListOf<String>()
    val diffBuilder = StringBuilder()

    // Helper function to create the diff message in a unified format
    fun addDiff(line: Int, description: String, changes: String? = null) {
        diffBuilder.append("  • line $line: $description\n")
        if (changes != null) {
            diffBuilder.append("    changes: \"$changes\"\n")
        }
    }

    // Helper function to create a diff change notation
    fun createDiffNotation(original: String, modified: String): String {
        val result = StringBuilder()
        var i = 0
        var j = 0
        
        while (i < original.length || j < modified.length) {
            when {
                i >= original.length -> {
                    result.append("{+${modified.substring(j)}+}")
                    break
                }
                j >= modified.length -> {
                    result.append("[-${original.substring(i)}-]")
                    break
                }
                original[i] == modified[j] -> {
                    result.append(original[i])
                    i++
                    j++
                }
                else -> {
                    // Find the next matching position
                    var matchFound = false
                    var lookahead = 1
                    while (lookahead <= 3 && (i + lookahead < original.length || j + lookahead < modified.length)) {
                        if (i + lookahead < original.length && j < modified.length && 
                            original[i + lookahead] == modified[j]) {
                            result.append("[-${original.substring(i, i + lookahead)}-]")
                            i += lookahead
                            matchFound = true
                            break
                        }
                        if (i < original.length && j + lookahead < modified.length && 
                            original[i] == modified[j + lookahead]) {
                            result.append("{+${modified.substring(j, j + lookahead)}+}")
                            j += lookahead
                            matchFound = true
                            break
                        }
                        lookahead++
                    }
                    if (!matchFound) {
                        result.append("[-${original[i]}-]{+${modified[j]}+}")
                        i++
                        j++
                    }
                }
            }
        }
        return result.toString()
    }

    // Compare lines and collect differences
    var lineIndex = 1
    val minLines = minOf(actualLines.size, expectedLines.size)
    
    for (i in 0 until minLines) {
        val actualLine = actualLines[i]
        val expectedLine = expectedLines[i]
        
        if (actualLine != expectedLine) {
            // Handle whitespace differences
            if (actualLine.trim() == expectedLine.trim()) {
                if (actualLine.endsWith(" ")) {
                    differences.add("line $lineIndex: trailing whitespace difference")
                } else {
                    val actualIndent = actualLine.takeWhile { it.isWhitespace() }
                    val expectedIndent = expectedLine.takeWhile { it.isWhitespace() }
                    if (actualIndent != expectedIndent) {
                        addDiff(lineIndex, "indentation difference",
                            "${" ".repeat(actualIndent.length)}<${actualLine.trimStart()}>")
                    }
                }
            } else {
                // Handle regular text differences
                val diffNotation = createDiffNotation(actualLine, expectedLine)
                addDiff(lineIndex, "characters differ", diffNotation)
            }
        }
        lineIndex++
    }

    // Handle different line counts
    if (actualLines.size != expectedLines.size) {
        if (actualLines.size < expectedLines.size) {
            differences.add("structural: missing lines after line ${actualLines.size}")
            for (i in actualLines.size until expectedLines.size) {
                diffBuilder.append("    + ${expectedLines[i]}\n")
            }
        } else {
            differences.add("structural: extra lines after line ${expectedLines.size}")
            for (i in expectedLines.size until actualLines.size) {
                diffBuilder.append("    - ${actualLines[i]}\n")
            }
        }
    }

    // Handle newline at end of file
    if (this.endsWith("\n") != expected.endsWith("\n")) {
        differences.add("structural: " + if (this.endsWith("\n")) 
            "extra newline at end of file" else "missing newline at end of file")
    }

    // Create the final error message
    val message = buildString {
        appendLine("Text comparison failed:")
        appendLine("┌─ actual")
        actualLines.forEach { appendLine("│ $it") }
        appendLine("└─ differs from expected")
        expectedLines.forEach { appendLine("│ $it") }
        appendLine("└─ differences")
        append(diffBuilder)
        if (this@shouldEqual.contains(" \n") || expected.contains(" \n")) {
            appendLine("Note: ⠀ represents a space character")
        }
    }

    throw AssertionError(message)
}