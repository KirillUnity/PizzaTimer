package com.example.clockplannerproject.data

import com.example.clockplannerproject.kit.core.Appearance
import com.example.clockplannerproject.kit.core.AppearanceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * In-memory [AppearanceRepository] for previews and JVM tests.
 *
 * @since 1.0.0
 */
class InMemoryAppearanceRepository(
    initial: Appearance = Appearance.Default,
    onboardingSeen: Boolean = false,
) : AppearanceRepository {
    private val appearance = MutableStateFlow(initial)
    private val seen = MutableStateFlow(onboardingSeen)

    override fun observeAppearance(): Flow<Appearance> = appearance

    override suspend fun save(appearance: Appearance) {
        this.appearance.value = appearance
    }

    override fun observeOnboardingSeen(): Flow<Boolean> = seen

    override suspend fun setOnboardingSeen(seen: Boolean) {
        this.seen.value = seen
    }
}
