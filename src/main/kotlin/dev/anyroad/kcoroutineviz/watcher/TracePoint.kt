package dev.anyroad.kcoroutineviz.watcher

import kotlin.time.Duration

data class TracePoint(
    val timeElapsed: Duration,
    val description: String,
    val color: String,
    val drawInlineTitle: Boolean = false,
    val threadId: Long = Thread.currentThread().id,
    val threadName: String = Thread.currentThread().name
) {
    val absoluteTimeInMillis: Int
        get() = timeElapsed.inWholeMilliseconds.toInt()
}
