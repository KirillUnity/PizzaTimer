package com.example.clockplannerproject.kit.core

/**
 * Normalization and Room encoding for user-created task tags.
 *
 * Tags are free text. There is no built-in catalog. Matching ignores case;
 * the first spelling is kept. The unit separator is reserved for storage and
 * is stripped from typed text.
 *
 * @since 1.1.0
 */
object TaskTags {
    /** Character that separates tags in the Room column. Not valid inside a tag. */
    const val SEPARATOR: Char = '\u001f'

    /** Longest stored tag, in characters. Longer input is cut. */
    const val MAX_LENGTH: Int = 40

    /** Most tags kept on one task. Later names are dropped. */
    const val MAX_COUNT: Int = 12

    /**
     * Trims, drops blanks, cuts length, and drops case-insensitive duplicates.
     *
     * @param raw editor or stored names, in display order
     * @return stable list safe to persist and to paint as chips
     * @since 1.1.0
     */
    fun normalize(raw: List<String>): List<String> {
        val seen = LinkedHashSet<String>()
        val result = ArrayList<String>(raw.size)
        for (value in raw) {
            if (result.size >= MAX_COUNT) break
            val trimmed = value
                .replace(SEPARATOR, ' ')
                .trim()
                .take(MAX_LENGTH)
                .trim()
            if (trimmed.isEmpty()) continue
            if (seen.add(trimmed.lowercase())) {
                result.add(trimmed)
            }
        }
        return result
    }

    /**
     * @param tags already normalized names
     * @return empty string when [tags] is empty
     * @since 1.1.0
     */
    fun encode(tags: List<String>): String =
        normalize(tags).joinToString(separator = SEPARATOR.toString())

    /**
     * @param stored column value, or null
     * @return normalized tags; blank storage is an empty list
     * @since 1.1.0
     */
    fun decode(stored: String?): List<String> {
        if (stored.isNullOrBlank()) return emptyList()
        return normalize(stored.split(SEPARATOR))
    }
}
