package com.example.clockplannerproject.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Local TimeDial database.
 *
 * Schema v7:
 * `tasks(..., project, tags)`, `time_blocks(...)`,
 * `task_reports(..., text, attachmentRelativePath, attachmentDisplayName, attachmentMimeType)`.
 * [tags] is user-created labels, not a fixed catalog.
 *
 * **No Room `Migration` class.** Version bumps use
 * [fallbackToDestructiveMigration] only. Installing an app update that
 * changes [version] **wipes local data** — uninstall/reinstall or clear
 * app storage. Do not add a `Migration` until a later product decision.
 *
 * @since 0.1.0
 */
@Database(
    entities = [TaskEntity::class, TimeBlockEntity::class, TaskReportEntity::class],
    version = 7,
    exportSchema = false,
)
abstract class TimeDialDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun reportDao(): ReportDao

    companion object {
        fun create(context: Context): TimeDialDatabase =
            Room.databaseBuilder(
                context.applicationContext,
                TimeDialDatabase::class.java,
                "timedial.db",
            )
                .fallbackToDestructiveMigration()
                .build()
    }
}
