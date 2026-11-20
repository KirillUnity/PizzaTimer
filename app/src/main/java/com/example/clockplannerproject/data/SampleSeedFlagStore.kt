package com.example.clockplannerproject.data

/**
 * Remembers that this install already handled the debug sample day.
 *
 * The flag lives outside Room so an empty task database can stay empty
 * after the one-time insert. Clearing application data clears the flag.
 *
 * @since 0.6.0
 */
interface SampleSeedFlagStore {
    /**
     * @return true after this install inserted the sample or skipped it
     * because tasks already existed
     */
    suspend fun isHandled(): Boolean

    /**
     * Records that this install has handled the debug sample.
     */
    suspend fun markHandled()
}
