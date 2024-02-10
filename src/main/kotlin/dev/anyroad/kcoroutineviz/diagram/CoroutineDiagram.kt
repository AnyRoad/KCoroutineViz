package dev.anyroad.kcoroutineviz.diagram

import kotlin.math.max

data class CoroutineMark(
    val index: Int,
    val timeMillis: Int,
    val title: String,
    val drawInlineTitle: Boolean = false
)

data class ChildrenRow(
    val children: List<CoroutineDiagram>
) {
    val lastBlockEndsMillis: Int = children.maxOfOrNull(CoroutineDiagram::lastBlockEndsMillis) ?: 0

    val maxNestingLevel: Int = children.maxOfOrNull(CoroutineDiagram::maxNestingLevel) ?: 0
}

data class CoroutineDiagram(
    val name: String,
    val nestingLevel: Int,
    val startMillis: Int,
    val durationMillis: Int,
    val marks: List<CoroutineMark>,
    val childrenRows: List<ChildrenRow>
) {
    private val endsMillis = startMillis + durationMillis

    internal val lastBlockEndsMillis: Int = max(childrenRows.maxOfOrNull(ChildrenRow::lastBlockEndsMillis) ?: 0, endsMillis)

    val fullBoxWidth = lastBlockEndsMillis - startMillis

    val allMarksIncludingChildren: List<CoroutineMark> = marks +
            childrenRows.flatMap { ch -> ch.children.flatMap (CoroutineDiagram::marks) }

    val maxNestingLevel = max(childrenRows.maxOfOrNull(ChildrenRow::maxNestingLevel) ?: 0, nestingLevel)
}
