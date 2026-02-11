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

import org.intellij.lang.annotations.Language
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
 * Asserts that this string is the same as the [expected] HTML string.
 * Delegates to [sameAs] while providing IDE language injection for HTML,
 * enabling syntax highlighting and completion in the [expected] parameter.
 *
 * @param expected the expected HTML string.
 * @throws AssertionError if the strings are not equal, with unified diff output.
 */
public infix fun String?.sameAsHtml(
    @Language("html") expected: String
) {
    sameAs(expected)
}

/**
 * Asserts that this string is the same as the [expected] XML string.
 * Delegates to [sameAs] while providing IDE language injection for XML,
 * enabling syntax highlighting and completion in the [expected] parameter.
 *
 * @param expected the expected XML string.
 * @throws AssertionError if the strings are not equal, with unified diff output.
 */
public infix fun String?.sameAsXml(
    @Language("xml") expected: String
) {
    sameAs(expected)
}

/**
 * Splits a string into lines for diff processing.
 *
 * The standard [String.lines] function adds a trailing empty string when the string ends with \n.
 * This helper removes that trailing empty string to get the actual line content.
 */
private fun String.splitLinesForDiff(): List<String> {
    if (isEmpty()) return emptyList()
    val lines = lines()
    // If string ends with newline, lines() adds ONE trailing empty string - remove only that one
    return if (lines.size > 1 && endsWith('\n') && lines.last().isEmpty()) {
        lines.dropLast(1)
    } else {
        lines
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

        // Truncate if there are too many changes for LLM consumption
        val truncated = truncateIfNeeded(hunks, expectedLines.size, actualLines.size)
        for (hunk in truncated) {
            append(hunk)
        }
    }
}

/**
 * Truncates hunks if the total number of changed lines exceeds a threshold.
 *
 * This prevents overwhelming LLMs with diffs containing hundreds or thousands of changes.
 * When truncated, returns only the first hunks containing up to maxChangedLines changed lines,
 * followed by a summary message.
 *
 * Uses a single-pass algorithm: processes hunks until hitting the limit, tracking whether
 * truncation occurred. This avoids the overhead of counting all changes upfront.
 *
 * @param hunks the list of hunk strings to potentially truncate
 * @param expectedLineCount the total number of lines in the expected string
 * @param actualLineCount the total number of lines in the actual string
 * @param maxChangedLines the maximum number of changed lines (+ and - prefixed) to show before truncating
 * @return the original hunks if under threshold, or truncated hunks with summary message
 */
private fun truncateIfNeeded(
    hunks: List<String>,
    expectedLineCount: Int,
    actualLineCount: Int,
    maxChangedLines: Int = MAX_CHANGED_LINES_DISPLAY
): List<String> {
    val result = mutableListOf<String>()
    var changedLinesSoFar = 0
    var truncated = false

    for (hunk in hunks) {
        if (truncated) break

        val lines = hunk.lines()
        val truncatedLines = mutableListOf<String>()
        var oldLineCount = 0
        var newLineCount = 0

        for (line in lines) {
            if (line.isEmpty()) {
                truncatedLines.add(line)
                continue
            }

            val firstChar = line[0]
            // Check if this is a changed line (starts with + or - but not \)
            val isChangedLine = (firstChar == '+' || firstChar == '-') && !line.startsWith("\\ ")

            // Stop if we've already collected enough changed lines
            if (isChangedLine && changedLinesSoFar >= maxChangedLines) {
                truncated = true
                break
            }

            truncatedLines.add(line)

            // Count lines for hunk header recalculation
            when {
                firstChar == '-' && !line.startsWith("\\ ") -> oldLineCount++
                firstChar == '+' && !line.startsWith("\\ ") -> newLineCount++
                firstChar == ' ' -> {
                    oldLineCount++
                    newLineCount++
                }
            }

            if (isChangedLine) {
                changedLinesSoFar++
            }
        }

        // Add hunk to result if we collected any lines
        if (truncatedLines.isNotEmpty()) {
            val adjustedHunk = recalculateHunkHeader(truncatedLines, oldLineCount, newLineCount)
            result.add(adjustedHunk)
        }
    }

    // Add truncation message if we stopped early
    if (truncated) {
        val truncationMessage = """


        Diff truncated: more than $maxChangedLines lines changed

        Expected: $expectedLineCount lines
        Actual: $actualLineCount lines

        The differences are too extensive to show in unified diff format.
        Consider comparing smaller sections or reviewing the strings directly.

    """.trimIndent()
        result.add(truncationMessage)
    }

    return result
}

