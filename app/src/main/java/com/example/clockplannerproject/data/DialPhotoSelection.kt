package com.example.clockplannerproject.data

import com.example.clockplannerproject.kit.core.Appearance
import com.example.clockplannerproject.kit.core.AppearanceRepository
import com.example.clockplannerproject.kit.core.DialPhotoFiles
import com.example.clockplannerproject.kit.core.config.BackgroundConfig
import java.io.InputStream

/**
 * Records a copied dial photo on [AppearanceRepository] and removes the file
 * when the background is no longer a user image.
 *
 * @param files app-files copy of the photo
 * @param appearance DataStore-backed appearance
 * @since 1.0.0
 */
class DialPhotoSelection(
    private val files: DialPhotoFiles,
    private val appearance: AppearanceRepository,
) {
    /**
     * Copies [source] and saves [BackgroundConfig.UserImage] with that path.
     *
     * The picker URI is not stored. [source] is closed.
     *
     * @param current appearance to keep view mode and colors from
     */
    suspend fun choose(current: Appearance, source: InputStream): Appearance {
        val path = source.use { files.import(it) }
        val next = current.copy(background = BackgroundConfig.UserImage(path))
        appearance.save(next)
        return next
    }

    /**
     * Saves [next]. Leaving a user image deletes the copied file.
     *
     * @param current appearance before this save, used only to find the old file
     */
    suspend fun apply(current: Appearance, next: Appearance): Appearance {
        val previous = current.background
        val incoming = next.background
        if (previous is BackgroundConfig.UserImage && incoming !is BackgroundConfig.UserImage) {
            files.delete(previous.uri)
        }
        appearance.save(next)
        return next
    }
}
