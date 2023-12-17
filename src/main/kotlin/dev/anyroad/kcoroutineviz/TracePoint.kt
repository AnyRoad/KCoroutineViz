package dev.anyroad.kcoroutineviz

import kotlin.time.Duration

data class TracePoint(
    val timeElapsed: Duration,
    val description: String,
    val color: String,
    val threadId: Long = Thread.currentThread().id,
    val threadName: String = Thread.currentThread().name

)
