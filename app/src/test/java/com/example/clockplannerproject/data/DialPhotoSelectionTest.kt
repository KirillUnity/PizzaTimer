package com.example.clockplannerproject.data

import com.example.clockplannerproject.kit.core.Appearance
import com.example.clockplannerproject.kit.core.config.BackgroundConfig
import com.example.clockplannerproject.kit.core.config.BackgroundPresets
import java.io.ByteArrayInputStream
import java.io.File
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DialPhotoSelectionTest {

    @Test
    fun choose_storesPathInsideAppFiles_andResetClearsUserImage() = runTest {
        val filesDir = File("build/dial-photo-test").resolve(System.nanoTime().toString())
        filesDir.mkdirs()
        try {
            val files = FilesDirDialPhotoStore(filesDir)
            val repo = InMemoryAppearanceRepository()
            val selection = DialPhotoSelection(files, repo)

            selection.choose(Appearance.Default, ByteArrayInputStream(byteArrayOf(1, 2, 3, 4)))
            val stored = repo.observeAppearance().first().background as BackgroundConfig.UserImage
            val copy = File(stored.uri).canonicalFile
            val root = filesDir.canonicalFile
            assertTrue(copy.path.startsWith(root.path + File.separator))
            assertTrue(copy.path.replace('\\', '/').contains("dial-background/user-underlay"))
            assertFalse(stored.uri.replace('\\', '/').contains("report-media"))
            assertFalse(stored.uri.startsWith("content:"))
            assertEquals(listOf<Byte>(1, 2, 3, 4), copy.readBytes().toList())

            val chosen = repo.observeAppearance().first()
            selection.choose(chosen, ByteArrayInputStream(byteArrayOf(9)))
            val replaced = repo.observeAppearance().first().background as BackgroundConfig.UserImage
            assertEquals(listOf<Byte>(9), File(replaced.uri).readBytes().toList())

            val outside = File(filesDir, "secret.txt").apply { writeText("keep") }
            files.delete(outside.canonicalPath)
            assertTrue(outside.exists())

            selection.apply(
                chosen.copy(background = replaced),
                chosen.copy(background = BackgroundConfig.None),
            )
            assertEquals(BackgroundConfig.None, repo.observeAppearance().first().background)
            assertFalse(File(replaced.uri).exists())

            val again = selection.choose(
                Appearance.Default,
                ByteArrayInputStream(byteArrayOf(7)),
            )
            selection.apply(
                again,
                again.copy(background = BackgroundConfig.Preset(BackgroundPresets.PARCHMENT)),
            )
            val afterPreset = repo.observeAppearance().first().background
            assertTrue(afterPreset is BackgroundConfig.Preset)
            assertFalse(File((again.background as BackgroundConfig.UserImage).uri).exists())
        } finally {
            filesDir.deleteRecursively()
        }
    }
}
