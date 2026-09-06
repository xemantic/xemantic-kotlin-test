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

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.test.assertNotNull

/**
 * Asserts that this object is not `null` and runs the suspending [block]
 * of assertions on it.
 *
 * Returns the asserted object, so that it can be used further, e.g.:
 *
 * ```kotlin
 * val session = createSession() should {
 *     have(id.isNotBlank())
 * }
 * ```
 *
 * Note: the returned value has the type of the receiver, therefore a type
 * narrowing done with [com.xemantic.kotlin.test.be] inside the [block] will
 * not be reflected in the type of the returned value.
 *
 * @param block the assertions to run on this object.
 * @return this object, guaranteed to be non-`null`.
 * @throws AssertionError if this object is `null`, or if any assertion in the [block] fails.
 */
@OptIn(ExperimentalContracts::class)
public suspend infix fun <T> T?.should(
    block: suspend T.() -> Unit
): T & Any {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
    assertNotNull(this)
    block()
    return this
}
