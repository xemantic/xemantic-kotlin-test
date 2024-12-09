val groupId = "com.xemantic.kotlin"
val name = "xemantic-kotlin-test"

rootProject.name = name
gradle.beforeProject {
  group = groupId
}