/**
 * Recalculates the hunk header to reflect the actual line counts after truncation.
 *
 * @param lines the hunk lines including the header
 * @param oldLineCount the number of lines from the old file in this hunk
 * @param newLineCount the number of lines from the new file in this hunk
 * @return the hunk with updated header
 */
private fun recalculateHunkHeader(lines: List<String>, oldLineCount: Int, newLineCount: Int): String {
    if (lines.isEmpty()) return ""

    val firstLine = lines[0]
    if (!firstLine.startsWith("@@")) {
        return lines.joinToString("\n")
    }

    // Extract the starting line numbers from the original header
    val match = HUNK_HEADER_REGEX.find(firstLine) ?: return lines.joinToString("\n")

    val oldStart = match.groupValues[1].toInt()
    val newStart = match.groupValues[2].toInt()

    // Format the new header
    val oldRange = formatHunkRange(oldStart, oldLineCount)
    val newRange = formatHunkRange(newStart, newLineCount)

    return buildString {
        append("@@ -")
        append(oldRange)
        append(" +")
        append(newRange)
        append(" @@")
        for (i in 1 until lines.size) {
            append('\n')
            append(lines[i])
        }
    }
}

/**
 * Maximum number of changed lines (+ and - prefixed) to show in diff output before truncating.
 * This prevents overwhelming LLMs with diffs containing hundreds or thousands of changes.
 */
private const val MAX_CHANGED_LINES_DISPLAY = 100

/**
 * Maximum edit distance (insertions + deletions) to process in Myers' algorithm before stopping.
 * This is set higher than display limit to allow normal truncation to work, while preventing
 * memory exhaustion with extremely large diffs.
 */
private const val MAX_DIFF_CHANGES = 500

/**
 * Number of unchanged lines to show before and after each changed section in diff hunks.
 * Standard unified diff convention uses 3 lines of context.
 */
private const val DIFF_CONTEXT_LINES = 3

/**
 * Regex for parsing hunk headers in unified diff format.
 * Cached at top level to avoid recompilation on every call.
 */
private val HUNK_HEADER_REGEX = """@@ -(\d+)(?:,\d+)? \+(\d+)(?:,\d+)? @@""".toRegex()

/**
 * Represents a change operation in the diff.
 */
private sealed class DiffOperation {
    data class Delete(val oldIndex: Int) : DiffOperation()
    data class Insert(val newIndex: Int) : DiffOperation()
    data class Equal(val oldIndex: Int, val newIndex: Int) : DiffOperation()
}

/**
 * Checks if matching lines at the given positions would create a newline mismatch.
 *
 * Lines are considered to have different newline status if one is the last line without
 * a trailing newline, while the other is not the last line (implicitly has a newline after).
 *
 * @param x position in expected list
 * @param y position in actual list
 * @param expectedSize size of expected list
 * @param actualSize size of actual list
 * @param expectedEndsWithNewline whether expected string ends with newline
 * @param actualEndsWithNewline whether actual string ends with newline
 * @return true if these positions would create a newline mismatch
 */
private fun hasNewlineMismatch(
    x: Int,
    y: Int,
    expectedSize: Int,
    actualSize: Int,
    expectedEndsWithNewline: Boolean,
    actualEndsWithNewline: Boolean
): Boolean {
    val isExpectedLastLine = x == expectedSize - 1
    val isActualLastLine = y == actualSize - 1

    return (isExpectedLastLine && !expectedEndsWithNewline && !isActualLastLine) ||
           (isActualLastLine && !actualEndsWithNewline && !isExpectedLastLine)
}

/**
 * Computes the diff between two lists of lines using Myers' diff algorithm.
 *
 * This implementation follows the standard Myers' algorithm, comparing lines by content only.
 * Newline differences at end-of-file are handled separately during hunk generation, not here.
 * However, when both sequences end at the same line position but have different newline status,
 * we need to ensure that difference is captured in the edit script.
 *
 * To prevent memory exhaustion with very large diffs, this function will stop early if the
 * edit distance (d) exceeds maxChanges. When truncated, it returns a partial diff representing
 * the changes found up to that point.
 *
 * @param maxChanges maximum edit distance (number of insertions + deletions) to process before stopping.
 *                   This is specifically to prevent OOM with extremely large diffs. Set to a higher value
 *                   than the display truncation limit (100 changed lines) to allow normal truncation to work.
 * @return list of diff operations, which may be a partial diff if truncated
 */
