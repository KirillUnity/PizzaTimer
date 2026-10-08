package com.example.clockplannerproject.di

import com.example.clockplannerproject.data.RoomReportRepository
import com.example.clockplannerproject.data.RoomTaskRepository
import com.example.clockplannerproject.data.SampleDaySeeder
import com.example.clockplannerproject.data.SystemTimeProvider
import com.example.clockplannerproject.data.TimeDialDatabase
import com.example.clockplannerproject.kit.core.ReportRepository
import com.example.clockplannerproject.kit.core.TaskRepository
import com.example.clockplannerproject.kit.core.TimeProvider
import com.example.clockplannerproject.ui.day.DayViewModel
import com.example.clockplannerproject.ui.stats.ReportsViewModel
import com.example.clockplannerproject.ui.task.TasksViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val appModule = module {
    single { TimeDialDatabase.create(androidContext()) }
    single { get<TimeDialDatabase>().taskDao() }
    single { get<TimeDialDatabase>().reportDao() }
    single<TaskRepository> { RoomTaskRepository(get()) }
    single<ReportRepository> { RoomReportRepository(get()) }
    single<TimeProvider> { SystemTimeProvider() }
    single { SampleDaySeeder(get(), get()) }
    viewModelOf(::DayViewModel)
    viewModelOf(::TasksViewModel)
    viewModelOf(::ReportsViewModel)
}
