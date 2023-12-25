package dev.anyroad.kcoroutineviz.diagram

import kotlin.math.max

data class CoroutineMark(
    val index: Int,
    val timeMillis: Int,
    val title: String,
    val color: String,
    val drawInlineTitle: Boolean = false
)

data class ChildrenRow(
    val children: List<CoroutineDiagram>
) {
    val lastBlockEndsMillis: Int = children.maxOfOrNull(CoroutineDiagram::lastBlockEndsMillis) ?: 0
}

data class CoroutineDiagram(
    val name: String,
    val nestingLevel: Int,
    val startMillis: Int,
    val durationMillis: Int,
    val marks: List<CoroutineMark>,
    val childrenRows: List<ChildrenRow>
) {
    val endsMillis = startMillis + durationMillis

    val lastBlockEndsMillis: Int = max(childrenRows.maxOfOrNull(ChildrenRow::lastBlockEndsMillis) ?: 0, endsMillis)

    val fullBoxWidth = lastBlockEndsMillis - startMillis

    val allMarksAreInline: Boolean = marks.all(CoroutineMark::drawInlineTitle)
            && childrenRows.all { ch -> ch.children.all(CoroutineDiagram::allMarksAreInline) }
}
