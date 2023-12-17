package dev.anyroad.kcoroutineviz

import kotlin.math.max

data class CoroutineMark(
    val index: Int,
    val timeMillis: Long,
    val title: String,
    val color: String
)

data class ChildrenRow(
    val children: List<CoroutineDiagram>
) {
    val lastBlockEndsMillis: Long = children.maxOfOrNull(CoroutineDiagram::lastBlockEndsMillis) ?: 0
}

data class CoroutineDiagram(
    val name: String,
    val nestingLevel: Int,
    val startMillis: Long,
    val durationMillis: Long,
    val marks: List<CoroutineMark>,
    val childrenRows: List<ChildrenRow>
) {
    val endsMillis = startMillis + durationMillis

    val lastBlockEndsMillis: Long = max(childrenRows.maxOfOrNull(ChildrenRow::lastBlockEndsMillis) ?: 0, endsMillis)
}
