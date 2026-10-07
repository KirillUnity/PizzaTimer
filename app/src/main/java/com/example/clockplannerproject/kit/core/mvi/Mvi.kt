package com.example.clockplannerproject.kit.core.mvi

/**
 * Marker for an immutable screen snapshot.
 *
 * @since 0.1.0
 */
interface UiState

/**
 * User or system wish that the ViewModel handles.
 *
 * @since 0.1.0
 */
interface UiIntent

/**
 * One-shot event (snackbar, navigation). Not part of [UiState].
 *
 * @since 0.1.0
 */
interface UiEffect
