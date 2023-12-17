package dev.anyroad.kcoroutineviz.svg

import com.github.nwillc.ksvg.elements.SVG
import dev.anyroad.kcoroutineviz.diagram.CoroutineDiagram
import kotlin.math.max

data class SecondAxesSettings(
    val spaceBetweenAxes: Int = 50,
    val maxMillisToTitleEveryAxe: Int = 500,
    val color: String = "gray",
    val lineDashStroke: String = "4 1",
    val titleColor: String = "gray"
)

data class MarksSettings(
    val radius: Int = 4,
    val markSectionHeight: Int = 20,
    val inlineSectionHeight: Int = 20,
    val inlineSectionFontSize: Int = 14,
    val inlineSectionLeftMargin: Int = 10,

    val footerLineHeight: Int = 20,
    val footerLineLeftMargin: Int = 5,
    val footerFontSize: Int = 14,
)

data class TitleSettings(
    val titleSectionHeight: Int = 20,
    val noTitleOffset: Int = 5,
    val leftMargin: Int = 5,
    val fontSize: Int = 16,
)

data class SvgRenderingSettings(
    val margin: Int = 10,
    val mainColor: String = "#00DD00",
    val axesSettings: SecondAxesSettings = SecondAxesSettings(),
    val marksSettings: MarksSettings = MarksSettings(),
    val titleSettings: TitleSettings = TitleSettings(),
    val lastRowMargin: Int = 5,
    val betweenRowMargin: Int = 10,
    val bodyStyle: String = """
         svg .black-stroke { stroke: black; stroke-width: 2; }
         svg .fur-color { fill: white; }
     """.trimIndent()
)

class SvgDiagramDrawer(private val settings: SvgRenderingSettings = SvgRenderingSettings()) {

    fun draw(coroutineDiagram: CoroutineDiagram, resultWidth: Int): String {
        val svg = SVG.svg(true) {
            // height = coroutineDiagram.endsMillis
            width = resultWidth.toString()
            style {
                body = settings.bodyStyle
            }

            val scale = (resultWidth - settings.margin * 2).toDouble() / coroutineDiagram.lastBlockEndsMillis.toDouble()

            val totalHeight = drawDiagram(this, coroutineDiagram, 0, scale)
            val heightWithAxes = drawAxes(this, resultWidth, totalHeight, scale)
            val heightWithMarks = drawMarksFooter(this, coroutineDiagram, heightWithAxes)
            height = (heightWithMarks + settings.margin).toString()
        }
        val sb = StringBuilder()
        svg.render(sb, SVG.RenderMode.FILE)
        return sb.toString()
            // ksvg does not support emojis well so for now just manually replace
            // from encoded value to unicode string
            .replace("&#55357;&#56688;", "🕰")
            .replace("&#55357;&#56656;", "🕐")
    }

    private fun drawMarksFooter(svg: SVG, coroutineDiagram: CoroutineDiagram, heightWithAxes: Int): Int {
        var yOffset = heightWithAxes
        val marksSettings = settings.marksSettings

        coroutineDiagram.marks.forEach { mark ->
            svg.text {
                x = (settings.margin + marksSettings.footerLineLeftMargin).toString()
                y = (yOffset + marksSettings.footerLineHeight).toString()
                fontSize = marksSettings.footerFontSize.toString()
                body = "[${mark.index}] : ${mark.title}"
            }
            yOffset += marksSettings.footerLineHeight
        }

        coroutineDiagram.childrenRows.forEach {
            it.children.forEach { c ->
                yOffset = drawMarksFooter(svg, c, yOffset)
            }
        }

        return yOffset
    }

    private fun xCoord(x: Int, scale: Double, offset: Int = 0): String {
        return (settings.margin + x * scale + offset).toString()
    }

    private fun xWidth(x: Int, scale: Double): String {
        return (x * scale).toString()
    }

    private fun drawAxes(svg: SVG, resultWidth: Int, totalHeight: Int, scale: Double): Int {
        val axesSettings = settings.axesSettings
        val spaceBetweenSecondAxes = axesSettings.spaceBetweenAxes
        val maxMillisToTitleEveryAxe = axesSettings.maxMillisToTitleEveryAxe
        val axesSpace =
            if (resultWidth < maxMillisToTitleEveryAxe) spaceBetweenSecondAxes else spaceBetweenSecondAxes * 2

        var axeX = 0
        while (axeX < resultWidth) {
            val xValue = xCoord(axeX, scale)
            svg.line {
                x1 = xValue
                y1 = "0"

                x2 = xValue
                y2 = totalHeight.toString()

                stroke = axesSettings.color
                attributes["stroke-dasharray"] = axesSettings.lineDashStroke
            }
            if (axeX % axesSpace == 0) {
                svg.text {
                    x = xCoord(axeX, scale, -10)
                    y = (totalHeight + 15).toString()
                    fill = axesSettings.titleColor
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
    ): Int {
        var endOffset = yOffset
        val startOffset = yOffset

        val parentRect = svg.rect {
            x = xCoord(coroutineDiagram.startMillis, scale)
            y = yOffset.toString()
            fill = calculateDiagramColor(coroutineDiagram)

            width = xWidth(coroutineDiagram.durationMillis, scale)
        }

        endOffset = drawDiagramTitle(coroutineDiagram, svg, scale, yOffset, endOffset)

        val marksSettings = settings.marksSettings
        if (coroutineDiagram.marks.isNotEmpty()) {
            coroutineDiagram.marks.forEach { mark ->
                svg.circle {
                    cx = xCoord(mark.timeMillis, scale)
                    cy = (endOffset + marksSettings.inlineSectionHeight / 2).toString()
                    r = marksSettings.radius.toString()
                    fill = mark.color
                }
                svg.text {
                    x = xCoord(mark.timeMillis, scale, marksSettings.inlineSectionLeftMargin)
                    y = (endOffset + marksSettings.inlineSectionFontSize).toString()
                    body = "[${mark.index}]"
                }
            }
            endOffset += marksSettings.inlineSectionHeight
        }

        coroutineDiagram.childrenRows.forEachIndexed { index, row ->
            var rowOffset = endOffset
            row.children.forEach { childDiagram ->
                rowOffset = max(rowOffset, drawDiagram(svg, childDiagram, endOffset, scale))
            }
            endOffset =
                rowOffset + if (index == coroutineDiagram.childrenRows.size - 1) settings.lastRowMargin else settings.betweenRowMargin
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

    private fun drawDiagramTitle(
        coroutineDiagram: CoroutineDiagram,
        svg: SVG,
        scale: Double,
        yOffset: Int,
        endOffset: Int
    ): Int {
        var newEndOffset = endOffset
        val titleSettings = settings.titleSettings
        if (coroutineDiagram.name.isNotBlank()) {
            svg.text {
                x = xCoord(coroutineDiagram.startMillis, scale, titleSettings.leftMargin)
                y = (yOffset + titleSettings.fontSize).toString()
                fontSize = titleSettings.fontSize.toString()
                body = coroutineDiagram.name
            }

            newEndOffset += titleSettings.titleSectionHeight
        } else {
            newEndOffset += titleSettings.noTitleOffset
        }
        return newEndOffset
    }

    private fun calculateDiagramColor(coroutineDiagram: CoroutineDiagram) =
        settings.mainColor + Integer.toHexString((coroutineDiagram.nestingLevel + 1) * 40)
}
