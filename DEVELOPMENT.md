# Development

Maintenance notes for working on this project itself.
From time to time, it is worth to update the build tooling and dependencies.

## Build the project

Clone this project, and then run:

```shell
./gradlew build
```

## Update gradlew wrapper

```shell
./gradlew wrapper --gradle-version latest --distribution-type bin
```

## Update all the dependencies to the latest versions

All the gradle dependencies are managed by the
[libs.versions.toml](gradle/libs.versions.toml) file in the `gradle` dir.

To resolve the latest versions,
and apply them automatically to [libs.versions.toml](gradle/libs.versions.toml),
run the [version-catalog-update](https://github.com/littlerobots/version-catalog-update-plugin) plugin:

```shell
./gradlew versionCatalogUpdate
```

To review and pick the updates one by one instead of applying them all,
use the interactive mode:

```shell
./gradlew versionCatalogUpdate --interactive
```

then apply the staged changes with:

```shell
./gradlew versionCatalogApplyUpdates
```

> [!NOTE]
> The plugin is configured in [build.gradle.kts](build.gradle.kts)
> to preserve the manual ordering of `libs.versions.toml` (`sortByKey = false`),
> and to keep the `kotlinTarget`, `javaTarget`, and `asm` version constants,
> which have no `version.ref` and would otherwise be removed as unused.

## Update the API dump

This project is a published library,
so the [binary-compatibility-validator](https://github.com/Kotlin/binary-compatibility-validator)
plugin guards its public API.
After an intentional API change, or after adding or removing a Kotlin target,
regenerate the dumps in [api](api):

```shell
./gradlew apiDump
```

## Documentation conventions

All the Markdown files in this project are authored with
[semantic line breaks](https://sembr.org/).
Each sentence starts on its own line,
and long sentences may be split further at clause boundaries.
This keeps `git diff` and code review focused on the sentence that actually changed,
instead of on a whole reflowed paragraph.

There is no maximum line length,
and paragraphs are never hard-wrapped to a fixed column.
Line length is a rendering concern,
so it is left to the editor.

### Markdown soft wrapping in the IDE

**IntelliJ IDEA**:
`Settings` → `Editor` → `General` → `Soft Wraps`,
enable `Soft-wrap these files` and make sure the mask contains `*.md`
(the default mask already does).
To toggle it for the file at hand only,
use `View` → `Active Editor` → `Soft-Wrap`.

**VS Code**:
add the following to your `settings.json`:

```json
{
  "[markdown]": {
    "editor.wordWrap": "on"
  }
}
```

Alternatively toggle it for the current file with `Alt`+`Z` (`Option`+`Z` on macOS).

## Documentation snippets

The assertion failure messages quoted in [README.md](README.md) are produced by power-assert,
the Kotlin compiler plugin,
so their exact rendering can change between Kotlin releases,
while the XML wrapping around them comes from the
[xemantic-conventions](https://github.com/xemantic/xemantic-conventions) gradle plugin.

`ProjectDocumentationTest` is the fixture producing these messages.
Both of its test cases are meant to fail,
so it is excluded from every test task by a filter in [build.gradle.kts](build.gradle.kts).
This keeps the build green,
while the fixture itself stays free of an `@Ignore`,
because the README quotes the class as-is.

Setting the `documentationSnippets` property inverts the filter,
making the fixture the only test which runs:

```shell
./gradlew jvmTest wasmJsNodeTest -PdocumentationSnippets --console=plain --rerun-tasks --continue
```

Then copy the reported failures into the README.
The build is expected to fail,
which is why `--continue` is needed,
so that the failing `jvmTest` does not prevent `wasmJsNodeTest` from running,
and `--rerun-tasks` is needed,
so that up-to-date test results are not reused.
The stack traces quote line numbers of `ProjectDocumentationTest.kt`,
so the snippets go stale whenever that file shifts.
