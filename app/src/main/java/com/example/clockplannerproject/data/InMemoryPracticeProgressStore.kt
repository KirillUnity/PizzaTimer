package com.example.clockplannerproject.data

import com.example.clockplannerproject.kit.core.practice.PracticeProgressStore
import com.example.clockplannerproject.kit.core.practice.PracticeSkill
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

/**
 * Process-local [PracticeProgressStore] for tests and ViewModel defaults.
 *
 * @param initial skills already tried
 * @since 1.3.0
 */
class InMemoryPracticeProgressStore(
    initial: Set<PracticeSkill> = emptySet(),
) : PracticeProgressStore {
    private val skills = MutableStateFlow(initial)

    override fun observe(): Flow<Set<PracticeSkill>> = skills

    override suspend fun markTried(skill: PracticeSkill) {
        skills.update { current -> current + skill }
    }
}
