package com.example.clockplannerproject.kit.core

import java.io.File
import java.io.InputStream

/**
 * One user photo under the dial, stored beside the app files directory.
 *
 * The folder is not report attachments. A stored path must stay inside
 * [DIRECTORY] so a bad preference cannot delete another file.
 *
 * @since 1.0.0
 */
object DialPhotoPaths {
    /** Folder inside `filesDir`. */
    const val DIRECTORY: String = "dial-background"

    /** Single slot. A new pick overwrites this file. */
    const val FILE_NAME: String = "user-underlay"

    /**
     * @param filesDir [android.content.Context.getFilesDir]
     * @return the absolute copy location under [DIRECTORY]
     */
    fun destination(filesDir: File): File {
        val root = File(filesDir, DIRECTORY).canonicalFile
        val target = File(root, FILE_NAME).canonicalFile
        require(target.parentFile?.canonicalFile == root) {
            "Dial photo path escapes $DIRECTORY"
        }
        return target
    }

    /**
     * Resolves a stored path only when it stays inside [DIRECTORY].
     *
     * @param filesDir application files directory
     * @param storedPath absolute path previously returned by [destination], or empty
     * @return the file to delete, or null when the path is blank or outside the folder
     */
    fun resolveStored(filesDir: File, storedPath: String): File? {
        if (storedPath.isBlank() || storedPath.startsWith("content:")) return null
        val root = File(filesDir, DIRECTORY).canonicalFile
        val target = File(storedPath).canonicalFile
        val prefix = root.path + File.separator
        if (!target.path.startsWith(prefix)) return null
        return target
    }
}

/**
 * Copies and deletes the single dial photo in the app files directory.
 *
 * @since 1.0.0
 */
interface DialPhotoFiles {
    /**
     * Replaces the current copy with [source].
     *
     * @return absolute path of the copy, inside the app files directory
     */
    fun import(source: InputStream): String

    /**
     * Deletes [storedPath] when it is the dial-background copy.
     *
     * Paths outside that folder are ignored.
     */
    fun delete(storedPath: String)
}
