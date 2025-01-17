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
 * Returns an empty string if the strings are identical.
 */
public infix fun String.diff(other: String): String {
    if (this == other) return ""
    
    val thisLines = this.lines()
    val otherLines = other.lines()
    
    val builder = StringBuilder()
    // Start with format description
    builder.append("""
        Text comparison failed:
        Format description:
        • [-c-] indicates character 'c' present in actual text but not in expected
        • {+c+} indicates character 'c' present in expected text but not in actual
        • ⠀ represents a space character in differences to make them visible
        • Each character change is marked separately for precise difference tracking
        • Line numbers are 1-based
        • Structural changes show exact position of insertion or deletion

    """.trimIndent()).append("\n")
    
    // Output actual text
    builder.append("┌─ actual\n")
    thisLines.forEach { line ->
        builder.append("│ ${line.visualizeTrailingSpaces()}\n")
    }
    
    // Output expected text
    builder.append("└─ differs from expected\n")
    otherLines.forEach { line ->
        builder.append("│ ${line.visualizeTrailingSpaces()}\n")
    }
    
    // Output differences
    builder.append("└─ differences\n")
    
    if (thisLines.size == 1 && otherLines.size == 1) {
        // Single line comparison
        builder.append("  • line 1: strings differ\n")
        builder.append("    - actual:   \"${thisLines[0]}\"\n")
        builder.append("    - expected: \"${otherLines[0]}\"\n")
        builder.append("    - changes:  \"${diffLine(thisLines[0], otherLines[0])}\"\n")
    } else {
        // Find matching blocks
        val matches = findMatchingBlocks(thisLines, otherLines)
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
                        builder.append("    + ${otherLines[otherIndex]}\n")
                        otherIndex++
                    }
                    thisIndex < match.thisStart && otherIndex >= match.otherStart -> {
                        // Deletion in this - skip for now as we focus on additions
                        thisIndex++
                        currentLine++
                    }
                    thisIndex < match.thisStart && otherIndex < match.otherStart -> {
                        // Lines differ
                        val thisLine = thisLines[thisIndex]
                        val otherLine = otherLines[otherIndex]
                        
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
                if (thisLines[thisIndex] != otherLines[otherIndex]) {
                    val thisLine = thisLines[thisIndex]
                    val otherLine = otherLines[otherIndex]
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
        while (otherIndex < otherLines.size) {
            builder.append("  • structural: missing line after line ${currentLine - 1}\n")
            builder.append("    + ${otherLines[otherIndex]}\n")
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
    // Find longest common prefix and suffix
    var prefixLen = 0
    val minLen = minOf(line1.length, line2.length)
    while (prefixLen < minLen && line1[prefixLen] == line2[prefixLen]) {
        prefixLen++
    }
    
    var suffixLen = 0
    while (suffixLen < minLen - prefixLen && 
           line1[line1.length - 1 - suffixLen] == line2[line2.length - 1 - suffixLen]) {
        suffixLen++
    }
    
    val builder = StringBuilder()
    
    // Add common prefix
    if (prefixLen > 0) {
        builder.append(line1.substring(0, prefixLen))
    }
    
    // Handle the differing part character by character
    val chars1 = line1.substring(prefixLen, line1.length - suffixLen).toList()
    val chars2 = line2.substring(prefixLen, line2.length - suffixLen).toList()
    
    // First output all deletions
    chars1.forEach { c ->
        builder.append(if (c == ' ') "[-⠀-]" else "[-$c-]")
    }
    
    // Then output all additions
    chars2.forEach { c ->
        builder.append(if (c == ' ') "{+⠀+}" else "{+$c+}")
    }
    
    // Add common suffix
    if (suffixLen > 0) {
        builder.append(line1.substring(line1.length - suffixLen))
    }
    
    return builder.toString()
}