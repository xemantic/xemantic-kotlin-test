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
    // Find the common prefix length
    var prefixLength = 0
    val minLength = minOf(line1.length, line2.length)
    while (prefixLength < minLength && line1[prefixLength] == line2[prefixLength]) {
        prefixLength++
    }
    
    // Find the common suffix length
    var suffixLength = 0
    while (suffixLength < minLength - prefixLength &&
           line1[line1.length - 1 - suffixLength] == line2[line2.length - 1 - suffixLength]) {
        suffixLength++
    }
    
    val builder = StringBuilder()
    
    // Add common prefix
    if (prefixLength > 0) {
        builder.append(line1.substring(0, prefixLength))
    }
    
    // Process the different parts
    val diffStart1 = prefixLength
    val diffEnd1 = line1.length - suffixLength
    val diffStart2 = prefixLength
    val diffEnd2 = line2.length - suffixLength
    
    // First output all deletions
    for (i in diffStart1 until diffEnd1) {
        if (line1[i] == ' ') {
            builder.append("[ -]")
        } else {
            builder.append("[-${line1[i]}-]")
        }
    }
    
    // Then output all additions
    for (j in diffStart2 until diffEnd2) {
        builder.append("{+${line2[j]}+}")
    }
    
    // Add common suffix
    if (suffixLength > 0) {
        builder.append(line1.substring(line1.length - suffixLength))
    }
    
    return builder.toString()
}