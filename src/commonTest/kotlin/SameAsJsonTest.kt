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

import kotlin.test.Test
import kotlin.test.assertFailsWith

/**
 * Comprehensive test suite for [sameAsJson] function.
 *
 * The [sameAsJson] function should:
 * - Compare JSON strings
 * - Prettify the actual JSON before comparison
 * - Report differences in unified diff format
 * - Handle null receiver gracefully
 */
class SameAsJsonTest {

    @Test
    fun `should pass on equal JSON`() {
        """{"foo":"bar"}""" sameAsJson """
            {
              "foo": "bar"
            }
        """.trimIndent()
    }

    @Test
    fun `should pass on equal JSON with multiple properties`() {
        """{"foo":"bar","baz":42,"qux":true}""" sameAsJson """
            {
              "foo": "bar",
              "baz": 42,
              "qux": true
            }
        """.trimIndent()
    }

    @Test
    fun `should pass on equal JSON arrays`() {
        """[1,2,3]""" sameAsJson """
            [
              1,
              2,
              3
            ]
        """.trimIndent()
    }

    @Test
    fun `should pass on equal nested JSON objects`() {
        """{"outer":{"inner":{"value":"test"}}}""" sameAsJson """
            {
              "outer": {
                "inner": {
                  "value": "test"
                }
              }
            }
        """.trimIndent()
    }

    @Test
    fun `should pass on equal JSON with arrays of objects`() {
        """{"items":[{"id":1,"name":"first"},{"id":2,"name":"second"}]}""" sameAsJson """
            {
              "items": [
                {
                  "id": 1,
                  "name": "first"
                },
                {
                  "id": 2,
                  "name": "second"
                }
              ]
            }
        """.trimIndent()
    }

    @Test
    fun `should pass on equal JSON with special characters`() {
        """{"text":"Hello\nWorld\t!"}""" sameAsJson """
            {
              "text": "Hello\nWorld\t!"
            }
        """.trimIndent()
    }

    @Test
    fun `should pass on equal JSON with unicode characters`() {
        """{"emoji":"🎉","chinese":"你好"}""" sameAsJson """
            {
              "emoji": "🎉",
              "chinese": "你好"
            }
        """.trimIndent()
    }

    @Test
    fun `should pass on equal JSON with null values`() {
        """{"foo":null,"bar":"baz"}""" sameAsJson """
            {
              "foo": null,
              "bar": "baz"
            }
        """.trimIndent()
    }

    @Test
    fun `should pass on equal empty JSON object`() {
        "{}" sameAsJson "{}"
    }

    @Test
    fun `should pass on equal empty JSON array`() {
        "[]" sameAsJson "[]"
    }

    @Test
    fun `should fail on different string values`() {
        assertFailsWith<AssertionError> {
            """{"foo":"bar"}""" sameAsJson """
                {
                  "foo": "baz"
                }
            """.trimIndent()
        }.message sameAs """
            --- expected
            +++ actual
            @@ -1,3 +1,3 @@
             {
            -  "foo": "baz"
            +  "foo": "bar"
             }

        """.trimIndent()
    }

    @Test
    fun `should fail on different number values`() {
        assertFailsWith<AssertionError> {
            """{"count":42}""" sameAsJson """
                {
                  "count": 43
                }
            """.trimIndent()
        }.message sameAs """
            --- expected
            +++ actual
            @@ -1,3 +1,3 @@
             {
            -  "count": 43
            +  "count": 42
             }

        """.trimIndent()
    }

    @Test
    fun `should fail on different boolean values`() {
        assertFailsWith<AssertionError> {
            """{"flag":true}""" sameAsJson """
                {
                  "flag": false
                }
            """.trimIndent()
        }.message sameAs """
            --- expected
            +++ actual
            @@ -1,3 +1,3 @@
             {
            -  "flag": false
            +  "flag": true
             }

        """.trimIndent()
    }

