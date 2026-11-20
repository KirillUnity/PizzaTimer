package com.example.clockplannerproject.data

import com.example.clockplannerproject.kit.core.DialPhotoFiles
import com.example.clockplannerproject.kit.core.DialPhotoPaths
import java.io.File
import java.io.InputStream

/**
 * Writes the dial photo into `filesDir/dial-background`, not report-media.
 *
 * @param filesDir [android.content.Context.getFilesDir]
 * @since 1.0.0
 */
class FilesDirDialPhotoStore(
    private val filesDir: File,
) : DialPhotoFiles {

    override fun import(source: InputStream): String {
        val dest = DialPhotoPaths.destination(filesDir)
        dest.parentFile?.mkdirs()
        dest.outputStream().use { output -> source.copyTo(output) }
        return dest.canonicalPath
    }

    override fun delete(storedPath: String) {
        DialPhotoPaths.resolveStored(filesDir, storedPath)?.delete()
    }
}
