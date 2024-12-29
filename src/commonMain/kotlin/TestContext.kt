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

/**
 * Returns the value of the specified environment variable.
 *
 * Note: on platforms which does not support access to system environment variables,
 * they can be still set with gradle test configuration.
 * See [README.md](https://github.com/xemantic/xemantic-kotlin-test) for details.
 *
 * @param name the name of the environment variable to read.
 */
public expect fun getEnv(name: String): String?

/**
 * Returns true, if we are on the browser platform.
 *
 * This flag might be used to skip certain tests, e.g. file based tests
 * on platforms which does not offer access to the filesystem.
 */
public expect val isBrowserPlatform: Boolean

/**
 * The root dir of the gradle project.
 *
 * It is provided as an absolute file path. The specificity of emulators
 * is taken into account.
 *
 * Note: the gradle root dir can be only resolved with additional gradle
 * settings.
 * See [README.md](https://github.com/xemantic/xemantic-kotlin-test) for details.
 */
public val gradleRootDir: String get() = getEnv("GRADLE_ROOT_DIR")!!
