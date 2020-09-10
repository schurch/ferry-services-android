package com.stefanchurch.ferryservices.models

enum class Status(val value: Int) {
    NORMAL(0),
    DISRUPTED(1),
    CANCELLED(2),
    UNKNOWN(-99)
}