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

package com.xemantic.kotlin.test.coroutines

import com.xemantic.kotlin.test.assert
import com.xemantic.kotlin.test.be
import com.xemantic.kotlin.test.have
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

class SuspendShouldTest {

    data class AsyncMessage(
        val id: Int,
        val content: String
    )

    data class AsyncUser(
        val name: String,
        val messages: List<AsyncMessage>
    )

    private suspend fun fetchUser(): AsyncUser {
        delay(10)
        return AsyncUser(
            name = "Alice",
            messages = listOf(
                AsyncMessage(1, "Hello"),
                AsyncMessage(2, "World")
            )
        )
    }

    private suspend fun fetchMessage(id: Int): AsyncMessage? {
        delay(5)
        return when (id) {
            1 -> AsyncMessage(1, "Hello")
            2 -> AsyncMessage(2, "World")
            else -> null
        }
    }

    private suspend fun calculateSum(a: Int, b: Int): Int {
        delay(1)
        return a + b
    }

    @Test
    fun `should pass all assertions on suspend function result`() = runTest {
        fetchUser() should {
            have(name == "Alice")
            have(messages.size == 2)
            messages[0] should {
                have(id == 1)
                have(content == "Hello")
            }
            messages[1] should {
                have(id == 2)
                have(content == "World")
            }
        }
    }

    @Test
    fun `should work with nested suspend calls in should block`() = runTest {
        fetchUser() should {
            have(name == "Alice")
            fetchMessage(messages[0].id) should {
                be<AsyncMessage>()
                have(content == "Hello")
            }
        }
    }

    @Test
    fun `should work with suspend function assertions`() = runTest {
        calculateSum(2, 3) should {
            assert(this == 5)
        }
    }

    @Test
    fun `should fail when asserting on null suspend result`() = runTest {
        val nullMessage: AsyncMessage? = fetchMessage(999)
        val exception = assertFailsWith<AssertionError> {
            nullMessage should {}
        }
        assertContains(exception.message!!, "null")
    }

    @Test
    fun `should fail when asserting wrong type on suspend result`() = runTest {
        val exception = assertFailsWith<AssertionError> {
            fetchUser() should {
                be<String>()
            }
        }
        assertNotNull(exception.message)
        assertContains(exception.message!!,
            """
                |AsyncUser(name=Alice, messages=[AsyncMessage(id=1, content=Hello), AsyncMessage(id=2, content=World)])
                ||- should be of type
            """.trimMargin()
        )
    }

    @Test
    fun `should fail when asserting wrong user name on suspend result`() = runTest {
        val exception = assertFailsWith<AssertionError> {
            fetchUser() should {
                have(name == "Bob")
            }
        }
        assertEquals(
            expected = """
                |
                |have(name == "Bob")
                |     |    |
                |     |    false
                |     Alice
                |
            """.trimMargin(),
            actual = exception.message
        )
    }

    @Test
    fun `should fail when asserting wrong message count on suspend result`() = runTest {
        val exception = assertFailsWith<AssertionError> {
            fetchUser() should {
                have(messages.size == 3)
            }
        }
        assertEquals(
            expected = """
                |
                |have(messages.size == 3)
                |     |        |    |
                |     |        |    false
                |     |        2
                |     [AsyncMessage(id=1, content=Hello), AsyncMessage(id=2, content=World)]
                |
            """.trimMargin(),
            actual = exception.message
        )
    }

    @Test
    fun `should fail when asserting wrong nested message content on suspend result`() = runTest {
        val exception = assertFailsWith<AssertionError> {
            fetchUser() should {
                messages[0] should {
                    have(content == "Goodbye")
                }
            }
        }
        assertEquals(
            expected = """
                |
                |have(content == "Goodbye")
                |     |       |
                |     |       false
                |     Hello
                |
            """.trimMargin(),
            actual = exception.message
        )
    }

    @Test
    fun `should fail when nested suspend call assertion fails`() = runTest {
        val exception = assertFailsWith<AssertionError> {
            fetchUser() should {
                fetchMessage(messages[0].id) should {
                    have(content == "Goodbye")
                }
            }
        }
        assertEquals(
            expected = """
                |
                |have(content == "Goodbye")
                |     |       |
                |     |       false
                |     Hello
                |
            """.trimMargin(),
            actual = exception.message
        )
    }

    @Test
    fun `should fail when suspend calculation assertion fails`() = runTest {
        val exception = assertFailsWith<AssertionError> {
            calculateSum(2, 3) should {
                assert(this == 6)
            }
        }
        assertEquals(
            expected = """
                |
                |assert(this == 6)
                |       |    |
                |       |    false
                |       5
                |
            """.trimMargin(),
            actual = exception.message
        )
    }

}