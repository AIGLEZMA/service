import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
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
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val sourceFiles: ConfigurableFileCollection

    @get:Input
    abstract val sourceRootPath: Property<String>

    protected fun kotlinSourceFiles(): List<Path> {
        return sourceFiles.files
            .map { it.toPath() }
            .sorted()
    }

    protected fun relativePath(path: Path): String {
        val rootPath = Path.of(sourceRootPath.get())
        return rootPath.relativize(path).toString().replace('\\', '/')
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
        val violations = mutableListOf<String>()

        kotlinSourceFiles().forEach { path ->
            val relativePath = relativePath(path)
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
val kotlinStyleSourceFiles = files(
    fileTree(layout.projectDirectory) {
        include("**/*.kt", "**/*.kts")
        exclude("**/build/**", ".gradle/**", ".idea/**")
    },
)

val formatKotlin by tasks.registering(FormatKotlinTask::class) {
    group = "formatting"
    description = "Formats Kotlin sources with repository-local whitespace rules."
    sourceFiles.from(kotlinStyleSourceFiles)
    sourceRootPath.set(layout.projectDirectory.asFile.absolutePath)
}

val lintKotlin by tasks.registering(LintKotlinTask::class) {
    group = "verification"
    description = "Checks Kotlin source formatting and file-size guardrails."
    sourceFiles.from(kotlinStyleSourceFiles)
    sourceRootPath.set(layout.projectDirectory.asFile.absolutePath)
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
