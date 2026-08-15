package me.aiglez.service.di

import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.createTempDirectory
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ApplicationDataPathTest {

    @Test
    fun `uses the macOS application support directory`() {
        val directory = resolveApplicationDataDirectory(
            osName = "Mac OS X",
            userHome = "/Users/tester",
            windowsAppData = null,
            xdgDataHome = null,
        )

        assertEquals(
            Path.of("/Users/tester/Library/Application Support/me.aiglez.service"),
            directory,
        )
    }

    @Test
    fun `uses APPDATA on Windows`() {
        val directory = resolveApplicationDataDirectory(
            osName = "Windows 11",
            userHome = "/Users/tester",
            windowsAppData = "/profiles/tester/AppData/Roaming",
            xdgDataHome = null,
        )

        assertEquals(
            Path.of("/profiles/tester/AppData/Roaming/me.aiglez.service"),
            directory,
        )
    }

    @Test
    fun `uses XDG data home on Linux`() {
        val directory = resolveApplicationDataDirectory(
            osName = "Linux",
            userHome = "/home/tester",
            windowsAppData = null,
            xdgDataHome = "/data/tester",
        )

        assertEquals(Path.of("/data/tester/me.aiglez.service"), directory)
    }

    @Test
    fun `falls back to the standard Linux user data directory`() {
        val directory = resolveApplicationDataDirectory(
            osName = "Linux",
            userHome = "/home/tester",
            windowsAppData = null,
            xdgDataHome = null,
        )

        assertEquals(Path.of("/home/tester/.local/share/me.aiglez.service"), directory)
    }

    @Test
    fun `copies a legacy database without deleting it`() {
        withTemporaryDirectory { temporaryDirectory ->
            val legacyDatabase = temporaryDirectory.resolve("legacy/service.db")
            Files.createDirectories(legacyDatabase.parent)
            Files.write(legacyDatabase, byteArrayOf(1, 2, 3, 4))

            val databaseFile = prepareApplicationDatabaseFile(
                applicationDataDirectory = temporaryDirectory.resolve("application-data"),
                legacyDatabaseFile = legacyDatabase,
            )

            assertTrue(Files.isRegularFile(legacyDatabase))
            assertTrue(Files.isRegularFile(databaseFile))
            assertContentEquals(Files.readAllBytes(legacyDatabase), Files.readAllBytes(databaseFile))
        }
    }

    @Test
    fun `does not overwrite an existing application database`() {
        withTemporaryDirectory { temporaryDirectory ->
            val applicationDataDirectory = temporaryDirectory.resolve("application-data")
            val databaseFile = applicationDataDirectory.resolve("service.db")
            val legacyDatabase = temporaryDirectory.resolve("legacy.db")
            Files.createDirectories(applicationDataDirectory)
            Files.write(databaseFile, byteArrayOf(9, 8, 7))
            Files.write(legacyDatabase, byteArrayOf(1, 2, 3))

            val result = prepareApplicationDatabaseFile(applicationDataDirectory, legacyDatabase)

            assertEquals(databaseFile.toAbsolutePath().normalize(), result)
            assertContentEquals(byteArrayOf(9, 8, 7), Files.readAllBytes(result))
            assertFalse(Files.isSameFile(legacyDatabase, result))
        }
    }

    private fun withTemporaryDirectory(block: (Path) -> Unit) {
        val directory = createTempDirectory("service-application-data-test")
        try {
            block(directory)
        } finally {
            directory.toFile().deleteRecursively()
        }
    }
}