    @Test
    fun `should fail on missing property`() {
        assertFailsWith<AssertionError> {
            """{"foo":"bar"}""" sameAsJson """
                {
                  "foo": "bar",
                  "baz": "qux"
                }
            """.trimIndent()
        }.message sameAs """
            --- expected
            +++ actual
            @@ -1,4 +1,3 @@
             {
            -  "foo": "bar",
            -  "baz": "qux"
            +  "foo": "bar"
             }

        """.trimIndent()
    }

    @Test
    fun `should fail on extra property`() {
        assertFailsWith<AssertionError> {
            """{"foo":"bar","extra":"value"}""" sameAsJson """
                {
                  "foo": "bar"
                }
            """.trimIndent()
        }.message sameAs """
            --- expected
            +++ actual
            @@ -1,3 +1,4 @@
             {
            -  "foo": "bar"
            +  "foo": "bar",
            +  "extra": "value"
             }

        """.trimIndent()
    }

    @Test
    fun `should fail on extra null property`() {
        assertFailsWith<AssertionError> {
            """{"foo":"bar","extra":null}""" sameAsJson """
                {
                  "foo": "bar"
                }
            """.trimIndent()
        }.message sameAs """
            --- expected
            +++ actual
            @@ -1,3 +1,4 @@
             {
            -  "foo": "bar"
            +  "foo": "bar",
            +  "extra": null
             }

        """.trimIndent()
    }

    @Test
    fun `should fail on different array length`() {
        assertFailsWith<AssertionError> {
            """[1,2,3]""" sameAsJson """
                [
                  1,
                  2
                ]
            """.trimIndent()
        }.message sameAs """
            --- expected
            +++ actual
            @@ -1,4 +1,5 @@
             [
               1,
            -  2
            +  2,
            +  3
             ]

        """.trimIndent()
    }

    @Test
    fun `should fail on different array values`() {
        assertFailsWith<AssertionError> {
            """[1,2,3]""" sameAsJson """
                [
                  1,
                  2,
                  4
                ]
            """.trimIndent()
        }.message sameAs """
            --- expected
            +++ actual
            @@ -1,5 +1,5 @@
             [
               1,
               2,
            -  4
            +  3
             ]

        """.trimIndent()
    }

    @Test
    fun `should fail on different nested values`() {
        assertFailsWith<AssertionError> {
            """{"outer":{"inner":"value1"}}""" sameAsJson """
                {
                  "outer": {
                    "inner": "value2"
                  }
                }
            """.trimIndent()
        }.message sameAs """
            --- expected
            +++ actual
            @@ -1,5 +1,5 @@
             {
               "outer": {
            -    "inner": "value2"
            +    "inner": "value1"
               }
             }

        """.trimIndent()
    }

    @Test
    fun `should fail on type mismatch - string vs number`() {
        assertFailsWith<AssertionError> {
            """{"value":"42"}""" sameAsJson """
                {
                  "value": 42
                }
            """.trimIndent()
        }.message sameAs """
            --- expected
            +++ actual
            @@ -1,3 +1,3 @@
             {
            -  "value": 42
            +  "value": "42"
             }

        """.trimIndent()
    }

    @Test
    fun `should fail on type mismatch - object vs array`() {
        assertFailsWith<AssertionError> {
            """{"data":{}}""" sameAsJson """
                {
                  "data": []
                }
            """.trimIndent()
        }.message sameAs """
            --- expected
            +++ actual
            @@ -1,3 +1,3 @@
             {
            -  "data": []
            +  "data": {}
             }

        """.trimIndent()
    }

    @Test
    fun `should fail on null vs value`() {
        assertFailsWith<AssertionError> {
            """{"foo":null}""" sameAsJson """
                {
                  "foo": "bar"
                }
            """.trimIndent()
        }.message sameAs """
            --- expected
            +++ actual
            @@ -1,3 +1,3 @@
             {
            -  "foo": "bar"
            +  "foo": null
             }

        """.trimIndent()
    }

