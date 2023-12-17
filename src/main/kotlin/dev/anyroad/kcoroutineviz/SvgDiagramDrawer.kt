package dev.anyroad.kcoroutineviz

import com.github.nwillc.ksvg.elements.SVG
import kotlin.math.max

class SvgDiagramDrawer(private val coroutineDiagram: CoroutineDiagram) {
    companion object {
        const val MARGIN = 10
    }

    fun draw(resultWidth: Int): String {
        val svg = SVG.svg(true) {
            // height = coroutineDiagram.endsMillis
            width = resultWidth.toString()
            style {
                body = """

                 svg .black-stroke { stroke: black; stroke-width: 2; }
                 svg .fur-color { fill: white; }

             """.trimIndent()
            }

            val scale = (resultWidth - MARGIN * 2).toDouble() / coroutineDiagram.lastBlockEndsMillis.toDouble()

            val totalHeight = drawDiagram(this, coroutineDiagram, 0, scale, 1)
            val heightWithAxes = drawAxes(this, resultWidth, totalHeight, scale)
            val heightWithMarks = drawMarks(this, coroutineDiagram, heightWithAxes)
            height = (heightWithMarks + MARGIN).toString()
        }
        val sb = StringBuilder()
        svg.render(sb, SVG.RenderMode.FILE)
        return sb.toString()
            // ksvg does not support emojis well so for now just manually replace
            // from encoded value to unicode string
            .replace("&#55357;&#56688;", "🕰")
            .replace("&#55357;&#56656;", "🕐")
    }

    private fun drawMarks(svg: SVG, coroutineDiagram: CoroutineDiagram, heightWithAxes: Int): Int {
        var yOffset = heightWithAxes

        coroutineDiagram.marks.forEach { mark ->
            svg.text {
                x = (MARGIN + 10).toString()
                y = (yOffset + 20).toString()
                body = "[${mark.index}] : ${mark.title}"
            }
            yOffset += 20
        }

        coroutineDiagram.childrenRows.forEach {
            it.children.forEach { c ->
                yOffset = drawMarks(svg, c, yOffset)
            }
        }

        return yOffset
    }

    private fun xCoord(x: Long, scale: Double, offset: Int = 0): String {
        return (MARGIN + x * scale + offset).toString()
    }

    private fun xWidth(x: Long, scale: Double): String {
        return (x * scale).toString()
    }

    private fun drawAxes(svg: SVG, resultWidth: Int, totalHeight: Int, scale: Double): Int {
        val axesSpace = if (resultWidth < 500) 50 else 100

        var axeX: Long = 0
        while (axeX < resultWidth) {
            val xValue = xCoord(axeX, scale)
            svg.line {
                x1 = xValue
                y1 = "5"

                x2 = xValue
                y2 = totalHeight.toString()

                stroke = "gray"
                attributes["stroke-dasharray"] = "4 1"
            }
            if (axeX % axesSpace == 0L) {
                svg.text {
                    x = xCoord(axeX, scale, -10)
                    y = (totalHeight + 15).toString()
                    fill = "gray"
                    body = "${axeX}ms"
                }
            }
            axeX += 50
        }

        return totalHeight + 25
    }

    private fun drawDiagram(
        svg: SVG,
        coroutineDiagram: CoroutineDiagram,
        yOffset: Int,
        scale: Double,
        markIndex: Int
    ): Int {
        var endOffset = yOffset
        val startOffset = yOffset
        var currentMarkIndex = markIndex

        val parentRect = svg.rect {
            x = xCoord(coroutineDiagram.startMillis, scale)
            y = yOffset.toString()
            fill = "#00DD00" + Integer.toHexString((coroutineDiagram.nestingLevel + 1) * 40)

            width = xWidth(coroutineDiagram.durationMillis, scale)
        }

        if (coroutineDiagram.name.isNotBlank()) {
            svg.text {
                x = xCoord(coroutineDiagram.startMillis, scale, 5)
                y = (yOffset + 15).toString()
                body = coroutineDiagram.name
            }

            endOffset += 20
        } else {
            endOffset += 5
        }

        if (coroutineDiagram.marks.isNotEmpty()) {
            coroutineDiagram.marks.forEach { mark ->
                svg.circle {
                    cx = xCoord(mark.timeMillis, scale)
                    cy = (endOffset + 10).toString()
                    r = "4"
                    fill = "#880000"
                }
                svg.text {
                    x = xCoord(mark.timeMillis, scale, 10)
                    y = (endOffset + 15).toString()
                    body = "[${currentMarkIndex}]"
                }
                currentMarkIndex++
            }
            endOffset += 20
        }

        coroutineDiagram.childrenRows.forEachIndexed { index, row ->
            var rowOffset = endOffset
            row.children.forEach { childDiagram ->
                rowOffset = max(rowOffset, drawDiagram(svg, childDiagram, endOffset, scale, currentMarkIndex))
            }
            endOffset = rowOffset + if (index == coroutineDiagram.childrenRows.size - 1) 5 else 10
        }


        parentRect.height = (endOffset - startOffset).toString()
        svg.rect {
            x = parentRect.x
            y = parentRect.y
            width = parentRect.width
            height = parentRect.height
            stroke = "black"
            strokeWidth = "1"
            fill = "none"
        }

        return endOffset
    }
}
