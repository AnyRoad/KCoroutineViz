package dev.anyroad.kcoroutineviz.diagram

import dev.anyroad.kcoroutineviz.watcher.TracePoint
import dev.anyroad.kcoroutineviz.watcher.Watcher
import java.util.concurrent.atomic.AtomicInteger

class DiagramBuilder {
    companion object {
        val comparator = Comparator<Watcher> { c1, c2 ->
            if (c1.absoluteTimeStartInMillis != c2.absoluteTimeStartInMillis) {
                c1.absoluteTimeStartInMillis - c2.absoluteTimeStartInMillis
            } else {
                c1.durationInMillis - c2.durationInMillis
            }
        }
    }

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
            startMillis = watcher.absoluteTimeStartInMillis,
            durationMillis = watcher.durationInMillis,
            marks = watcher.tracePoints.map { point -> toCoroutineMark(point, lastMarkIndex.incrementAndGet()) },
            childrenRows = splitChildrenIntoRows(watcher.children, nestingLevel + 1, lastMarkIndex)
        )

        return diagram
    }

    private fun toCoroutineMark(point: TracePoint, markIndex: Int): CoroutineMark =
        CoroutineMark(
            timeMillis = point.absoluteTimeInMillis,
            title = "${point.description}[tid=${point.threadId}]",
            color = point.color,
            index = markIndex,
            drawInlineTitle = point.drawInlineTitle
        )

    private fun splitChildrenIntoRows(
        children: List<Watcher>,
        nestingLevel: Int,
        lastMarkIndex: AtomicInteger
    ): List<ChildrenRow> {
        val childrenRows = mutableListOf<MutableList<CoroutineDiagram>>()

        val sortedChildren = children.sortedWith(comparator)

        for (child in sortedChildren) {
            val diagram = buildDiagramBlock(child, nestingLevel, lastMarkIndex)
            val rowToInsert = childrenRows.find { it.last().lastBlockEndsMillis <= diagram.startMillis }
                ?: mutableListOf<CoroutineDiagram>().also(childrenRows::add)
            rowToInsert.add(diagram)
        }

        return childrenRows.map { ChildrenRow(it) }
    }
}
