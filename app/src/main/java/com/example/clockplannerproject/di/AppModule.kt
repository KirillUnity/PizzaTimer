package com.example.clockplannerproject.di

import com.example.clockplannerproject.data.AndroidReportImporter
import com.example.clockplannerproject.data.FilesDirReportStore
import com.example.clockplannerproject.data.RoomReportRepository
import com.example.clockplannerproject.data.RoomTaskRepository
import com.example.clockplannerproject.data.SampleDaySeeder
import com.example.clockplannerproject.data.SystemTimeProvider
import com.example.clockplannerproject.data.TimeDialDatabase
import com.example.clockplannerproject.data.AndroidPlannerPhrases
import com.example.clockplannerproject.data.DataStoreAppearanceRepository
import com.example.clockplannerproject.data.DataStoreFlowMemoryStore
import com.example.clockplannerproject.data.DataStorePlannerPlanStore
import com.example.clockplannerproject.data.DataStorePracticeProgressStore
import com.example.clockplannerproject.data.DataStoreSampleSeedFlagStore
import com.example.clockplannerproject.data.DialPhotoSelection
import com.example.clockplannerproject.data.FilesDirDialPhotoStore
import com.example.clockplannerproject.data.MockPlannerSource
import com.example.clockplannerproject.data.SampleSeedFlagStore
import com.example.clockplannerproject.kit.core.DialPhotoFiles
import com.example.clockplannerproject.kit.core.AppearanceRepository
import com.example.clockplannerproject.kit.core.FlowMemoryStore
import com.example.clockplannerproject.kit.core.practice.PracticeProgressStore
import com.example.clockplannerproject.kit.core.TaskRepository
import com.example.clockplannerproject.kit.core.TimeProvider
import com.example.clockplannerproject.kit.core.ReportRepository
import com.example.clockplannerproject.kit.core.planner.PlannerPhrases
import com.example.clockplannerproject.kit.core.planner.PlannerPlanStore
import com.example.clockplannerproject.kit.core.planner.PlannerSource
import com.example.clockplannerproject.ui.day.DayViewModel
import com.example.clockplannerproject.ui.settings.GoalsViewModel
import com.example.clockplannerproject.ui.settings.SettingsViewModel
import com.example.clockplannerproject.ui.settings.course.CourseViewModel
import com.example.clockplannerproject.ui.settings.practice.PracticeViewModel
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
    single<ReportRepository> { RoomReportRepository(get(), fileStore = get()) }
    single { FilesDirReportStore(androidContext().filesDir) }
    single<com.example.clockplannerproject.kit.core.ReportFileStore> { get<FilesDirReportStore>() }
    single<com.example.clockplannerproject.kit.core.ReportAttachmentImporter> {
        AndroidReportImporter(androidContext(), get())
    }
    single<AppearanceRepository> { DataStoreAppearanceRepository.create(androidContext()) }
    single<DialPhotoFiles> { FilesDirDialPhotoStore(androidContext().filesDir) }
    single { DialPhotoSelection(get(), get()) }
    single<FlowMemoryStore> { DataStoreFlowMemoryStore.create(androidContext()) }
    single<PracticeProgressStore> { DataStorePracticeProgressStore.create(androidContext()) }
    single<com.example.clockplannerproject.kit.core.GoalBookStore> {
        com.example.clockplannerproject.data.DataStoreGoalBookStore.create(androidContext())
    }
    single<com.example.clockplannerproject.kit.core.CourseHistoryStore> {
        com.example.clockplannerproject.data.DataStoreCourseHistoryStore.create(androidContext())
    }
    single<com.example.clockplannerproject.kit.core.CustomCourseStore> {
        com.example.clockplannerproject.data.DataStoreCustomCourseStore.create(androidContext())
    }
    single<PlannerPlanStore> { DataStorePlannerPlanStore.create(androidContext()) }
    single<PlannerPhrases> { AndroidPlannerPhrases(androidContext()) }
    single<PlannerSource> { MockPlannerSource(get(), get(), get()) }
    single<TimeProvider> { SystemTimeProvider() }
    single<SampleSeedFlagStore> { DataStoreSampleSeedFlagStore.create(androidContext()) }
    single { SampleDaySeeder(get(), get(), get(), purgeFixtures = true) }
    viewModelOf(::DayViewModel)
    viewModelOf(::TasksViewModel)
    viewModelOf(::ReportsViewModel)
    viewModelOf(::SettingsViewModel)
    viewModelOf(::GoalsViewModel)
    viewModelOf(::CourseViewModel)
    viewModelOf(::PracticeViewModel)
}
