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
        • ⠀ represents a space character in differences to make them visible
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
        // Find matching blocks
        val matches = findMatchingBlocks(originalLines, revisedLines)
        var originalIndex = 0
        var revisedIndex = 0
        var currentLine = 1
        
        matches.forEach { match ->
            // Handle differences before the match
            while (originalIndex < match.originalStart || revisedIndex < match.revisedStart) {
                when {
                    originalIndex >= match.originalStart && revisedIndex < match.revisedStart -> {
                        // Addition in revised
                        builder.append("  • structural: missing line after line ${currentLine - 1}\n")
                        builder.append("    + ${revisedLines[revisedIndex]}\n")
                        revisedIndex++
                    }
                    originalIndex < match.originalStart && revisedIndex >= match.revisedStart -> {
                        // Deletion in original - skip for now as we focus on additions
                        originalIndex++
                        currentLine++
                    }
                    originalIndex < match.originalStart && revisedIndex < match.revisedStart -> {
                        // Lines differ
                        val originalLine = originalLines[originalIndex]
                        val revisedLine = revisedLines[revisedIndex]
                        
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
                                builder.append("    - original: \"${originalLine}\" ($originalSpaces spaces)\n")
                                builder.append("    - revised:  \"${revisedLine}\" ($revisedSpaces spaces)\n")
                                builder.append("    - changes:  \"[-⠀-]${originalLine.trimStart()}\"\n")
                            }
                        } else {
                            builder.append("  • line $currentLine: strings differ\n")
                            builder.append("    - original:   \"${originalLine}\"\n")
                            builder.append("    - revised: \"${revisedLine}\"\n")
                            builder.append("    - changes:  \"${diffLine(originalLine, revisedLine)}\"\n")
                        }
                        originalIndex++
                        revisedIndex++
                        currentLine++
                    }
                }
            }
            
            // Skip matching block
            repeat(match.length) {
                if (originalLines[originalIndex] != revisedLines[revisedIndex]) {
                    val originalLine = originalLines[originalIndex]
                    val revisedLine = revisedLines[revisedIndex]
                    builder.append("  • line $currentLine: strings differ\n")
                    builder.append("    - original: \"${originalLine}\"\n")
                    builder.append("    - revised:  \"${revisedLine}\"\n")
                    builder.append("    - changes:  \"${diffLine(originalLine, revisedLine)}\"\n")
                }
                originalIndex++
                revisedIndex++
                currentLine++
            }
        }
        
        // Handle remaining lines
        while (revisedIndex < revisedLines.size) {
            builder.append("  • structural: missing line after line ${currentLine - 1}\n")
            builder.append("    + ${revisedLines[revisedIndex]}\n")
            revisedIndex++
        }
    }
    
    return builder.toString()
}

private data class Match(
    val originalStart: Int,
    val revisedStart: Int,
    val length: Int
)

private fun findMatchingBlocks(
    originalLines: List<String>,
    revisedLines: List<String>
): List<Match> {

    val matches = mutableListOf<Match>()
    var originalIndex = 0
    var revisedIndex = 0
    
    while (originalIndex < originalLines.size && revisedIndex < revisedLines.size) {
        if (originalLines[originalIndex] == revisedLines[revisedIndex]) {
            // Found a match, look for more matching lines
            val startOriginal = originalIndex
            val startRevised = revisedIndex
            var length = 0
            
            while (originalIndex < originalLines.size &&
                   revisedIndex < revisedLines.size &&
                   originalLines[originalIndex] == revisedLines[revisedIndex]) {
                originalIndex++
                revisedIndex++
                length++
            }
            
            if (length > 0) {
                matches.add(Match(startOriginal, startRevised, length))
            }
        } else {
            // Try to find next match
            var found = false
            for (lookAhead in 1..3) { // Limited look-ahead to avoid quadratic behavior
                if (originalIndex + lookAhead < originalLines.size &&
                    revisedLines[revisedIndex] == originalLines[originalIndex + lookAhead]) {
                    // Found match in original, add unmatched lines from revised
                    originalIndex += lookAhead
                    found = true
                    break
                }
                if (revisedIndex + lookAhead < revisedLines.size &&
                    originalLines[originalIndex] == revisedLines[revisedIndex + lookAhead]) {
                    // Found match in revised, add unmatched lines from original
                    revisedIndex += lookAhead
                    found = true
                    break
                }
            }
            if (!found) {
                // No quick match found, move both indices
                originalIndex++
                revisedIndex++
            }
        }
    }
    
    return matches
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

private fun diffLine(line1: String, line2: String): String {
    val chars1 = line1.toList()
    val chars2 = line2.toList()
    
    val builder = StringBuilder()
    var i = 0
    var j = 0
    var lastMatchEnd = 0
    
    // Find matching and differing sections
    while (i < chars1.size || j < chars2.size) {
        // If we have corresponding characters, check if they match
        if (i < chars1.size && j < chars2.size && chars1[i] == chars2[j]) {
            builder.append(chars1[i])
            i++
            j++
            lastMatchEnd = builder.length
            continue
        }
        
        // Mark all deletions from current position until next match or end
        val deletionsStart = builder.length
        while (i < chars1.size && (j >= chars2.size || chars1[i] != chars2[j])) {
            val c = chars1[i]
            builder.append(if (c == ' ') "[-⠀-]" else "[-$c-]")
            i++
        }
        
        // Mark all additions from current position until next match or end
        while (j < chars2.size && (i >= chars1.size || chars1[i] != chars2[j])) {
            val c = chars2[j]
            builder.append(if (c == ' ') "{+⠀+}" else "{+$c+}")
            j++
        }
    }
    
    return builder.toString()
}