private fun computeDiff(
    expected: List<String>,
    actual: List<String>,
    expectedEndsWithNewline: Boolean,
    actualEndsWithNewline: Boolean,
    maxChanges: Int = MAX_DIFF_CHANGES
): List<DiffOperation> {
    val n = expected.size
    val m = actual.size
    val max = n + m

    val v = IntArray(2 * max + 1)
    val trace = mutableListOf<IntArray>()

    for (d in 0..max) {
        // Early termination: stop if edit distance exceeds threshold
        // This prevents memory exhaustion when comparing very large, different strings
        if (d > maxChanges) {
            // When we hit the limit, we can't properly backtrack because the trace is incomplete.
            // Instead, generate a simple diff showing first maxChanges/2 deletions and first maxChanges/2 insertions
            val ops = mutableListOf<DiffOperation>()
            val maxDels = minOf(expected.size, maxChanges / 2)
            val maxIns = minOf(actual.size, maxChanges - maxDels)

            for (i in 0 until maxDels) {
                ops.add(DiffOperation.Delete(i))
            }
            for (i in 0 until maxIns) {
                ops.add(DiffOperation.Insert(i))
            }
            return ops
        }

        for (k in -d..d step 2) {
            // Determine whether to move down (insert) or right (delete)
            var x = if (k == -d || (k != d && v[max + k - 1] < v[max + k + 1])) {
                v[max + k + 1]
            } else {
                v[max + k - 1] + 1
            }
            var y = x - k

            // Extend diagonally as far as possible while lines match
            // However, don't treat lines as equal if they have different newline status
            while (x < n && y < m && expected[x] == actual[y]) {
                if (hasNewlineMismatch(x, y, n, m, expectedEndsWithNewline, actualEndsWithNewline)) {
                    break
                }
                x++
                y++
            }

            v[max + k] = x

            // Check if we've reached the end of both sequences
            if (x >= n && y >= m) {
                trace.add(v.copyOf())
                return backtrack(trace, expected, actual, max, expectedEndsWithNewline, actualEndsWithNewline)
            }
        }
        trace.add(v.copyOf())
    }

    return emptyList()
}

/**
 * Backtrack through the diff trace to construct the edit script.
 *
 * Walks backwards through the trace to reconstruct the sequence of operations
 * (equal, delete, insert) that transforms expected into actual.
 *
 * @param maxOps maximum number of non-Equal operations to include before truncating
 */
private fun backtrack(
    trace: List<IntArray>,
    expected: List<String>,
    actual: List<String>,
    max: Int,
    expectedEndsWithNewline: Boolean,
    actualEndsWithNewline: Boolean,
    maxOps: Int = Int.MAX_VALUE
): List<DiffOperation> {
    // Early return if trace is empty (shouldn't happen normally)
    if (trace.isEmpty()) {
        return emptyList()
    }

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

        // Add equal operations for diagonal moves
        while (x > prevX && y > prevY) {
            ops.add(0, DiffOperation.Equal(x - 1, y - 1))
            x--
            y--
        }

        // Add delete or insert operation for non-diagonal moves
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

    // If we've processed all lines but newline status differs, ensure the diff captures this.
    // This is represented by having the final line marked as different in the hunk output.
    if (expected.size == actual.size &&
        expected.isNotEmpty() &&
        expectedEndsWithNewline != actualEndsWithNewline) {
        // The last line exists in both but with different newline status.
        // If we have an Equal operation for the last line, we need to replace it with Delete+Insert
        // to show the newline difference in the output.
        val lastIndex = ops.lastIndex
        if (lastIndex >= 0 && ops[lastIndex] is DiffOperation.Equal) {
            val lastEqual = ops[lastIndex] as DiffOperation.Equal
            if (lastEqual.oldIndex == expected.size - 1 && lastEqual.newIndex == actual.size - 1) {
                ops[lastIndex] = DiffOperation.Delete(lastEqual.oldIndex)
                ops.add(DiffOperation.Insert(lastEqual.newIndex))
            }
        }
    }

    // Truncate to maxOps if needed - keep only first maxOps non-Equal operations
    if (maxOps < Int.MAX_VALUE) {
        var changeCount = 0
        val truncatedOps = mutableListOf<DiffOperation>()

        for (op in ops) {
            if (op !is DiffOperation.Equal) {
                if (changeCount >= maxOps) {
                    break
                }
                changeCount++
            }
            truncatedOps.add(op)
        }

        return truncatedOps
    }

    return ops
}

