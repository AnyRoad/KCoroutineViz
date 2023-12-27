package dev.anyroad.kcoroutineviz.svg

import com.github.nwillc.ksvg.elements.SVG
import dev.anyroad.kcoroutineviz.diagram.CoroutineDiagram
import kotlin.math.max

class SvgDiagramDrawer(
    private val settings: SvgRenderingSettings = SvgRenderingSettings(),
    private val scaler: HorizontalCoordinateScaler,
    private val axesDrawer: AxesDrawer,
    private val marksFooterDrawer: MarksFooterDrawer,
    private val colorCalculator: ColorCalculator
) {

    fun draw(coroutineDiagram: CoroutineDiagram): String {
        val svg = SVG.svg(true) {
            val resultWidth = coroutineDiagram.fullBoxWidth
            width = scaler.scaleFullWidth(resultWidth)
            style {
                body = settings.bodyStyle
            }

            val totalHeight = drawDiagram(this, coroutineDiagram, 0)
            val heightWithAxes = totalHeight + axesDrawer.drawAxes(this, resultWidth, totalHeight, scaler)
            val heightWithMarks = heightWithAxes + marksFooterDrawer.drawMarksFooter(this, coroutineDiagram, heightWithAxes, scaler)
            height = (heightWithMarks + scaler.margin).toString()
        }

        val sb = StringBuilder()
        svg.render(sb, SVG.RenderMode.FILE)
        return sb.toString()
            // ksvg does not support emojis well so for now just manually replace
            // from encoded value to unicode string
            .replace("&#55357;&#56688;", "🕰")
            .replace("&#55357;&#56656;", "🕐")
    }

    private fun drawDiagram(
        svg: SVG,
        coroutineDiagram: CoroutineDiagram,
        yOffset: Int,
    ): Int {
        var endOffset = yOffset

        val parentRect = svg.rect {
            x = scaler.scaleHorizontalCoordinate(coroutineDiagram.startMillis)
            y = yOffset.toString()
            fill = colorCalculator.colorForNestingLevel(coroutineDiagram.nestingLevel)

            width = scaler.scaleWidth(coroutineDiagram.fullBoxWidth)
        }

        endOffset = drawDiagramTitle(coroutineDiagram, svg, scaler, yOffset, endOffset)

        val marksSettings = settings.marksSettings
        if (coroutineDiagram.marks.isNotEmpty()) {
            coroutineDiagram.marks.forEach { mark ->
                svg.circle {
                    cx = scaler.scaleHorizontalCoordinate(mark.timeMillis)
                    cy = (endOffset + marksSettings.inlineSectionHeight / 2).toString()
                    r = marksSettings.radius.toString()
                    fill = mark.color
                }
                val markText = "[${mark.index}] " + if (mark.drawInlineTitle) mark.title else ""
                svg.text {
                    x = scaler.scaleHorizontalCoordinate(mark.timeMillis, marksSettings.inlineSectionLeftMargin)
                    y = (endOffset + marksSettings.inlineSectionFontSize).toString()
                    body = markText
                }
            }
            endOffset += marksSettings.inlineSectionHeight
        }

        coroutineDiagram.childrenRows.forEachIndexed { index, row ->
            var rowOffset = endOffset
            row.children.forEach { childDiagram ->
                rowOffset = max(rowOffset, drawDiagram(svg, childDiagram, endOffset))
            }
            endOffset =
                rowOffset + if (index == coroutineDiagram.childrenRows.size - 1) settings.lastRowMargin else settings.betweenRowMargin
        }


        parentRect.height = (endOffset - yOffset).toString()
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
        scaler: HorizontalCoordinateScaler,
        yOffset: Int,
        endOffset: Int
    ): Int {
        var newEndOffset = endOffset
        val titleSettings = settings.titleSettings
        if (coroutineDiagram.name.isNotBlank()) {
            svg.text {
                x = scaler.scaleHorizontalCoordinate(coroutineDiagram.startMillis, titleSettings.leftMargin)
                y = (yOffset + titleSettings.margin + titleSettings.fontSize).toString()
                fontSize = titleSettings.fontSize.toString()
                body = coroutineDiagram.name
            }

            newEndOffset += titleSettings.fontSize + 2 * titleSettings.margin
        } else {
            newEndOffset += titleSettings.noTitleOffset
        }
        return newEndOffset
    }
}
