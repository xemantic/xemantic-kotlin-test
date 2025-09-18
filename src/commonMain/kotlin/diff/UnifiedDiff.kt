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

package com.xemantic.kotlin.test.diff

/**
 * Minimal, allocation-friendly unified diff generator for Kotlin Multiplatform.
 *
 * It operates on lists of strings (lines) and produces a unified diff with
 * a configurable [contextSize]. In this project we use [contextSize] of 0.
 */
internal object UnifiedDiff {

    fun generate(
        originalName: String,
        revisedName: String,
        original: List<String>,
        revised: List<String>,
        contextSize: Int = 0,
    ): List<String> {
        val ops = diffOps(original, revised)
        val result = ArrayList<String>()
        result += "--- $originalName"
        result += "+++ $revisedName"

        var origLine = 1
        var revLine = 1
        var idx = 0
        while (idx < ops.size) {
            val op = ops[idx]
            if (op is Op.Equal) {
                origLine++
                revLine++
                idx++
                continue
            }
            // Start of a hunk
            val hunkOrigStart = origLine
            val hunkRevStart = revLine
            val deleted = ArrayList<String>()
            val inserted = ArrayList<String>()
            var j = idx
            while (j < ops.size) {
                when (val o = ops[j]) {
                    is Op.Equal -> break
                    is Op.Delete -> {
                        deleted += o.s
                        origLine++
                    }
                    is Op.Insert -> {
                        inserted += o.s
                        revLine++
                    }
                }
                j++
            }

            // With contextSize == 0 we don't include equal/context lines.
            // Header counts represent number of lines from each side in the hunk.
            val origCount = deleted.size
            val revCount = inserted.size
            result += "@@ -$hunkOrigStart,$origCount +$hunkRevStart,$revCount @@"
            for (d in deleted) result += "-$d"
            for (a in inserted) result += "+$a"

            idx = j
        }

        return result
    }

    private sealed class Op {
        class Equal(val s: String) : Op()
        class Delete(val s: String) : Op()
        class Insert(val s: String) : Op()
    }

    private fun diffOps(a: List<String>, b: List<String>): List<Op> {
        val n = a.size
        val m = b.size
        if (n == 0 && m == 0) return emptyList()
        // Myers' O(ND) diff to get minimal SES
        val max = n + m
        val size = 2 * max + 1
        val v = IntArray(size)
        val trace = ArrayList<IntArray>(max + 1)

        outer@ for (d in 0..max) {
            val vSnap = v.copyOf()
            trace.add(vSnap)
            var k = -d
            while (k <= d) {
                val idx = k + max
                val x = if (k == -d || (k != d && v[idx - 1] < v[idx + 1])) v[idx + 1]
                else v[idx - 1] + 1
                var y = x - k
                var x2 = x
                var y2 = y
                while (x2 < n && y2 < m && a[x2] == b[y2]) {
                    x2++; y2++
                }
                v[idx] = x2
                if (x2 >= n && y2 >= m) {
                    // Reached the end
                    // Replace last snapshot with the final state
                    trace[trace.size - 1] = v.copyOf()
                    break@outer
                }
                k += 2
            }
        }

        // Backtrack
        val opsRev = ArrayList<Op>()
        var x = n
        var y = m
        for (d in trace.size - 1 downTo 0) {
            val vSnap = trace[d]
            val k = x - y
            val idx = k + max
            val prevK = if (k == -d || (k != d && vSnap[idx - 1] < vSnap[idx + 1])) k + 1 else k - 1
            val prevX = vSnap[prevK + max]
            val prevY = prevX - prevK
            // Traverse equal (snake)
            while (x > prevX && y > prevY) {
                opsRev.add(Op.Equal(a[x - 1]))
                x--; y--
            }
            if (d > 0) {
                when {
                    x == prevX -> { // insertion
                        opsRev.add(Op.Insert(b[y - 1]))
                        y--
                    }
                    y == prevY -> { // deletion
                        opsRev.add(Op.Delete(a[x - 1]))
                        x--
                    }
                }
            }
        }
        opsRev.reverse()
        return opsRev
    }
}
