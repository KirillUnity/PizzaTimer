package com.example.clockplannerproject.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.clockplannerproject.kit.core.Task
import com.example.clockplannerproject.kit.core.TaskId
import com.example.clockplannerproject.kit.core.TaskStatus
import com.example.clockplannerproject.kit.core.TimeBlock
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDate
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RoomTaskRepositoryInstrumentedTest {

    private lateinit var database: TimeDialDatabase
    private lateinit var repository: RoomTaskRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, TimeDialDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = RoomTaskRepository(database.taskDao(), Dispatchers.Unconfined)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun upsert_thenObserve_readsFromRoom() = runBlocking {
        val date = LocalDate(2026, 10, 6)
        val task = Task(
            id = TaskId("room-1"),
            title = "Focus",
            colorArgb = 0xFF6750A4,
            blocks = listOf(TimeBlock(540, 720)),
            date = date,
        )
        repository.upsert(task)
        assertEquals(listOf(task), repository.observeTasks(date).first())
    }
}
