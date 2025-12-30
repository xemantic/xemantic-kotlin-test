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

public infix fun String?.sameAs(expected: String?) {
  if (this == expected) return
  
  val actualNonNull = this ?: ""
  val expectedNonNull = expected ?: ""
  
  val diff = computeUnifiedDiff(actualNonNull, expectedNonNull)
  throw AssertionError(diff)
}

private fun computeUnifiedDiff(actual: String, expected: String): String {
  val actualLines = if (actual.isEmpty()) emptyList() else {
    val split = actual.split('\n')
    if (actual.endsWith('\n') && split.last().isEmpty()) split.dropLast(1) else split
  }
  val expectedLines = if (expected.isEmpty()) emptyList() else {
    val split = expected.split('\n')
    if (expected.endsWith('\n') && split.last().isEmpty()) split.dropLast(1) else split
  }
  
  val actualHasTrailingNewline = actual.endsWith('\n')
  val expectedHasTrailingNewline = expected.endsWith('\n')
  
  val edits = computeDiff(expectedLines, actualLines)
  
  return formatUnifiedDiff(
    expectedLines,
    actualLines,
    edits,
    expectedHasTrailingNewline,
    actualHasTrailingNewline
  )
}

private data class Edit(val type: EditType, val oldIndex: Int, val newIndex: Int)

private enum class EditType {
  EQUAL, DELETE, INSERT
}

private fun computeDiff(expected: List<String>, actual: List<String>): List<Edit> {
  val n = expected.size
  val m = actual.size
  val max = n + m
  val maxD = 500
  
  val v = IntArray(2 * max + 1)
  val trace = mutableListOf<IntArray>()
  
  for (d in 0..minOf(maxD, max)) {
    for (k in -d..d step 2) {
      val kIndex = k + max
      
      var x = if (k == -d || (k != d && v[kIndex - 1] < v[kIndex + 1])) {
        v[kIndex + 1]
      } else {
        v[kIndex - 1] + 1
      }
      
      var y = x - k
      
      while (x < n && y < m && expected[x] == actual[y]) {
        x++
        y++
      }
      
      v[kIndex] = x
      
      if (x >= n && y >= m) {
        trace.add(v.copyOf())
        return backtrack(expected, actual, trace, maxD)
      }
    }
    trace.add(v.copyOf())
  }
  
  return buildNaiveDiff(expected, actual)
}

private fun buildNaiveDiff(expected: List<String>, actual: List<String>): List<Edit> {
  val edits = mutableListOf<Edit>()
  for (i in expected.indices) {
    edits.add(Edit(EditType.DELETE, i, -1))
  }
  for (i in actual.indices) {
    edits.add(Edit(EditType.INSERT, -1, i))
  }
  return edits
}

private fun backtrack(
  expected: List<String>,
  actual: List<String>,
  trace: List<IntArray>,
  maxD: Int
): List<Edit> {
  val n = expected.size
  val m = actual.size
  val max = n + m
  
  var x = n
  var y = m
  
  val edits = mutableListOf<Edit>()
  
  for (d in minOf(trace.size - 1, maxD) downTo 0) {
    val v = trace[d]
    val k = x - y
    val kIndex = k + max
    
    val prevK = if (k == -d || (k != d && v[kIndex - 1] < v[kIndex + 1])) {
      k + 1
    } else {
      k - 1
    }
    
    val prevX = if (prevK + max in v.indices) v[prevK + max] else 0
    val prevY = prevX - prevK
    
    while (x > prevX && y > prevY) {
      x--
      y--
      edits.add(0, Edit(EditType.EQUAL, x, y))
    }
    
    if (d > 0) {
      if (x > prevX) {
        x--
        edits.add(0, Edit(EditType.DELETE, x, -1))
      } else {
        y--
        edits.add(0, Edit(EditType.INSERT, -1, y))
      }
    }
  }
  
  return edits
}

