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

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.reflect.typeOf
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@OptIn(ExperimentalContracts::class)
public infix fun <T> T?.should(block: T.() -> Unit) {
  contract {
    callsInPlace(block, InvocationKind.EXACTLY_ONCE)
  }
  assertNotNull(this)
  try {
    block()
  } catch (e : AssertionError) {
    throw ShouldAssertionError(
      message = if (e is ShouldAssertionError) {
        "$this\n containing:\n${e.message}"
      } else {
        "$this\n should:${e.message}"
      },
      cause = e
    )
  }
}

@OptIn(ExperimentalContracts::class)
public inline fun <reified T> Any?.be() {
  contract {
    returns() implies (this@be is T)
  }
  if (this !is T) {
    throw AssertionError(
      " be of type <${typeOf<T>()}>, actual <${this!!::class}>"
    )
  }
}

public fun have(
  condition: Boolean,
  message: String? = null
) {
  assertTrue(condition, message)
}

public class ShouldAssertionError(
  message: String,
  cause: AssertionError
) : AssertionError(message, cause)
