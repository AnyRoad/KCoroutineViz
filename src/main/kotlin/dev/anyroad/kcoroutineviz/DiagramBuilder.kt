package dev.anyroad.kcoroutineviz

import java.util.concurrent.atomic.AtomicInteger

class DiagramBuilder {
    fun buildDiagram(watcher: Watcher): CoroutineDiagram {
        return buildDiagramBlock(watcher, 0)
    }

    private fun buildDiagramBlock(
        watcher: Watcher,
        nestingLevel: Int = 0,
        lastMarkIndex: AtomicInteger = AtomicInteger(0)
    ): CoroutineDiagram {
        val diagram = CoroutineDiagram(
            name = watcher.name,
            nestingLevel = nestingLevel,
            startMillis = watcher.absoluteTimeStart.inWholeMilliseconds,
            durationMillis = watcher.duration().inWholeMilliseconds,
            marks = watcher.tracePoints.map { point ->
                CoroutineMark(
                    timeMillis = point.timeElapsed.inWholeMilliseconds,
                    title = "${point.description}[thread id=${point.threadId}]",
                    color = point.color,
                    index = lastMarkIndex.incrementAndGet()
                )
            },
            childrenRows = splitChildrenIntoRows(watcher.children, nestingLevel + 1, lastMarkIndex)
        )

        return diagram
    }

    private fun splitChildrenIntoRows(
        children: List<Watcher>,
        nestingLevel: Int,
        lastMarkIndex: AtomicInteger
    ): List<ChildrenRow> {
        val childrenRows = mutableListOf<MutableList<CoroutineDiagram>>()

        val sortedChildren = children.sortedWith { c1, c2 ->
            if (c1.absoluteTimeStart != c2.absoluteTimeStart) {
                c1.absoluteTimeStart.inWholeMilliseconds.toInt() - c2.absoluteTimeStart.inWholeMilliseconds.toInt()
            } else {
                c1.duration().inWholeMilliseconds.toInt() - c2.duration().inWholeMilliseconds.toInt()
            }
        }

        for (child in sortedChildren) {
            val diagram = buildDiagramBlock(child, nestingLevel, lastMarkIndex)
            val rowToInsert = childrenRows.find { it.last().endsMillis <= diagram.startMillis }
                ?: mutableListOf<CoroutineDiagram>().also(childrenRows::add)
            rowToInsert.add(diagram)
        }

        return childrenRows.map { ChildrenRow(it) }
    }
}
