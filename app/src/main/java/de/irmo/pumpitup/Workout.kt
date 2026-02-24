package de.irmo.pumpitup

import java.util.UUID

data class Workout(
    val id: String = UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val count: Int
)
