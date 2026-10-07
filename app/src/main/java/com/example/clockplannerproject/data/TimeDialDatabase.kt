package com.example.clockplannerproject.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Local TimeDial database.
 *
 * Schema v3:
 * `tasks(..., recurrenceKind TEXT, weekdaysMask INTEGER, seriesId TEXT)`
 * `time_blocks(..., endMinute INTEGER nullable for open timer)`
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
    version = 3,
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
