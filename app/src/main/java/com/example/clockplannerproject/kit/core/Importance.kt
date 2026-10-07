package com.example.clockplannerproject.kit.core

/**
 * Priority of an **untimed** task in leftover packing.
 *
 * Weights HIGH:MEDIUM:LOW = **5:3:2**. Civil [TimeBlock]s ignore this;
 * only leftover minutes on a 12-hour face are shared.
 *
 * @since 0.3.0
 */
enum class Importance(
    /** Leftover share numerator. */
    val leftoverWeight: Int,
) {
    HIGH(5),
    MEDIUM(3),
    LOW(2),
}