private fun formatUnifiedDiff(
  expectedLines: List<String>,
  actualLines: List<String>,
  edits: List<Edit>,
  expectedHasTrailingNewline: Boolean,
  actualHasTrailingNewline: Boolean
): String {
  val adjustedEdits = edits.toMutableList()
  
  if (!expectedHasTrailingNewline) {
    val lastExpectedLineIndex = expectedLines.lastIndex
    val editAtLastExpectedLine = adjustedEdits.indexOfLast {it.oldIndex == lastExpectedLineIndex }
    if (editAtLastExpectedLine >= 0 && adjustedEdits[editAtLastExpectedLine].type == EditType.EQUAL) {
      val edit = adjustedEdits[editAtLastExpectedLine]
      adjustedEdits[editAtLastExpectedLine] = Edit(EditType.DELETE, edit.oldIndex, -1)
      adjustedEdits.add(editAtLastExpectedLine + 1, Edit(EditType.INSERT, -1, edit.newIndex))
    }
  }
  
  if (!actualHasTrailingNewline && expectedHasTrailingNewline) {
    val lastActualLineIndex = actualLines.lastIndex
    val editAtLastActualLine = adjustedEdits.indexOfLast { it.newIndex == lastActualLineIndex }
    if (editAtLastActualLine >= 0 && adjustedEdits[editAtLastActualLine].type == EditType.EQUAL) {
      val edit = adjustedEdits[editAtLastActualLine]
      adjustedEdits[editAtLastActualLine] = Edit(EditType.DELETE, edit.oldIndex, -1)
      adjustedEdits.add(editAtLastActualLine + 1, Edit(EditType.INSERT, -1, edit.newIndex))
    }
  }
  
  val hunks = buildHunks(adjustedEdits, expectedLines, actualLines, contextSize = 3)
  val mergedHunks = mergeHunks(hunks, contextSize = 3)
  
  return buildString {
    append("--- expected\n")
    append("+++ actual\n")
    
    var totalChangedLines = 0
    var truncated = false
    
    for ((hunkIndex, hunk) in mergedHunks.withIndex()) {
      val hunkChangedLines = hunk.lines.count { it.type != EditType.EQUAL }
      
      if (totalChangedLines + hunkChangedLines > 100) {
        truncated = true
        
        val remainingBudget = 100 - totalChangedLines
        val truncatedHunk = truncateHunk(hunk, remainingBudget)
        
        if (truncatedHunk.lines.isNotEmpty()) {
          append(formatHunk(
            truncatedHunk,
            expectedLines,
            actualLines,
            expectedHasTrailingNewline,
            actualHasTrailingNewline
          ))
        }
        break
      }
      
      totalChangedLines += hunkChangedLines
      append(formatHunk(
        hunk,
        expectedLines,
        actualLines,
        expectedHasTrailingNewline,
        actualHasTrailingNewline
      ))
    }
    
    if (truncated) {
      append("\nDiff truncated: more than 100 lines changed\n\n")
      append("Expected: ${expectedLines.size} lines\n")
      append("Actual: ${actualLines.size} lines\n\n")
      append("The differences are too extensive to show in unified diff format.\n")
      append("Consider comparing smaller sections or reviewing the strings directly.\n")
    }
  }
}

private data class Hunk(
  val oldStart: Int,
  val oldCount: Int,
  val newStart: Int,
  val newCount: Int,
  val lines: List<HunkLine>
)

private data class HunkLine(val type: EditType, val oldIndex: Int, val newIndex: Int)

private fun buildHunks(
  edits: List<Edit>,
  expectedLines: List<String>,
  actualLines: List<String>,
  contextSize: Int
): List<Hunk> {
  if (edits.isEmpty()) return emptyList()
  
  val hunks = mutableListOf<Hunk>()
  var hunkLines = mutableListOf<HunkLine>()
  var hunkOldStart = -1
  var hunkNewStart = -1
  var lastChangeIndex = -1
  var contextAfterChange = 0
  
  for ((index, edit) in edits.withIndex()) {
    val isChange = edit.type != EditType.EQUAL
    
    if (isChange) {
      if (hunkLines.isEmpty()) {
        val contextStart = maxOf(0, index - contextSize)
        for (i in contextStart until index) {
          val e = edits[i]
          hunkLines.add(HunkLine(e.type, e.oldIndex, e.newIndex))
        }
        hunkOldStart = if (edits[contextStart].oldIndex >= 0) edits[contextStart].oldIndex else 0
        hunkNewStart = if (edits[contextStart].newIndex >= 0) edits[contextStart].newIndex else 0
      } else if (contextAfterChange > contextSize) {
        val removeCount = contextAfterChange - contextSize
        repeat(removeCount) { hunkLines.removeLast() }
        
        hunks.add(createHunk(hunkOldStart, hunkNewStart, hunkLines))
        
        hunkLines = mutableListOf()
        val contextStart = maxOf(0, index - contextSize)
        for (i in contextStart until index) {
          val e = edits[i]
          hunkLines.add(HunkLine(e.type, e.oldIndex, e.newIndex))
        }
        hunkOldStart = if (edits[contextStart].oldIndex >= 0) edits[contextStart].oldIndex else 0
        hunkNewStart = if (edits[contextStart].newIndex >= 0) edits[contextStart].newIndex else 0
      }
      
      hunkLines.add(HunkLine(edit.type, edit.oldIndex, edit.newIndex))
      lastChangeIndex = index
      contextAfterChange = 0
    } else if (hunkLines.isNotEmpty()) {
      hunkLines.add(HunkLine(edit.type, edit.oldIndex, edit.newIndex))
      contextAfterChange++
    }
  }
  
  if (hunkLines.isNotEmpty()) {
    if (contextAfterChange > contextSize) {
      val removeCount = contextAfterChange - contextSize
      repeat(removeCount) { hunkLines.removeLast() }
    }
    hunks.add(createHunk(hunkOldStart, hunkNewStart, hunkLines))
  }
  
  return hunks
}

