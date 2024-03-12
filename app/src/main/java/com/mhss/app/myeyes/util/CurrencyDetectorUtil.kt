package com.mhss.app.myeyes.util

fun Map<String, Int>.toCurrencySummary(): String {
    val numbersOnly = this.mapKeys { it.key.dropLast(3) }.toSortedMap()
    val summary = buildString {
        numbersOnly.onEachIndexed { index, entry ->
            if (index != 0 && index == numbersOnly.size - 1) append("and ")
            if (numbersOnly.size == 1 && entry.value == 1) append(entry.key)
            else append("${entry.value} ${entry.key},")
        }
        append("Egyptian Pounds")
    }
    return summary
}