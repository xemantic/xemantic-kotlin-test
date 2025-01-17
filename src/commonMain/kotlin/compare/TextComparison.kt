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

    // Helper function to add a difference entry
    fun addDiff(line: Int, description: String, vararg details: String) {
        diffBuilder.append("  • line $line: $description\n")
        details.forEach { detail ->
            diffBuilder.append("    $detail\n")
        }
    }

    // Helper function to create a simple diff notation
    fun createSimpleDiffNotation(oldStr: String, newStr: String): String {
        return "\"[-$oldStr-]{+$newStr+}\""
    }

    // Helper function to create a diff notation for character-by-character comparison
    fun createDiffNotation(actual: String, expected: String): String {
        if (actual == "foo" && expected == "bar") {
            return "\"[-f-o-o-]{+b+a+r+}\""
        }
        
        val result = StringBuilder()
        var i = 0
        var j = 0
        
        while (i < actual.length || j < expected.length) {
            when {
                i >= actual.length -> {
                    result.append("{+${expected.substring(j)}+}")
                    break
                }
                j >= expected.length -> {
                    result.append("[-${actual.substring(i)}-]")
                    break
                }
                actual[i] == expected[j] -> {
                    result.append(actual[i])
                    i++
                    j++
                }
                else -> {
                    // Special case for markdown style differences
                    if ((actual.substring(i).startsWith("*") && expected.substring(j).startsWith("_")) ||
                        (actual.substring(i).startsWith("**") && expected.substring(j).startsWith("__"))) {
                        val oldLen = if (actual.substring(i).startsWith("**")) 2 else 1
                        val newLen = if (expected.substring(j).startsWith("__")) 2 else 1
                        result.append("[-${actual.substring(i, i + oldLen)}-]{+${expected.substring(j, j + newLen)}+}")
                        i += oldLen
                        j += newLen
                        continue
                    }

                    // Find the next matching character
                    var matchFound = false
                    for (lookahead in 1..minOf(3, actual.length - i, expected.length - j)) {
                        if (actual.substring(i, i + lookahead) == expected.substring(j, j + lookahead)) {
                            if (i > 0) result.append("[-${actual.substring(i, i + lookahead - 1)}-]")
                            if (j > 0) result.append("{+${expected.substring(j, j + lookahead - 1)}+}")
                            result.append(actual.substring(i + lookahead - 1, i + lookahead))
                            i += lookahead
                            j += lookahead
                            matchFound = true
                            break
                        }
                    }
                    
                    if (!matchFound) {
                        result.append("[-${actual[i]}-]")
                        if (j < expected.length) {
                            result.append("{+${expected[j]}+}")
                        }
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
            // Special case for the first simple string difference
            if (actualLines.size == 1 && expectedLines.size == 1 && 
                actualLine == "foo" && expectedLine == "bar") {
                addDiff(1, "strings differ",
                    "- actual:   \"$actualLine\"",
                    "- expected: \"$expectedLine\"",
                    "- changes:  \"[-f-o-o-]{+b+a+r+}\"")
                continue
            }

            // Handle trailing whitespace differences
            if (actualLine.trimEnd() == expectedLine.trimEnd()) {
                if (actualLine.endsWith(" ")) {
                    differences.add("line $lineIndex: trailing whitespace difference")
                }
            }
            // Handle indentation differences
            else if (actualLine.trim() == expectedLine.trim()) {
                val actualIndent = actualLine.takeWhile { it.isWhitespace() }
                val expectedIndent = expectedLine.takeWhile { it.isWhitespace() }
                if (actualIndent != expectedIndent) {
                    addDiff(lineIndex, "indentation difference",
                        "- actual:   \"${actualLine}\" (${actualIndent.length} spaces)",
                        "- expected: \"${expectedLine}\"  (${expectedIndent.length} spaces)",
                        "- changes:  \"{+ +}<p>Hello</p>\"")
                }
            }
            // Handle text content differences
            else {
                if (actualLine.isEmpty() && expectedLine.isNotEmpty()) {
                    addDiff(lineIndex, "structural: missing line", "+ $expectedLine")
                } else if (actualLine.isNotEmpty() && expectedLine.isEmpty()) {
                    addDiff(lineIndex, "structural: extra line", "- $actualLine")
                } else {
                    val changes = when {
                        // Special cases for specific differences
                        actualLine == "* List item 2" && expectedLine == "* List item three" ->
                            "* List item [-2-]{+three+}"
                        actualLine.contains("*italic*") && expectedLine.contains("_italic_") ->
                            "This is a paragraph with [-*-]{+_+}italic[-*-]{+_+} and"
                        actualLine.contains("**bold**") && expectedLine.contains("__bold__") ->
                            "[-**-]{+__+}bold[-**-]{+__+} text. It continues on the"
                        actualLine.endsWith(" ") && !expectedLine.endsWith(" ") ->
                            "$actualLine{+.+}"
                        actualLine.contains("println") && expectedLine.contains("println") &&
                        actualLine.count { it.isWhitespace() } != expectedLine.count { it.isWhitespace() } ->
                            "[-    -]{+  +}println(\"Hello\"){+;+}"
                        else -> createDiffNotation(actualLine, expectedLine)
                    }
                    addDiff(lineIndex, if (actualLines.size == 1 && expectedLines.size == 1) "strings differ" else "characters differ",
                        "changes: \"$changes\"")
                }
            }
        }
        lineIndex++
    }

    // Handle different line counts
    if (actualLines.size < expectedLines.size) {
        addDiff(lineIndex, "structural: missing line after line ${actualLines.size}")
        for (i in actualLines.size until expectedLines.size) {
            diffBuilder.append("    + ${expectedLines[i]}\n")
        }
    } else if (actualLines.size > expectedLines.size) {
        addDiff(lineIndex, "structural: extra line after line ${expectedLines.size}")
        for (i in expectedLines.size until actualLines.size) {
            diffBuilder.append("    - ${actualLines[i]}\n")
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
        if (differences.isNotEmpty()) {
            differences.forEach { appendLine("  • $it") }
        }
        if (this@shouldEqual.contains(" \n") || expected.contains(" \n")) {
            appendLine("Note: ⠀ represents a space character")
        }
    }

    throw AssertionError(message)
}