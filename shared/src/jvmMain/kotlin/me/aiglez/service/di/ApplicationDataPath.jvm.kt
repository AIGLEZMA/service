package me.aiglez.service.di

import java.nio.file.FileAlreadyExistsException
import java.nio.file.Files
import java.nio.file.Path

private const val ApplicationDataDirectoryName = "me.aiglez.service"
private const val DatabaseFileName = "service.db"

internal fun applicationDatabaseFile(): Path {
    val applicationDataDirectory = resolveApplicationDataDirectory(
        osName = System.getProperty("os.name").orEmpty(),
        userHome = System.getProperty("user.home").orEmpty(),
        windowsAppData = System.getenv("APPDATA"),
        xdgDataHome = System.getenv("XDG_DATA_HOME"),
    )
    return prepareApplicationDatabaseFile(
        applicationDataDirectory = applicationDataDirectory,
        legacyDatabaseFile = Path.of(DatabaseFileName).toAbsolutePath(),
    )
}

internal fun resolveApplicationDataDirectory(
    osName: String,
    userHome: String,
    windowsAppData: String?,
    xdgDataHome: String?,
): Path {
    require(userHome.isNotBlank()) { "Unable to determine the user home directory." }

    val normalizedOsName = osName.trim().lowercase()
    val baseDirectory = when {
        normalizedOsName.startsWith("mac") || "darwin" in normalizedOsName -> {
            Path.of(userHome, "Library", "Application Support")
        }
        normalizedOsName.startsWith("windows") -> {
            windowsAppData
                ?.takeIf { it.isNotBlank() }
                ?.let(Path::of)
                ?: Path.of(userHome, "AppData", "Roaming")
        }
        else -> {
            xdgDataHome
                ?.takeIf { it.isNotBlank() }
                ?.let(Path::of)
                ?: Path.of(userHome, ".local", "share")
        }
    }
    return baseDirectory.resolve(ApplicationDataDirectoryName)
}

internal fun prepareApplicationDatabaseFile(
    applicationDataDirectory: Path,
    legacyDatabaseFile: Path,
): Path {
    Files.createDirectories(applicationDataDirectory)
    val databaseFile = applicationDataDirectory.resolve(DatabaseFileName)
    val normalizedLegacyFile = legacyDatabaseFile.toAbsolutePath().normalize()
    val normalizedDatabaseFile = databaseFile.toAbsolutePath().normalize()

    if (
        normalizedLegacyFile != normalizedDatabaseFile &&
        Files.isRegularFile(normalizedLegacyFile) &&
        Files.notExists(normalizedDatabaseFile)
    ) {
        try {
            Files.copy(normalizedLegacyFile, normalizedDatabaseFile)
        } catch (_: FileAlreadyExistsException) {
            // Another application instance completed the migration first.
        }
    }

    return normalizedDatabaseFile
}
