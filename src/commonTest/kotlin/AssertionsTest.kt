/*
 * Copyright 2024-2025 Kazimierz Pogoda / Xemantic
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

import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class AssertionsTest {

    // test classes to test assertions against

    data class Message(
        val id: Int,
        val content: List<Content>
    )

    interface Content {
        val type: String
    }

    data class Text(
        val text: String,
    ) : Content {

        override val type = "text"

    }

    data class Image(
        val path: String,
        val width: Int,
        val height: Int,
        val mediaType: MediaType
    ) : Content {

        override val type = "image"

        data class MediaType(
            val type: String
        )

    }

    /**
     * Message is our main test class - the root in hierarchical structure.
     * It is nullable, because we also want to test if `should` works with nullable instances.
     */
    val message: Message? = Message(
        id = 42,
        content = listOf(
            Text(text = "Hello there"),
            Image(
                path = "image.png",
                width = 1024,
                height = 768,
                mediaType = Image.MediaType("image/png")
            )
        )
    )


    @Test
    fun `Should pass all assertions on default message instance`() {
        message should {
            have(id == 42)
            have(content.size == 2)
            content[0] should {
                be<Text>()
                have(type == "text")
                have("Hello" in text)
            }
            content[1] should {
                be<Image>()
                have(type == "image")
                have(width >= 800)
                have(height >= 600)
                mediaType should {
                    have(type == "image/png")
                }
            }
        }
    }

    @Test
    fun `Should fail when asserting on null object`() {
        val nullMessage: Message? = null
        val exception = assertFailsWith<AssertionError> {
            nullMessage should {}
        }
        assertContains(exception.message!!, "null")
    }

    @Test
    fun `Should fail when asserting wrong instance type`() {
        val exception = assertFailsWith<AssertionError> {
            message should {
                be<String>()
            }
        }
        assertContains(exception.message!!, "should: be of type")
    }

    @Test
    fun `Should fail when asserting wrong message id`() {
        val exception = assertFailsWith<AssertionError> {
            message should {
                have(id == 0)
            }
        }
        assertEquals(
            expected = """
                |Message(id=42, content=[Text(text=Hello there), Image(path=image.png, width=1024, height=768, mediaType=MediaType(type=image/png))])
                | should:
                |have(id == 0)
                |     |  |
                |     |  false
                |     42
                |     Message(id=42, content=[Text(text=Hello there), Image(path=image.png, width=1024, height=768, mediaType=MediaType(type=image/png))])
                |
      """.trimMargin(),
            actual = exception.message
        )
    }

    @Test
    fun `Should fail when asserting empty message content`() {
        val exception = assertFailsWith<AssertionError> {
            message should {
                have(content.isEmpty())
            }
        }
        assertEquals(
            expected = """
                |Message(id=42, content=[Text(text=Hello there), Image(path=image.png, width=1024, height=768, mediaType=MediaType(type=image/png))])
                | should:
                |have(content.isEmpty())
                |     |       |
                |     |       false
                |     [Text(text=Hello there), Image(path=image.png, width=1024, height=768, mediaType=MediaType(type=image/png))]
                |     Message(id=42, content=[Text(text=Hello there), Image(path=image.png, width=1024, height=768, mediaType=MediaType(type=image/png))])
                |
      """.trimMargin(),
            actual = exception.message
        )
    }

    @Test
    fun `Should fail when asserting wrong content type`() {
        val exception = assertFailsWith<AssertionError> {
            message should {
                content[0] should {
                    be<Image>()
                }
            }
        }
        assertContains(
            exception.message!!,
            $$"""
                |Message(id=42, content=[Text(text=Hello there), Image(path=image.png, width=1024, height=768, mediaType=MediaType(type=image/png))])
                | containing:
                |Text(text=Hello there)
                | should: be of type
      """.trimMargin()
        )
    }

    @Test
    fun `Should fail when asserting wrong message-image-mediaType`() {
        val exception = assertFailsWith<AssertionError> {
            message should {
                content[1] should {
                    be<Image>()
                    mediaType should {
                        have(type == "image/jpeg")
                    }
                }
            }
        }
        assertEquals(
            expected = """
                |Message(id=42, content=[Text(text=Hello there), Image(path=image.png, width=1024, height=768, mediaType=MediaType(type=image/png))])
                | containing:
                |Image(path=image.png, width=1024, height=768, mediaType=MediaType(type=image/png))
                | containing:
                |MediaType(type=image/png)
                | should:
                |have(type == "image/jpeg")
                |     |    |
                |     |    false
                |     image/png
                |     MediaType(type=image/png)
                |
      """.trimMargin(),
            actual = exception.message
        )
    }

    @Test
    fun `Should fail when assertion resolves to false`() {
        val exception = assertFailsWith<AssertionError> {
            assert(2 + 2 == 2 + 3)
        }
        assertEquals(
            expected = """
                |
                |assert(2 + 2 == 2 + 3)
                |         |   |    |
                |         |   |    5
                |         |   false
                |         4
                |
      """.trimMargin(),
            actual = exception.message
        )
    }

}
