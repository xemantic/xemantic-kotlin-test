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

import kotlin.test.fail

/**
 * Asserts that this string is the same as the [expected] string.
 * If they differ, throws an [AssertionError] with a unified diff format
 * showing the differences, making it easy for LLMs to understand the changes.
 *
 * @param expected the expected string.
 * @throws AssertionError if the strings are not equal, with unified diff output.
 */
public infix fun String?.sameAs(expected: String) {

    if (this == expected) return
    if (this == null) {
        fail("The string is null, but expected to be: $expected")
    }

    val diff = generateUnifiedDiff(expected, this)
    fail(diff)
}

/**
 * Splits a string into lines for diff processing.
 *
 * The standard [String.lines] function adds a trailing empty string when the string ends with \n.
 * This helper removes that trailing empty string to get the actual line content.
 */
private fun String.splitLinesForDiff(): List<String> = when {
    isEmpty() -> emptyList()
    else -> {
        val lines = lines()
        // If string ends with newline, lines() adds ONE trailing empty string - remove only that one
        if (endsWith('\n') && lines.isNotEmpty() && lines.last() == "") {
            lines.dropLast(1)
        } else {
            lines
        }
    }
}

/**
 * Generates a unified diff between expected and actual strings.
 *
 * Uses Myers' diff algorithm to compute the minimal set of changes between the two strings,
 * then formats the output in unified diff format (similar to GNU diff -u). This format is
 * particularly well-suited for LLMs to understand and process changes.
 *
 * The unified diff format shows:
 * - Lines prefixed with `-` are from the expected string (deleted/changed)
 * - Lines prefixed with `+` are from the actual string (inserted/changed)
 * - Lines prefixed with ` ` (space) are context lines (unchanged)
 * - `\ No newline at end of file` markers when lines lack trailing newlines
 */
private fun generateUnifiedDiff(expected: String, actual: String): String {
    val expectedLines = expected.splitLinesForDiff()
    val actualLines = actual.splitLinesForDiff()

    val expectedEndsWithNewline = expected.isEmpty() || expected.endsWith('\n')
    val actualEndsWithNewline = actual.isEmpty() || actual.endsWith('\n')

    val diff = computeDiff(expectedLines, actualLines, expectedEndsWithNewline, actualEndsWithNewline)
    val hunks = generateHunks(diff, expectedLines, actualLines, expectedEndsWithNewline, actualEndsWithNewline)

    return buildString {
        append("--- expected\n")
        append("+++ actual\n")
        for (hunk in hunks) {
            append(hunk)
        }
    }
}

/**
 * Represents a change operation in the diff.
 */
private sealed class DiffOperation {
    data class Delete(val oldIndex: Int) : DiffOperation()
    data class Insert(val newIndex: Int) : DiffOperation()
    data class Equal(val oldIndex: Int, val newIndex: Int) : DiffOperation()
}

/**
 * Computes the diff between two lists of lines using Myers' diff algorithm.
 */
private fun computeDiff(
    expected: List<String>,
    actual: List<String>,
    expectedEndsWithNewline: Boolean,
    actualEndsWithNewline: Boolean
): List<DiffOperation> {
    val n = expected.size
    val m = actual.size
    val max = n + m

    val v = IntArray(2 * max + 1)
    val trace = mutableListOf<IntArray>()

    for (d in 0..max) {
        for (k in -d..d step 2) {
            var x = if (k == -d || (k != d && v[max + k - 1] < v[max + k + 1])) {
                v[max + k + 1]
            } else {
                v[max + k - 1] + 1
            }
            var y = x - k

            while (x < n && y < m && expected[x] == actual[y]) {
                // Check if this is the last line and newline status differs
                val isLastExpectedLine = (x == n - 1)
                val isLastActualLine = (y == m - 1)

                // If both are last lines, they can only be equal if newline status matches
                if (isLastExpectedLine && isLastActualLine) {
                    if (expectedEndsWithNewline != actualEndsWithNewline) {
                        break // Don't treat as equal if newline status differs
                    }
                }
                // If expected is last but actual is not, and expected has no newline,
                // they're not truly equal (expected line lacks newline, actual has it)
                else if (isLastExpectedLine && !isLastActualLine && !expectedEndsWithNewline) {
                    break
                }
                // If actual is last but expected is not, and actual has no newline,
                // they're not truly equal
                else if (!isLastExpectedLine && isLastActualLine && !actualEndsWithNewline) {
                    break
                }

                x++
                y++
            }

            v[max + k] = x

            if (x >= n && y >= m) {
                trace.add(v.copyOf())
                return backtrack(trace, expected, actual, max)
            }
        }
        trace.add(v.copyOf())
    }

    return emptyList()
}

/**
 * Backtrack through the diff trace to construct the edit script.
 */
private fun backtrack(trace: List<IntArray>, expected: List<String>, actual: List<String>, max: Int): List<DiffOperation> {
    var x = expected.size
    var y = actual.size
    val ops = mutableListOf<DiffOperation>()

    for (d in trace.size - 1 downTo 0) {
        val v = trace[d]
        val k = x - y

        val prevK = if (k == -d || (k != d && v[max + k - 1] < v[max + k + 1])) {
            k + 1
        } else {
            k - 1
        }

        val prevX = v[max + prevK]
        val prevY = prevX - prevK

        while (x > prevX && y > prevY) {
            ops.add(0, DiffOperation.Equal(x - 1, y - 1))
            x--
            y--
        }

        if (d > 0) {
            if (x > prevX) {
                ops.add(0, DiffOperation.Delete(x - 1))
                x--
            } else {
                ops.add(0, DiffOperation.Insert(y - 1))
                y--
            }
        }
    }

    return ops
}

