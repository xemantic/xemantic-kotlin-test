# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is **xemantic-kotlin-test**, an AX-first (AI/Agent Experience) Kotlin multiplatform testing library with power-assert compatible assertions DSL. Designed to minimize cognitive load for LLMs—meaning concise, semi-natural language flow that produces clear, diff-based error output when assertions fail. Supports JVM, JS, Native (macOS, iOS, Linux, Windows), and WASM.

## Core Architecture

### Source Structure
- `src/commonMain/kotlin/` - Core multiplatform code:
  - `Assertions.kt` - Power-assert DSL: `assert`, `should`, `be`, `have`
  - `SameAs.kt` - String comparison with unified diff output (Myers' algorithm)
  - `SameAsJson.kt` - JSON comparison with automatic prettification
  - `TestContext.kt` - Cross-platform environment access (`getEnv`, `gradleRootDir`, `isBrowserPlatform`)
  - `coroutines/SuspendShould.kt` - Suspend version of `should` for coroutine tests
  - `text/StringFlows.kt` - Flow utilities for testing streaming text
- Platform-specific `expect` implementations in `src/{jvm,js,native,wasmJs,wasmWasi}Main/kotlin/`

### Key Assertion Functions

| Function | Purpose |
|----------|---------|
| `assert(condition)` | Power-assert enabled boolean assertion |
| `obj should { ... }` | Scoped assertions on an object with null check |
| `be<Type>()` | Type assertion with smart cast within `should` block |
| `have(condition)` | Power-assert enabled condition within `should` block |
| `actual sameAs expected` | String equality with unified diff on failure |
| `actual sameAsJson expected` | JSON comparison (prettifies actual, diff on failure) |

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
./gradlew wasmJsTest               # Run WASM JS tests
./gradlew macosArm64Test           # Run native tests on macOS ARM64 (use macosX64Test for Intel)
```

### Running a Single Test
```bash
./gradlew jvmTest --tests "com.xemantic.kotlin.test.SameAsTest"           # Run specific test class
./gradlew jvmTest --tests "com.xemantic.kotlin.test.SameAsTest.test name" # Run specific test method
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

### Maintenance
```bash
./gradlew dependencyUpdates        # Check for available dependency updates
```

## Power-Assert Configuration

The power-assert plugin is configured for `com.xemantic.kotlin.test.assert` and `com.xemantic.kotlin.test.have`. When adding new assertion functions that should benefit from power-assert's expression breakdown, add them to the `powerAssert.functions` list in `build.gradle.kts`.

## Platform-Specific Notes

- **Environment variables**: Configured in `build.gradle.kts` for JVM/JS/Native; browser tests use `webpack.config.d/env-config.js`
- **Native emulators**: Use `SIMCTL_CHILD_` prefix for environment variables
- **Disabled tests**: `tvosSimulatorArm64Test`, `watchosSimulatorArm64Test` (require Xcode), `wasmWasiNodeTest` (no env var support), `wasmJsBrowserTest` (stale issues)

## Key Dependencies

- `kotlin-test` - Base testing functionality
- `kotlinx-serialization-json` - JSON parsing for `sameAsJson`
- `kotlinx-coroutines-core` - Coroutine support for suspend assertions
- Power-assert plugin - Enhanced assertion messages with expression breakdown
- Binary compatibility validator - API stability checks