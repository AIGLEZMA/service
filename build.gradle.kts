import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.TaskAction
import java.nio.file.Files
import java.nio.file.Path

plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinJvm) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.koin) apply false
    alias(libs.plugins.kotlinxSerialization) apply false
}

abstract class KotlinSourceStyleTask : DefaultTask() {
    @get:InputDirectory
    abstract val sourceRoot: DirectoryProperty

    protected fun kotlinSourceFiles(): List<Path> {
        val rootPath = sourceRoot.get().asFile.toPath()
        return Files.walk(rootPath).use { paths ->
            paths
                .filter(Files::isRegularFile)
                .filter { path -> path.fileName.toString().let { it.endsWith(".kt") || it.endsWith(".kts") } }
                .filter { path -> !relativePath(rootPath, path).isExcludedSourcePath() }
                .sorted()
                .toList()
        }
    }

    protected fun relativePath(rootPath: Path, path: Path): String {
        return rootPath.relativize(path).toString().replace('\\', '/')
    }

    private fun String.isExcludedSourcePath(): Boolean {
        return startsWith(".gradle/") ||
            startsWith(".idea/") ||
            contains("/build/") ||
            startsWith("build/")
    }
}

abstract class FormatKotlinTask : KotlinSourceStyleTask() {
    @TaskAction
    fun format() {
        kotlinSourceFiles().forEach { path ->
            val original = Files.readString(path)
            val formatted = original
                .lineSequence()
                .joinToString(separator = "\n") { line -> line.trimEnd() }
                .let { text -> if (text.isBlank()) "" else "$text\n" }

            if (formatted != original) {
                Files.writeString(path, formatted)
            }
        }
    }
}

abstract class LintKotlinTask : KotlinSourceStyleTask() {
    @get:Input
    abstract val maxSourceLines: Property<Int>

    @TaskAction
    fun lint() {
        val rootPath = sourceRoot.get().asFile.toPath()
        val violations = mutableListOf<String>()

        kotlinSourceFiles().forEach { path ->
            val relativePath = relativePath(rootPath, path)
            val text = Files.readString(path)
            val lines = text.lineSequence().toList()

            if (text.isNotEmpty() && !text.endsWith("\n")) {
                violations += "$relativePath: missing trailing newline"
            }

            lines.forEachIndexed { index, line ->
                if (line.endsWith(" ") || line.endsWith("\t")) {
                    violations += "$relativePath:${index + 1}: trailing whitespace"
                }
            }

            if (lines.size > maxSourceLines.get()) {
                violations += "$relativePath: ${lines.size} lines exceeds ${maxSourceLines.get()} line limit"
            }
        }

        if (violations.isNotEmpty()) {
            throw GradleException(
                buildString {
                    appendLine("Kotlin lint failed:")
                    violations.forEach { appendLine(" - $it") }
                    append("Run ./gradlew formatKotlin to fix whitespace issues.")
                },
            )
        }
    }
}

val maxKotlinSourceLines = 800

val formatKotlin by tasks.registering(FormatKotlinTask::class) {
    group = "formatting"
    description = "Formats Kotlin sources with repository-local whitespace rules."
    sourceRoot.set(layout.projectDirectory)
}

val lintKotlin by tasks.registering(LintKotlinTask::class) {
    group = "verification"
    description = "Checks Kotlin source formatting and file-size guardrails."
    sourceRoot.set(layout.projectDirectory)
    maxSourceLines.set(maxKotlinSourceLines)
}

tasks.register("check") {
    group = "verification"
    description = "Runs repository verification checks."
    dependsOn(lintKotlin)
}

subprojects {
    tasks.matching { it.name == "check" }.configureEach {
        dependsOn(rootProject.tasks.named("lintKotlin"))
    }
}


