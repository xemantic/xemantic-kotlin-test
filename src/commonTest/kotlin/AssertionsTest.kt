/*
 * Copyright 2024 Kazimierz Pogoda / Xemantic
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
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
    val height: Int
  ) : Content {
    override val type = "image"
  }

  data class Message(
    val id: Int,
    val content: List<Content>
  )

  val message: Message? = Message(
    id = 42,
    content = listOf(
      Text(text = "Hello there"),
      Image(
        path = "image.png",
        width = 1024,
        height = 768
      )
    )
  )

  @Test
  fun `Should pass`() {
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
      }
    }
  }

  @Test
  fun `Should fail when asserting on null object`() {
    val nullMessage: Message? = null
    assertFailsWith<AssertionError> {
      nullMessage should {}
    } should {
      have(message == "actual value is null")
    }
  }

  @Test
  fun `Should fail when asserting wrong instance type`() {
    assertFailsWith<AssertionError> {
      message should {
        be<String>()
      }
    } should {
      assertContains(message!!, "Expected value to be of type")
    }
  }

  @Test
  fun `Should fail when asserting empty message content`() {
    val exception = assertFailsWith<AssertionError> {
      message should {
        have(content.isEmpty())
      }
    }
    assertEquals("""
      |
      |have(content.isEmpty())
      |     |       |
      |     |       false
      |     [Text(text=Hello there), Image(path=image.png, width=1024, height=768)]
      |
      """.trimMargin(),
      exception.message
    )
  }

}