/**
 * Groups diff operations into hunks with context lines.
 */
private fun generateHunks(
    ops: List<DiffOperation>,
    expected: List<String>,
    actual: List<String>,
    expectedEndsWithNewline: Boolean,
    actualEndsWithNewline: Boolean,
    context: Int = 3
): List<String> {
    if (ops.isEmpty()) return emptyList()

    val hunks = mutableListOf<String>()
    var i = 0

    while (i < ops.size) {
        // Find the next change
        while (i < ops.size && ops[i] is DiffOperation.Equal) {
            i++
        }

        if (i >= ops.size) break

        // Start of a hunk - go back 'context' lines
        val hunkStart = maxOf(0, i - context)

        // Find the end of this hunk by looking for the last change within range
        var j = i
        var lastChangeIndex = i

        while (j < ops.size) {
            if (ops[j] !is DiffOperation.Equal) {
                lastChangeIndex = j
                j++
            } else {
                // Count consecutive equal lines
                val equalStart = j
                while (j < ops.size && ops[j] is DiffOperation.Equal) {
                    j++
                }
                val equalCount = j - equalStart

                // If we have more than 'context * 2' equal lines, split the hunk
                if (equalCount > context * 2) {
                    break
                }
                // Note: we don't update lastChangeIndex for equal lines
            }
        }

        // End of hunk - include up to 'context' lines after the last change
        val hunkEnd = minOf(ops.size, lastChangeIndex + context + 1)

        // Generate hunk
        val hunk = generateHunk(
            ops.subList(hunkStart, hunkEnd),
            expected,
            actual,
            expectedEndsWithNewline,
            actualEndsWithNewline
        )
        hunks.add(hunk)

        i = hunkEnd
    }

    return hunks
}

/**
 * Generates a single hunk string from a list of operations.
 */
private fun generateHunk(
    ops: List<DiffOperation>,
    expected: List<String>,
    actual: List<String>,
    expectedEndsWithNewline: Boolean,
    actualEndsWithNewline: Boolean
): String {
    if (ops.isEmpty()) return ""

    // Find the first line numbers in this hunk
    var oldStart = Int.MAX_VALUE
    var newStart = Int.MAX_VALUE

    for (op in ops) {
        when (op) {
            is DiffOperation.Equal -> {
                oldStart = minOf(oldStart, op.oldIndex)
                newStart = minOf(newStart, op.newIndex)
            }
            is DiffOperation.Delete -> {
                oldStart = minOf(oldStart, op.oldIndex)
            }
            is DiffOperation.Insert -> {
                newStart = minOf(newStart, op.newIndex)
            }
        }
    }

    // Count lines in each side
    val oldCount = ops.count { it is DiffOperation.Delete || it is DiffOperation.Equal }
    val newCount = ops.count { it is DiffOperation.Insert || it is DiffOperation.Equal }

    // Handle empty files - if no lines found, use 0 as start
    val oldStartLine = if (oldStart == Int.MAX_VALUE) 0 else oldStart + 1
    val newStartLine = if (newStart == Int.MAX_VALUE) 0 else newStart + 1

    // Format the hunk header according to unified diff convention
    val oldRange = when {
        oldCount == 0 -> "0,0"
        oldCount == 1 && oldStartLine > 0 -> "$oldStartLine"
        else -> "$oldStartLine,$oldCount"
    }
    val newRange = when {
        newCount == 0 -> "0,0"
        newCount == 1 && newStartLine > 0 -> "$newStartLine"
        else -> "$newStartLine,$newCount"
    }

    return buildString {
        append("@@ -$oldRange +$newRange @@\n")

        // Track state for "No newline at end of file" markers
        var lastOpWasDelete = false
        var lastDeletedLineIsFileEnd = false
        var pendingInsertMarker = false

        for (op in ops) {
            when (op) {
                is DiffOperation.Equal -> {
                    // Output any pending insert marker before the equal line
                    if (pendingInsertMarker) {
                        append("\\ No newline at end of file\n")
                        pendingInsertMarker = false
                    }
                    lastOpWasDelete = false
                    append(" ${expected[op.oldIndex]}\n")
                }
                is DiffOperation.Delete -> {
                    // Output any pending insert marker before transitioning to delete
                    if (pendingInsertMarker) {
                        append("\\ No newline at end of file\n")
                        pendingInsertMarker = false
                    }
                    lastOpWasDelete = true
                    lastDeletedLineIsFileEnd = (op.oldIndex == expected.size - 1)
                    append("-${expected[op.oldIndex]}\n")
                }
                is DiffOperation.Insert -> {
                    // Output delete marker if transitioning from delete to insert
                    if (lastOpWasDelete && lastDeletedLineIsFileEnd && !expectedEndsWithNewline) {
                        append("\\ No newline at end of file\n")
                    }
                    lastOpWasDelete = false
                    append("+${actual[op.newIndex]}\n")
                    // Track if this insert needs a marker (will output later if needed)
                    if (op.newIndex == actual.size - 1 && !actualEndsWithNewline) {
                        pendingInsertMarker = true
                    }
                }
            }
        }

        // Output any remaining markers at the end of the hunk
        if (ops.isNotEmpty()) {
            when (val lastOp = ops.last()) {
                is DiffOperation.Delete -> {
                    if (lastOp.oldIndex == expected.size - 1 && !expectedEndsWithNewline) {
                        append("\\ No newline at end of file\n")
                    }
                }
                is DiffOperation.Insert -> {
                    if (lastOp.newIndex == actual.size - 1 && !actualEndsWithNewline) {
                        append("\\ No newline at end of file\n")
                    }
                }
                is DiffOperation.Equal -> {
                    // No marker needed if hunk ends with equal
                }
            }
        }
    }
}