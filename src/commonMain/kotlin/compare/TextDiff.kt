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
    
    // Output actual text
    builder.append("┌─ actual\n")
    originalLines.forEach { line ->
        builder.append("│ ${line.visualizeTrailingSpaces()}\n")
    }
    
    // Output expected text
    builder.append("└─ differs from expected\n")
    revisedLines.forEach { line ->
        builder.append("│ ${line.visualizeTrailingSpaces()}\n")
    }
    
    // Output differences
    builder.append("└─ differences\n")
    
    if (originalLines.size == 1 && revisedLines.size == 1) {
        // Single line comparison
        builder.append("  • line 1: strings differ\n")
        builder.append("    - actual:   \"${originalLines[0]}\"\n")
        builder.append("    - expected: \"${revisedLines[0]}\"\n")
        builder.append("    - changes:  \"${diffLine(originalLines[0], revisedLines[0])}\"\n")
    } else {
        // Find matching blocks
        val matches = findMatchingBlocks(originalLines, revisedLines)
        var thisIndex = 0
        var otherIndex = 0
        var currentLine = 1
        
        matches.forEach { match ->
            // Handle differences before the match
            while (thisIndex < match.thisStart || otherIndex < match.otherStart) {
                when {
                    thisIndex >= match.thisStart && otherIndex < match.otherStart -> {
                        // Addition in other
                        builder.append("  • structural: missing line after line ${currentLine - 1}\n")
                        builder.append("    + ${revisedLines[otherIndex]}\n")
                        otherIndex++
                    }
                    thisIndex < match.thisStart && otherIndex >= match.otherStart -> {
                        // Deletion in this - skip for now as we focus on additions
                        thisIndex++
                        currentLine++
                    }
                    thisIndex < match.thisStart && otherIndex < match.otherStart -> {
                        // Lines differ
                        val thisLine = originalLines[thisIndex]
                        val otherLine = revisedLines[otherIndex]
                        
                        if (thisLine.trim() == otherLine.trim()) {
                            if (thisLine.countTrailingSpaces() != otherLine.countTrailingSpaces()) {
                                // Trailing whitespace difference
                                builder.append("  • line $currentLine: trailing whitespace difference\n")
                                builder.append("    - actual:   \"${thisLine.visualizeTrailingSpaces()}\"\n")
                                builder.append("    - expected: \"${otherLine.visualizeTrailingSpaces()}\"\n")
                                val trailingDiff = thisLine.countTrailingSpaces() - otherLine.countTrailingSpaces()
                                builder.append("    - changes:  \"${thisLine.trimEnd()}${
                                    "[-⠀-]".repeat(trailingDiff)}\"\n")
                            } else {
                                // Indentation difference
                                val thisSpaces = thisLine.countLeadingSpaces()
                                val otherSpaces = otherLine.countLeadingSpaces()
                                builder.append("  • line $currentLine: indentation difference\n")
                                builder.append("    - actual:   \"${thisLine}\" ($thisSpaces spaces)\n")
                                builder.append("    - expected: \"${otherLine}\"  ($otherSpaces spaces)\n")
                                builder.append("    - changes:  \"[-⠀-]${thisLine.trimStart()}\"\n")
                            }
                        } else {
                            builder.append("  • line $currentLine: strings differ\n")
                            builder.append("    - actual:   \"${thisLine}\"\n")
                            builder.append("    - expected: \"${otherLine}\"\n")
                            builder.append("    - changes:  \"${diffLine(thisLine, otherLine)}\"\n")
                        }
                        thisIndex++
                        otherIndex++
                        currentLine++
                    }
                }
            }
            
            // Skip matching block
            repeat(match.length) {
                if (originalLines[thisIndex] != revisedLines[otherIndex]) {
                    val thisLine = originalLines[thisIndex]
                    val otherLine = revisedLines[otherIndex]
                    builder.append("  • line $currentLine: strings differ\n")
                    builder.append("    - actual:   \"${thisLine}\"\n")
                    builder.append("    - expected: \"${otherLine}\"\n")
                    builder.append("    - changes:  \"${diffLine(thisLine, otherLine)}\"\n")
                }
                thisIndex++
                otherIndex++
                currentLine++
            }
        }
        
        // Handle remaining lines
        while (otherIndex < revisedLines.size) {
            builder.append("  • structural: missing line after line ${currentLine - 1}\n")
            builder.append("    + ${revisedLines[otherIndex]}\n")
            otherIndex++
        }
    }
    
    return builder.toString()
}

private data class Match(val thisStart: Int, val otherStart: Int, val length: Int)

private fun findMatchingBlocks(thisLines: List<String>, otherLines: List<String>): List<Match> {
    val matches = mutableListOf<Match>()
    var thisIndex = 0
    var otherIndex = 0
    
    while (thisIndex < thisLines.size && otherIndex < otherLines.size) {
        if (thisLines[thisIndex] == otherLines[otherIndex]) {
            // Found a match, look for more matching lines
            val startThis = thisIndex
            val startOther = otherIndex
            var length = 0
            
            while (thisIndex < thisLines.size && 
                   otherIndex < otherLines.size && 
                   thisLines[thisIndex] == otherLines[otherIndex]) {
                thisIndex++
                otherIndex++
                length++
            }
            
            if (length > 0) {
                matches.add(Match(startThis, startOther, length))
            }
        } else {
            // Try to find next match
            var found = false
            for (lookAhead in 1..3) { // Limited look-ahead to avoid quadratic behavior
                if (thisIndex + lookAhead < thisLines.size && 
                    otherLines[otherIndex] == thisLines[thisIndex + lookAhead]) {
                    // Found match in this, add unmatched lines from other
                    thisIndex += lookAhead
                    found = true
                    break
                }
                if (otherIndex + lookAhead < otherLines.size && 
                    thisLines[thisIndex] == otherLines[otherIndex + lookAhead]) {
                    // Found match in other, add unmatched lines from this
                    otherIndex += lookAhead
                    found = true
                    break
                }
            }
            if (!found) {
                // No quick match found, move both indices
                thisIndex++
                otherIndex++
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