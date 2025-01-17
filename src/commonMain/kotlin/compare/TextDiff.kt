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
public fun diff(original: String, revised: String): String {
    if (original == revised) return ""
    
    val originalLines = original.lines()
    val revisedLines = revised.lines()
    
    val builder = StringBuilder()
    // Start with format description
    builder.append("""
        Text comparison failed:
        Format description:
        • [-c-] indicates character 'c' present in original text but not in revised
        • {+c+} indicates character 'c' present in revised text but not in original
        • ⠀ (braille pattern blank, U+2800) represents a space character in differences to make them visible
        • Each character change is marked separately for precise difference tracking
        • Line numbers are 1-based
        • Structural changes show exact position of insertion or deletion

    """.trimIndent()).append("\n")
    
    // Output original text
    builder.append("┌─ original\n")
    originalLines.forEach { line ->
        builder.append("│ ${line.visualizeTrailingSpaces()}\n")
    }
    
    // Output revised text
    builder.append("└─ differs from revised\n")
    revisedLines.forEach { line ->
        builder.append("│ ${line.visualizeTrailingSpaces()}\n")
    }
    
    // Output differences
    builder.append("└─ differences\n")
    
    if (originalLines.size == 1 && revisedLines.size == 1) {
        // Single line comparison
        builder.append("  • line 1: strings differ\n")
        builder.append("    - original: \"${originalLines[0]}\"\n")
        builder.append("    - revised:  \"${revisedLines[0]}\"\n")
        builder.append("    - changes:  \"${diffLine(originalLines[0], revisedLines[0])}\"\n")
    } else {
        // Process line by line
        var currentLine = 1
        val maxLines = maxOf(originalLines.size, revisedLines.size)
        
        while (currentLine <= maxLines) {
            val originalLine = if (currentLine <= originalLines.size) originalLines[currentLine - 1] else null
            val revisedLine = if (currentLine <= revisedLines.size) revisedLines[currentLine - 1] else null
            
            when {
                originalLine == null && revisedLine != null -> {
                    // Line added
                    builder.append("  • structural: missing line after line ${currentLine - 1}\n")
                    builder.append("    + $revisedLine\n")
                }
                originalLine != null && revisedLine == null -> {
                    // Line removed
                    builder.append("  • line $currentLine: line removed\n")
                    builder.append("    - original: \"$originalLine\"\n")
                }
                originalLine != null && revisedLine != null && originalLine != revisedLine -> {
                    if (originalLine.trim() == revisedLine.trim()) {
                        if (originalLine.countTrailingSpaces() != revisedLine.countTrailingSpaces()) {
                            // Trailing whitespace difference
                            builder.append("  • line $currentLine: trailing whitespace difference\n")
                            builder.append("    - original: \"${originalLine.visualizeTrailingSpaces()}\"\n")
                            builder.append("    - revised:  \"${revisedLine.visualizeTrailingSpaces()}\"\n")
                            val trailingDiff = originalLine.countTrailingSpaces() - revisedLine.countTrailingSpaces()
                            builder.append("    - changes:  \"${originalLine.trimEnd()}${
                                "[-⠀-]".repeat(trailingDiff)}\"\n")
                        } else {
                            // Indentation difference
                            val originalSpaces = originalLine.countLeadingSpaces()
                            val revisedSpaces = revisedLine.countLeadingSpaces()
                            builder.append("  • line $currentLine: indentation difference\n")
                            builder.append("    - original: \"${originalLine}\"($originalSpaces spaces)\n")
                            builder.append("    - revised:  \"${revisedLine}\"($revisedSpaces spaces)\n")
                            builder.append("    - changes:  \"[-⠀-]${originalLine.trimStart()}\"\n")
                        }
                    } else {
                        // Content differs
                        builder.append("  • line $currentLine: strings differ\n")
                        builder.append("    - original: \"$originalLine\"\n")
                        builder.append("    - revised:  \"$revisedLine\"\n")
                        builder.append("    - changes:  \"${diffLine(originalLine, revisedLine)}\"\n")
                    }
                }
            }
            currentLine++
        }
    }
    
    return builder.toString()
}

private fun diffLine(str1: String, str2: String): String {
    val chars1 = str1.toList()
    val chars2 = str2.toList()
    
    val builder = StringBuilder()
    var i = 0
    var j = 0
    
    while (i < chars1.size || j < chars2.size) {
        if (i < chars1.size && j < chars2.size && chars1[i] == chars2[j]) {
            builder.append(chars1[i])
            i++
            j++
            continue
        }
        
        // Handle deletions
        if (i < chars1.size && (j >= chars2.size || !isPartOfMatch(chars1, chars2, i, j))) {
            val c = chars1[i]
            builder.append(if (c == ' ') "[-⠀-]" else "[-$c-]")
            i++
            continue
        }
        
        // Handle additions
        if (j < chars2.size) {
            val c = chars2[j]
            builder.append(if (c == ' ') "{+⠀+}" else "{+$c+}")
            j++
        }
    }
    
    return builder.toString()
}

private fun isPartOfMatch(chars1: List<Char>, chars2: List<Char>, pos1: Int, pos2: Int): Boolean {
    val lookAhead = 3
    for (k in 0 until lookAhead) {
        if (pos1 + k < chars1.size && pos2 + k < chars2.size) {
            if (chars1[pos1 + k] == chars2[pos2 + k]) {
                // Found a potential match point, check if it's worth using
                if (k == 0 || // immediate match
                    k > 1 || // multiple character lookahead
                    chars1[pos1] != ' ' && chars2[pos2] != ' ' // non-space characters
                ) {
                    return true
                }
            }
        }
    }
    return false
}

private fun String.countLeadingSpaces(): Int {
    var count = 0
    for (c in this) {
        if (c == ' ') count++
        else break
    }
    return count
}

private fun String.countTrailingSpaces(): Int {
    var count = 0
    for (i in length - 1 downTo 0) {
        if (this[i] == ' ') count++
        else break
    }
    return count
}

private fun String.visualizeTrailingSpaces(): String {
    if (isEmpty()) return this
    
    var lastNonSpace = length - 1
    while (lastNonSpace >= 0 && this[lastNonSpace] == ' ') {
        lastNonSpace--
    }
    
    if (lastNonSpace == length - 1) return this
    
    return substring(0, lastNonSpace + 1) + "⠀".repeat(length - lastNonSpace - 1)
}