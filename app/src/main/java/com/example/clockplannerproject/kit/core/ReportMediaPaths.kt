package com.example.clockplannerproject.kit.core

import java.io.File

/**
 * Relative paths for report files under the app files directory.
 *
 * Names are sanitized so a picker display name cannot escape [DIRECTORY].
 *
 * @since 1.0.0
 */
object ReportMediaPaths {
    /** Folder inside `filesDir`. */
    const val DIRECTORY: String = "report-media"

    /**
     * @param raw picker display name or last path segment
     * @return a single path segment safe to join under [DIRECTORY]
     */
    fun sanitizeFileName(raw: String): String {
        val base = raw.substringAfterLast('/').substringAfterLast('\\').trim()
        val cleaned = base.filter { char ->
            char.isLetterOrDigit() || char == '.' || char == '-' || char == '_'
        }
        return cleaned.ifBlank { "file" }.take(MAX_NAME_LENGTH)
    }

    /**
     * @param reportId [ReportId.value]
     * @param displayName original file name
     * @return `report-media/{id}-{name}`
     */
    fun relativePath(reportId: String, displayName: String): String {
        val id = sanitizeFileName(reportId)
        val name = sanitizeFileName(displayName)
        return "$DIRECTORY/$id-$name"
    }

    /**
     * Resolves [relativePath] and refuses paths that leave [DIRECTORY].
     *
     * @param filesDir application files directory
     */
    fun resolve(filesDir: File, relativePath: String): File {
        val root = File(filesDir, DIRECTORY).canonicalFile
        val target = File(filesDir, relativePath).canonicalFile
        val rootPrefix = root.path + File.separator
        require(target.path == root.path || target.path.startsWith(rootPrefix)) {
            "Attachment path escapes $DIRECTORY"
        }
        return target
    }

    private const val MAX_NAME_LENGTH: Int = 80
}