private fun createHunk(oldStart: Int, newStart: Int, lines: List<HunkLine>): Hunk {
  var oldCount = 0
  var newCount = 0
  
  for (line in lines) {
    when (line.type) {
      EditType.EQUAL -> {
        oldCount++
        newCount++
      }
      EditType.DELETE -> oldCount++
      EditType.INSERT -> newCount++
    }
  }
  
  val adjustedOldStart = if (oldCount == 0) 0 else oldStart + 1
  val adjustedNewStart = if (newCount == 0) 0 else newStart + 1
  
  return Hunk(adjustedOldStart, oldCount, adjustedNewStart, newCount, lines)
}

private fun mergeHunks(hunks: List<Hunk>, contextSize: Int): List<Hunk> {
  if (hunks.size <= 1) return hunks
  
  val merged = mutableListOf<Hunk>()
  var current = hunks[0]
  
  for (i in 1 until hunks.size) {
    val next = hunks[i]
    val currentEnd = current.oldStart + current.oldCount - 1
    val gapSize = next.oldStart - currentEnd - 1
    
    if (gapSize <= contextSize * 2) {
      val mergedLines = current.lines.toMutableList()
      
      for (line in current.lines.reversed()) {
        if (line.type == EditType.EQUAL) {
          mergedLines.removeLast()
        } else {
          break
        }
      }
      
      var skipped = 0
      for (line in next.lines) {
        if (line.type == EditType.EQUAL && skipped < next.lines.indexOfFirst { it.type != EditType.EQUAL }) {
          skipped++
          continue
        }
        break
      }
      
      val firstOldIndex = if (current.lines.first().oldIndex >= 0) current.lines.first().oldIndex else 0
      val firstNewIndex = if (current.lines.first().newIndex >= 0) current.lines.first().newIndex else 0
      
      val missingOldStart = currentEnd + 1
      val missingOldEnd = next.oldStart - 1
      
      if (missingOldStart <= missingOldEnd) {
        for (oi in missingOldStart..missingOldEnd) {
          val ni = oi + (firstNewIndex - firstOldIndex)
          mergedLines.add(HunkLine(EditType.EQUAL, oi - 1, ni - 1))
        }
      }
      
      mergedLines.addAll(next.lines)
      
      current = createHunk(firstOldIndex, firstNewIndex, mergedLines)
    } else {
      merged.add(current)
      current = next
    }
  }
  
  merged.add(current)
  return merged
}

private fun truncateHunk(hunk: Hunk, remainingBudget: Int): Hunk {
  var changedCount = 0
  val truncatedLines = mutableListOf<HunkLine>()
  
  for (line in hunk.lines) {
    if (line.type != EditType.EQUAL) {
      changedCount++
      if (changedCount > remainingBudget) break
    }
    truncatedLines.add(line)
  }
  
  if (truncatedLines.isEmpty()) {
    return Hunk(hunk.oldStart, 0, hunk.newStart, 0, emptyList())
  }
  
  val firstOldIndex = truncatedLines.first().oldIndex
  val firstNewIndex = truncatedLines.first().newIndex
  
  return createHunk(
    if (firstOldIndex >= 0) firstOldIndex else 0,
    if (firstNewIndex >= 0) firstNewIndex else 0,
    truncatedLines
  )
}

private fun formatHunk(
  hunk: Hunk,
  expectedLines: List<String>,
  actualLines: List<String>,
  expectedHasTrailingNewline: Boolean,
  actualHasTrailingNewline: Boolean
): String {
  if (hunk.lines.isEmpty()) return ""
  
  return buildString {
    val oldRange = when {
      hunk.oldCount == 0 -> "${hunk.oldStart},0"
      hunk.oldCount == 1 -> "${hunk.oldStart}"
      else -> "${hunk.oldStart},${hunk.oldCount}"
    }
    val newRange = when {
      hunk.newCount == 0 -> "${hunk.newStart},0"
      hunk.newCount == 1 -> "${hunk.newStart}"
      else -> "${hunk.newStart},${hunk.newCount}"
    }
    append("@@ -$oldRange +$newRange @@\n")
    
    for ((index, line) in hunk.lines.withIndex()) {
      val nextLine = if (index + 1 < hunk.lines.size) hunk.lines[index + 1] else null
      
      when (line.type) {
        EditType.EQUAL -> {
          append(" ${actualLines[line.newIndex]}\n")
        }
        EditType.DELETE -> {
          append("-${expectedLines[line.oldIndex]}\n")
          val isLastExpectedLine = line.oldIndex == expectedLines.lastIndex
          val nextIsNotDelete = nextLine?.type != EditType.DELETE
          if (isLastExpectedLine && !expectedHasTrailingNewline && nextIsNotDelete) {
            append("\\ No newline at end of file\n")
          }
        }
        EditType.INSERT -> {
          append("+${actualLines[line.newIndex]}\n")
          val isLastActualLine = line.newIndex == actualLines.lastIndex
          val nextIsNotInsert = nextLine?.type != EditType.INSERT
          if (isLastActualLine && !actualHasTrailingNewline && nextIsNotInsert) {
            append("\\ No newline at end of file\n")
          }
        }
      }
    }
  }
}
