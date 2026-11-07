package com.example.clockplannerproject.data

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.example.clockplannerproject.kit.core.ReportAttachment
import com.example.clockplannerproject.kit.core.ReportAttachmentImporter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Reads a content URI and copies the bytes into [FilesDirReportStore].
 *
 * Uses the system picker URI only as a source. The stored value is a path
 * under the app files directory, so the attachment still opens after process
 * death. No storage permission is requested.
 *
 * @since 1.0.0
 */
class AndroidReportImporter(
    private val context: Context,
    private val store: FilesDirReportStore,
) : ReportAttachmentImporter {

    override suspend fun import(sourceUri: String, reportId: String): ReportAttachment =
        withContext(Dispatchers.IO) {
            val uri = Uri.parse(sourceUri)
            val mime = context.contentResolver.getType(uri) ?: GENERIC_MIME
            val name = displayName(uri)
            context.contentResolver.openInputStream(uri).use { stream ->
                requireNotNull(stream) { "Could not read attachment" }
                store.import(stream, reportId, name, mime)
            }
        }

    private fun displayName(uri: Uri): String {
        context.contentResolver.query(
            uri,
            arrayOf(OpenableColumns.DISPLAY_NAME),
            null,
            null,
            null,
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index >= 0) {
                    val name = cursor.getString(index)
                    if (!name.isNullOrBlank()) return name
                }
            }
        }
        return uri.lastPathSegment ?: "file"
    }

    private companion object {
        const val GENERIC_MIME: String = "application/octet-stream"
    }
}
