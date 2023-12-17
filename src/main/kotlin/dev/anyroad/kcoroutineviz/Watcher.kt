package dev.anyroad.kcoroutineviz

import java.util.concurrent.CopyOnWriteArrayList
import kotlin.time.Duration
import kotlin.time.TimeMark
import kotlin.time.TimeSource

class Watcher(
    val name: String,
    val parent: Watcher? = null
) {
    var exception: Throwable? = null

    var children = CopyOnWriteArrayList<Watcher>()

    private var isRunning = false

    private lateinit var timeMark: TimeMark

    private var timeElapsed: Duration = Duration.ZERO

    var absoluteTimeStart: Duration = Duration.ZERO
        private set

    val tracePoints = CopyOnWriteArrayList<TracePoint>()

    var timeoutAt: Duration = Duration.ZERO
        private set

    private fun beforeStart() {
        if (isRunning) {
            return
        }
        timeMark = TimeSource.Monotonic.markNow()
        parent?.timeMark?.elapsedNow()?.let {
            absoluteTimeStart = it + parent.absoluteTimeStart
        }
        isRunning = true
    }

    private fun afterEnd() {
        if (!isRunning) {
            return
        }
        timeElapsed = timeMark.elapsedNow()
        isRunning = false

        // check recursively for watches that did not call afterEnd()
        if (parent == null) {
            timeoutAllRunningChildren(timeElapsed)
        }
    }

    private fun timeoutAllRunningChildren(timeoutAt: Duration) {
        children.forEach {
            if (it.isRunning) {
                it.timeout(timeoutAt)
            }
            it.timeoutAllRunningChildren(timeoutAt)
        }
    }

    private fun createChild(name: String): Watcher {
        val childWatcher = Watcher(name, this)
        children.add(childWatcher)
        return childWatcher
    }

    fun duration(): Duration {
        return if (timeElapsed > timeoutAt) {
            timeElapsed
        } else {
            timeoutAt
        }
    }

    fun timeout(duration: Duration) {
        if (isRunning) {
            isRunning = false
            timeoutAt = duration
        }
    }

    fun addTracePoint(description: String) {
        tracePoints.add(TracePoint(absoluteTimeStart + timeMark.elapsedNow(), description, "RED"))
    }

    suspend operator fun <T> invoke(block: suspend Watcher.() -> T): T? {
        beforeStart()
        val t = runCatching { block() }
        exception = t.exceptionOrNull()
        afterEnd()
        return t.getOrNull()
    }

    suspend fun <T> watch(name: String = "", block: suspend Watcher.() -> T): Watcher {
        val childWatcher = createChild(name)
        childWatcher.invoke(block)
        return childWatcher
    }

    suspend fun sleep(millis: Long, name: String = "") {
        val childName = "🕰" + name.ifBlank { " $millis ms" }
        watch(name = childName) {
            Thread.sleep(millis)
        }
    }

    suspend fun delay(millis: Long, name: String = "") {
        val childName = "🕐" + name.ifBlank { " $millis ms" }
        watch(name = childName) {
            kotlinx.coroutines.delay(millis)
        }
    }

    companion object {
        suspend fun <T> watcher(name: String, block: suspend Watcher.() -> T): Watcher {
            val watcher = Watcher(name)
            watcher.invoke(block)
            return watcher
        }
    }
}
