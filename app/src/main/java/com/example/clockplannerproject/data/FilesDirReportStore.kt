package com.example.clockplannerproject.data

import com.example.clockplannerproject.kit.core.ReportAttachment
import com.example.clockplannerproject.kit.core.ReportFileStore
import com.example.clockplannerproject.kit.core.ReportMediaPaths
import java.io.File
import java.io.InputStream

/**
 * Copies report bytes into [filesDir] and deletes them by relative path.
 *
 * @param filesDir [android.content.Context.getFilesDir]
 * @since 1.0.0
 */
class FilesDirReportStore(
    private val filesDir: File,
) : ReportFileStore {

    /**
     * @param input stream from a content resolver; closed by the caller
     * @param reportId report the file belongs to
     * @param displayName picker name
     * @param mimeType MIME type, or a generic type when unknown
     */
    fun import(
        input: InputStream,
        reportId: String,
        displayName: String,
        mimeType: String,
    ): ReportAttachment {
        val relative = ReportMediaPaths.relativePath(reportId, displayName)
        val dest = ReportMediaPaths.resolve(filesDir, relative)
        dest.parentFile?.mkdirs()
        dest.outputStream().use { output -> input.copyTo(output) }
        return ReportAttachment(
            relativePath = relative,
            displayName = displayName.ifBlank { dest.name },
            mimeType = mimeType.ifBlank { GENERIC_MIME },
        )
    }

    override fun delete(relativePath: String) {
        ReportMediaPaths.resolve(filesDir, relativePath).delete()
    }

    private companion object {
        const val GENERIC_MIME: String = "application/octet-stream"
    }
}