    @Test
    fun `should fail when receiver is null`() {
        val actual: String? = null
        assertFailsWith<AssertionError> {
            actual sameAsJson """
                {
                  "foo": "bar"
                }
            """.trimIndent()
        }.message sameAs """
            The string is null, but expected to be: {
              "foo": "bar"
            }
        """.trimIndent()
    }

    @Test
    fun `should fail on different property order`() {
        // JSON objects property order matters - different order should fail
        assertFailsWith<AssertionError> {
            """{"foo":"bar","baz":"qux"}""" sameAsJson """
                {
                  "baz": "qux",
                  "foo": "bar"
                }
            """.trimIndent()
        }.message sameAs """
            --- expected
            +++ actual
            @@ -1,4 +1,4 @@
             {
            -  "baz": "qux",
            -  "foo": "bar"
            +  "foo": "bar",
            +  "baz": "qux"
             }

        """.trimIndent()
    }

    @Test
    fun `should pass on complex real-world JSON example`() {
        """{"user":{"id":123,"name":"John Doe","email":"john@example.com","roles":["admin","user"],"metadata":{"created":"2024-01-01","updated":"2024-12-31"}}}""" sameAsJson """
            {
              "user": {
                "id": 123,
                "name": "John Doe",
                "email": "john@example.com",
                "roles": [
                  "admin",
                  "user"
                ],
                "metadata": {
                  "created": "2024-01-01",
                  "updated": "2024-12-31"
                }
              }
            }
        """.trimIndent()
    }

    @Test
    fun `should handle JSON with floating point numbers`() {
        """{"pi":3.14159,"e":2.71828}""" sameAsJson """
            {
              "pi": 3.14159,
              "e": 2.71828
            }
        """.trimIndent()
    }

    @Test
    fun `should handle JSON with negative numbers`() {
        """{"temp":-5,"balance":-100.50}""" sameAsJson """
            {
              "temp": -5,
              "balance": -100.5
            }
        """.trimIndent()
    }

    @Test
    fun `should handle deeply nested JSON`() {
        """{"a":{"b":{"c":{"d":{"e":"deep"}}}}}""" sameAsJson """
            {
              "a": {
                "b": {
                  "c": {
                    "d": {
                      "e": "deep"
                    }
                  }
                }
              }
            }
        """.trimIndent()
    }

    @Test
    fun `should handle mixed array of different types`() {
        """{"mixed":[1,"two",true,null,{"nested":"object"},[1,2,3]]}""" sameAsJson """
            {
              "mixed": [
                1,
                "two",
                true,
                null,
                {
                  "nested": "object"
                },
                [
                  1,
                  2,
                  3
                ]
              ]
            }
        """.trimIndent()
    }

