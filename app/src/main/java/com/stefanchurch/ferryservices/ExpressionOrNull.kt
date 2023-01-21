package com.stefanchurch.ferryservices

inline fun <R> expressionOrNull(block: () -> R): R? {
    return try {
        block()
    } catch (e: Throwable) {
        null
    }
}
