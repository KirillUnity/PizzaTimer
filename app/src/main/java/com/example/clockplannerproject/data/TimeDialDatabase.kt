package com.example.clockplannerproject.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Local TimeDial database.
 *
 * Schema v2:
 * `tasks(id TEXT PK, title TEXT, description TEXT, colorArgb INTEGER,
 * status TEXT, dateIso TEXT, importance TEXT, tagsCsv TEXT)`
 * `time_blocks(id TEXT PK, taskId TEXT FK, startMinute INTEGER, endMinute INTEGER, sortIndex INTEGER)`
 *
 * **No Room `Migration` class.** Version bumps use
 * [fallbackToDestructiveMigration] only. Installing an app update that
 * changes [version] **wipes local data** — uninstall/reinstall or clear
 * app storage. Do not add a `Migration` until a later product decision.
 *
 * @since 0.1.0
 */
@Database(
    entities = [TaskEntity::class, TimeBlockEntity::class],
    version = 2,
    exportSchema = false,
)
abstract class TimeDialDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao

    companion object {
        fun create(context: Context): TimeDialDatabase =
            Room.databaseBuilder(
                context.applicationContext,
                TimeDialDatabase::class.java,
                "timedial.db",
            )
                .fallbackToDestructiveMigration(dropAllTables = true)
                .build()
    }
}