    @Test
    fun `should fail on invalid actual JSON - missing closing brace`() {
        assertFailsWith<AssertionError> {
            """{"foo":"bar"""" sameAsJson """
                {
                  "foo": "bar"
                }
            """.trimIndent()
        }.message sameAs """
            Invalid JSON: Unexpected JSON token at offset 12: Expected end of the object or comma at path: $
            JSON input: {"foo":"bar"
        """.trimIndent()
    }

    @Test
    fun `should fail on invalid actual JSON - missing quotes`() {
        assertFailsWith<AssertionError> {
            """{foo:bar}""" sameAsJson """
                {
                  "foo": "bar"
                }
            """.trimIndent()
        }.message sameAs """
            Invalid JSON: Unexpected JSON token at offset 1: Expected quotation mark '"', but had 'f' instead at path: $
            JSON input: {foo:bar}
        """.trimIndent()
    }

    @Test
    fun `should fail on invalid actual JSON - trailing comma`() {
        assertFailsWith<AssertionError> {
            """{"foo":"bar",}""" sameAsJson """
                {
                  "foo": "bar"
                }
            """.trimIndent()
        }.message sameAs """
            Invalid JSON: Unexpected JSON token at offset 12: Trailing comma before the end of JSON object at path: $
            Trailing commas are non-complaint JSON and not allowed by default. Use 'allowTrailingComma = true' in 'Json {}' builder to support them.
            JSON input: {"foo":"bar",}
        """.trimIndent()
    }

    @Test
    fun `should fail on invalid actual JSON - single quotes`() {
        assertFailsWith<AssertionError> {
            """{'foo':'bar'}""" sameAsJson """
                {
                  "foo": "bar"
                }
            """.trimIndent()
        }.message sameAs """
            Invalid JSON: Unexpected JSON token at offset 1: Expected quotation mark '"', but had ''' instead at path: $
            JSON input: {'foo':'bar'}
        """.trimIndent()
    }

    @Test
    fun `should fail on invalid actual JSON - incomplete string`() {
        assertFailsWith<AssertionError> {
            """{"foo":"bar}""" sameAsJson """
                {
                  "foo": "bar"
                }
            """.trimIndent()
        }.message sameAs """
            Invalid JSON: Unexpected JSON token at offset 11: Expected quotation mark '"', but had '}' instead at path: $
            JSON input: {"foo":"bar}
        """.trimIndent()
    }

    @Test
    fun `should fail on invalid actual JSON - not JSON at all`() {
        assertFailsWith<AssertionError> {
            """this is not JSON""" sameAsJson """
                {
                  "foo": "bar"
                }
            """.trimIndent()
        }.message sameAs """
            Invalid JSON: Unexpected JSON token at offset 6: Expected EOF after parsing, but had i instead at path: $
            JSON input: this is not JSON
        """.trimIndent()
    }

    @Test
    fun `should throw IllegalArgumentException on invalid expected JSON - missing closing brace`() {
        assertFailsWith<IllegalArgumentException> {
            """{"foo":"bar"}""" sameAsJson """{"foo":"bar""""
        }.message sameAs """
            Invalid expected JSON: Unexpected JSON token at offset 12: Expected end of the object or comma at path: $
            JSON input: {"foo":"bar"
        """.trimIndent()
    }

    @Test
    fun `should throw IllegalArgumentException on invalid expected JSON - missing quotes`() {
        assertFailsWith<IllegalArgumentException> {
            """{"foo":"bar"}""" sameAsJson """{foo:bar}"""
        }.message sameAs """
            Invalid expected JSON: Unexpected JSON token at offset 1: Expected quotation mark '"', but had 'f' instead at path: $
            JSON input: {foo:bar}
        """.trimIndent()
    }

    @Test
    fun `should throw IllegalArgumentException on invalid expected JSON - trailing comma`() {
        assertFailsWith<IllegalArgumentException> {
            """{"foo":"bar"}""" sameAsJson """{"foo":"bar",}"""
        }.message sameAs """
            Invalid expected JSON: Unexpected JSON token at offset 12: Trailing comma before the end of JSON object at path: $
            Trailing commas are non-complaint JSON and not allowed by default. Use 'allowTrailingComma = true' in 'Json {}' builder to support them.
            JSON input: {"foo":"bar",}
        """.trimIndent()
    }

    @Test
    fun `should throw IllegalArgumentException on invalid expected JSON - single quotes`() {
        assertFailsWith<IllegalArgumentException> {
            """{"foo":"bar"}""" sameAsJson """{'foo':'bar'}"""
        }.message sameAs """
            Invalid expected JSON: Unexpected JSON token at offset 1: Expected quotation mark '"', but had ''' instead at path: $
            JSON input: {'foo':'bar'}
        """.trimIndent()
    }

    @Test
    fun `should throw IllegalArgumentException on invalid expected JSON - not JSON at all`() {
        assertFailsWith<IllegalArgumentException> {
            """{"foo":"bar"}""" sameAsJson """this is not JSON"""
        }.message sameAs """
            Invalid expected JSON: Unexpected JSON token at offset 6: Expected EOF after parsing, but had i instead at path: $
            JSON input: this is not JSON
        """.trimIndent()
    }

}
