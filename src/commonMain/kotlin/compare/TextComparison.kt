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
    builder.append("Text comparison failed:\n")
    
    // Output actual text
    builder.append("┌─ actual\n")
    thisLines.forEach { line ->
        builder.append("│ $line\n")
    }
    
    // Output expected text
    builder.append("└─ differs from expected\n")
    otherLines.forEach { line ->
        builder.append("│ $line\n")
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
        // Multiline comparison
        var lineNumber = 1
        val minLines = minOf(thisLines.size, otherLines.size)
        
        for (i in 0 until minLines) {
            if (thisLines[i] != otherLines[i]) {
                builder.append("  • line ${lineNumber}: strings differ\n")
                builder.append("    - actual:   \"${thisLines[i]}\"\n")
                builder.append("    - expected: \"${otherLines[i]}\"\n")
                builder.append("    - changes:  \"${diffLine(thisLines[i], otherLines[i])}\"\n")
            }
            lineNumber++
        }
    }
    
    return builder.toString()
}

private fun diffLine(line1: String, line2: String): String {
    val builder = StringBuilder()
    var i = 0
    var j = 0
    
    while (i < line1.length || j < line2.length) {
        when {
            i >= line1.length -> {
                builder.append("{+${line2[j]}+}")
                j++
            }
            j >= line2.length -> {
                builder.append("[-${line1[i]}-]")
                i++
            }
            line1[i] == line2[j] -> {
                builder.append(line1[i])
                i++
                j++
            }
            else -> {
                builder.append("[-${line1[i]}-]")
                if (j < line2.length) {
                    builder.append("{+${line2[j]}+}")
                }
                i++
                j++
            }
        }
    }
    
    return builder.toString()
}