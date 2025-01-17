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
        
        // Basic character-by-character diff
        val changes = StringBuilder()
        var i = 0
        var j = 0
        while (i < thisLines[0].length || j < otherLines[0].length) {
            when {
                i >= thisLines[0].length -> {
                    changes.append("{+${otherLines[0][j]}+}")
                    j++
                }
                j >= otherLines[0].length -> {
                    changes.append("[-${thisLines[0][i]}-]")
                    i++
                }
                thisLines[0][i] == otherLines[0][j] -> {
                    changes.append(thisLines[0][i])
                    i++
                    j++
                }
                else -> {
                    changes.append("[-${thisLines[0][i]}-]")
                    if (j < otherLines[0].length) {
                        changes.append("{+${otherLines[0][j]}+}")
                    }
                    i++
                    j++
                }
            }
        }
        builder.append("    - changes:  \"$changes\"\n")
    }
    
    return builder.toString()
}