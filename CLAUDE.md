# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is **xemantic-kotlin-test**, a Kotlin multiplatform testing library that provides power-assert compatible assertions DSL and testing utilities. The library supports all major Kotlin platforms including JVM, JS, Native (macOS, iOS, Linux, Windows), and WASM.

## Core Architecture

### Source Structure
- `src/commonMain/kotlin/` - Core multiplatform code
  - `Assertions.kt` - Main assertion functions (`assert`, `should`, `be`, `have`)
  - `TestContext.kt` - Cross-platform test context utilities
- `src/commonTest/kotlin/` - Common tests
- Platform-specific implementations in:
  - `src/jvmMain/kotlin/`
  - `src/jsMain/kotlin/`
  - `src/nativeMain/kotlin/`
  - `src/wasmJsMain/kotlin/`
  - `src/wasmWasiMain/kotlin/`

### Key Components

**Assertions Framework**: Built around power-assert plugin with custom DSL:
- `assert()` - Multiplatform assert function with power-assert support
- `should {}` - Infix function for chaining assertions on objects
- `be<T>()` - Type assertion with smart casting
- `have()` - Condition assertion function

**Test Context**: Cross-platform utilities for:
- Environment variable access (`getEnv()`)
- Gradle root directory access (`gradleRootDir`)
- Platform detection (`isBrowserPlatform`)

## Commands

### Build Commands
```bash
./gradlew build                    # Full build for all platforms
./gradlew assemble                 # Assemble artifacts without tests
./gradlew clean                    # Clean build directory
```

### Testing Commands
```bash
./gradlew allTests                 # Run tests on all platforms with aggregated report
./gradlew check                    # Run all verification tasks including tests and API checks
./gradlew jvmTest                  # Run JVM tests only
./gradlew jsTest                   # Run JS tests (browser + Node.js)
./gradlew macosX64Test             # Run native tests on macOS x64
./gradlew linuxX64Test             # Run native tests on Linux x64
```

### API Compatibility
```bash
./gradlew apiCheck                 # Check API compatibility
./gradlew jvmApiCheck              # Check JVM API compatibility specifically
./gradlew klibApiCheck             # Check KLib API compatibility
```

### Documentation
```bash
./gradlew dokkaGeneratePublicationHtml  # Generate API documentation
```

### Publishing
```bash
./gradlew publishToMavenLocal      # Publish to local Maven repository
./gradlew publish                  # Publish all publications
```

## Power-Assert Configuration

The project uses Kotlin's power-assert plugin configured for these functions:
- `com.xemantic.kotlin.test.assert`
- `com.xemantic.kotlin.test.have`

When working with assertions, ensure power-assert is properly configured in `build.gradle.kts`.

## Platform-Specific Notes

### Test Configuration
- **JVM/JS/Native**: Environment variables are configured in `build.gradle.kts`
- **Browser platforms**: Environment variables passed via webpack config in `webpack.config.d/env-config.js`
- **Native emulators**: Uses `SIMCTL_CHILD_` prefix for environment variables

### Disabled Tests
Some platform tests are disabled in the build:
- `tvosSimulatorArm64Test` and `watchosSimulatorArm64Test` (require Xcode)
- `wasmWasiNodeTest` (environment variable retrieval not implemented)
- `wasmJsBrowserTest` (stale test issues)

## Development Workflow

1. Make changes to core assertions in `src/commonMain/kotlin/Assertions.kt`
2. Add platform-specific implementations if needed
3. Write tests in `src/commonTest/kotlin/`
4. Run `./gradlew allTests` to verify all platforms
5. Run `./gradlew apiCheck` to verify API compatibility
6. Use `./gradlew build` for full verification before commits

## Key Dependencies

- `kotlin-test` for base testing functionality
- Power-assert plugin for enhanced assertion messages
- Binary compatibility validator for API stability
- Dokka for documentation generation