/**
 * Groups diff operations into hunks with context lines.
 *
 * Splits operations into separate hunks when there are more than context*2 consecutive
 * equal lines, following standard unified diff behavior.
 */
private fun generateHunks(
    ops: List<DiffOperation>,
    expected: List<String>,
    actual: List<String>,
    expectedEndsWithNewline: Boolean,
    actualEndsWithNewline: Boolean,
    context: Int = DIFF_CONTEXT_LINES
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

        // Generate hunk using index range to avoid creating sublists
        val hunk = generateHunk(
            ops,
            hunkStart,
            hunkEnd,
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
 * Formats a hunk range according to unified diff convention.
 * Returns "0,0" for empty, just the line number for single lines, or "start,count" for multiple lines.
 */
private fun formatHunkRange(startLine: Int, count: Int): String =
    when (count) {
        0 -> "0,0"
        1 -> if (startLine > 0) "$startLine" else "$startLine,$count"
        else -> "$startLine,$count"
    }

/**
 * Generates a single hunk string from a range of operations.
 *
 * Outputs the hunk header followed by the diff lines with appropriate prefixes:
 * - ' ' (space) for equal lines
 * - '-' for deleted lines
 * - '+' for inserted lines
 * - '\ No newline at end of file' markers when appropriate
 *
 * @param ops the complete list of diff operations
 * @param startIndex inclusive start index in the ops list
 * @param endIndex exclusive end index in the ops list
 */
private fun generateHunk(
    ops: List<DiffOperation>,
    startIndex: Int,
    endIndex: Int,
    expected: List<String>,
    actual: List<String>,
    expectedEndsWithNewline: Boolean,
    actualEndsWithNewline: Boolean
): String {
    if (startIndex >= endIndex) return ""

    // Find the first line numbers in this hunk
    var oldStart = Int.MAX_VALUE
    var newStart = Int.MAX_VALUE

    for (i in startIndex until endIndex) {
        when (val op = ops[i]) {
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

    // Count lines in each side within the range
    var oldCount = 0
    var newCount = 0
    for (i in startIndex until endIndex) {
        val op = ops[i]
        if (op is DiffOperation.Delete || op is DiffOperation.Equal) oldCount++
        if (op is DiffOperation.Insert || op is DiffOperation.Equal) newCount++
    }

    // Handle empty files - if no lines found, use 0 as start
    val oldStartLine = if (oldStart == Int.MAX_VALUE) 0 else oldStart + 1
    val newStartLine = if (newStart == Int.MAX_VALUE) 0 else newStart + 1

    val oldRange = formatHunkRange(oldStartLine, oldCount)
    val newRange = formatHunkRange(newStartLine, newCount)

    return buildString {
        append("@@ -$oldRange +$newRange @@\n")

        // Output all diff lines in the range
        var previousDeleteEndsFile = false

        for (i in startIndex until endIndex) {
            val isLastInRange = i == endIndex - 1

            when (val op = ops[i]) {
                is DiffOperation.Equal -> {
                    append(" ${expected[op.oldIndex]}\n")
                    previousDeleteEndsFile = false
                }
                is DiffOperation.Delete -> {
                    append("-${expected[op.oldIndex]}\n")
                    // Check if this delete is at end of file and needs a marker
                    val isFileEnd = op.oldIndex == expected.size - 1
                    val needsMarker = isFileEnd && !expectedEndsWithNewline
                    // Output marker immediately if next operation is not Insert
                    val nextOp = if (i + 1 < endIndex) ops[i + 1] else null
                    if (needsMarker && nextOp !is DiffOperation.Insert) {
                        append("\\ No newline at end of file\n")
                        previousDeleteEndsFile = false
                    } else if (needsMarker) {
                        previousDeleteEndsFile = true
                    } else {
                        previousDeleteEndsFile = false
                    }
                }
                is DiffOperation.Insert -> {
                    // If previous delete ended file without newline, output its marker now
                    if (previousDeleteEndsFile) {
                        append("\\ No newline at end of file\n")
                        previousDeleteEndsFile = false
                    }
                    append("+${actual[op.newIndex]}\n")
                    // If this insert is at end of file without newline, output marker if last in range
                    val isFileEnd = op.newIndex == actual.size - 1
                    val needsMarker = isFileEnd && !actualEndsWithNewline
                    if (needsMarker && isLastInRange) {
                        append("\\ No newline at end of file\n")
                    }
                }
            }
        }
    }
}