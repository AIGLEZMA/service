package me.aiglez.service.ui.common

import kotlin.math.absoluteValue
import kotlin.random.Random

internal fun newUiId(prefix: String): String {
    return "$prefix-${Random.nextLong().absoluteValue.toString(16)}"
}

internal fun slugify(value: String): String {
    return value.trim()
        .lowercase()
        .replace(Regex("[^a-z0-9]+"), "_")
        .trim('_')
        .ifBlank { "field" }
}



