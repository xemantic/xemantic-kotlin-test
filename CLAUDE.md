# CLAUDE.md

This file captures only what cannot be inferred from the codebase itself.

## Rules for editing this file

Both developers and AI agents are expected to add entries as they encounter surprises.

- **Add an entry** when you encounter something unexpected: a build quirk, a non-obvious constraint, a dependency gotcha, or any behavior that would surprise the next agent or developer.
- **Add an entry** when a developer flags an anti-pattern produced by AI — describe the anti-pattern and the preferred alternative.
- **Do not** add codebase overviews, directory listings, or anything discoverable by reading the source.
- Keep entries concise: one line per lesson, grouped under a heading if a theme emerges.

## Conventions

### Markdown authoring

Markdown files use [semantic line breaks](https://sembr.org/):
break a line after a sentence,
and optionally at clause boundaries within a long sentence,
so that diffs stay meaningful and reviewable.

There is no column width limit —
never reflow or hard-wrap a paragraph to fit some character count.
Modern editors soft-wrap Markdown visually,
see [DEVELOPMENT.md](DEVELOPMENT.md#markdown-soft-wrapping-in-the-ide) for how to enable it.

## Known gotchas

- After upgrading the Gradle wrapper, `jvmTest` may fail with `NoSuchFileException: build/test-results/jvmTest/binary/in-progress-results-generic.bin`, because the results of the previous Gradle version are stale — delete `build/test-results` (or run `clean`) and retry.
- The assertion failure messages asserted in the tests, and quoted in the README, are rendered by the power-assert compiler plugin, so a Kotlin upgrade can change them — Kotlin 2.4 started quoting string values and packing non-colliding values onto one line.
- In the Gradle Kotlin DSL, `AbstractTestTask` has no `filter { }` block — the name resolves to the `ContentFilterable.filter` extensions and fails to compile; configure test filtering through the property instead: `filter.excludeTestsMatching(...)`.
- `useJUnitPlatform()` is applied explicitly to `KotlinJvmTest` — without it `kotlin-test` resolves to its JUnit 4 variant, and the JVM stack traces quoted in the README stop matching the ones the build produces.

## Testing conventions

- Tests use `// given`, `// when`, `// then` comments to structure test cases. Use existing test cases as a reference.

## Anti-patterns to avoid

- Do not add content to this file that is already discoverable by reading the source or build scripts — that inflates context without adding signal, reducing AI agent task success rates (see [arxiv 2602.11988](https://arxiv.org/abs/2602.11988)).
