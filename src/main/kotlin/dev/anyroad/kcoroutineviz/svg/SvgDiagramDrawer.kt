package dev.anyroad.kcoroutineviz.svg

import com.github.nwillc.ksvg.elements.SVG
import dev.anyroad.kcoroutineviz.diagram.CoroutineDiagram
import kotlin.math.max

class SvgDiagramDrawer(
    private val settings: SvgRenderingSettings = SvgRenderingSettings(),
    private val scaler: HorizontalCoordinateScaler
) {

    fun draw(coroutineDiagram: CoroutineDiagram): String {
        val svg = SVG.svg(true) {
            val resultWidth = coroutineDiagram.lastBlockEndsMillis
            width = scaler.scaleFullWidth(resultWidth)
            style {
                body = settings.bodyStyle
            }

            val totalHeight = drawDiagram(this, coroutineDiagram, 0)
            val heightWithAxes = drawAxes(this, resultWidth, totalHeight)
            val heightWithMarks = drawMarksFooter(this, coroutineDiagram, heightWithAxes)
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

    private fun drawMarksFooter(svg: SVG, coroutineDiagram: CoroutineDiagram, heightWithAxes: Int): Int {
        if (coroutineDiagram.allMarksAreInline) {
            return heightWithAxes
        }

        var yOffset = heightWithAxes
        val marksSettings = settings.marksSettings

        coroutineDiagram.marks.forEach { mark ->
            svg.text {
                x = (scaler.margin + marksSettings.footerLineLeftMargin).toString()
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

    private fun drawAxes(svg: SVG, resultWidth: Int, totalHeight: Int): Int {
        val axesSettings = settings.axesSettings
        val spaceBetweenSecondAxes = axesSettings.spaceBetweenAxes
        val maxMillisToTitleEveryAxe = axesSettings.maxMillisToTitleEveryAxe
        val axesSpace =
            if (resultWidth < maxMillisToTitleEveryAxe) spaceBetweenSecondAxes else spaceBetweenSecondAxes * 2

        var axeX = 0
        while (axeX < resultWidth) {
            val xValue = scaler.scaleHorizontalCoordinate(axeX)
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
                    x = scaler.scaleHorizontalCoordinate(axeX, -10)
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
    ): Int {
        var endOffset = yOffset

        val parentRect = svg.rect {
            x = scaler.scaleHorizontalCoordinate(coroutineDiagram.startMillis)
            y = yOffset.toString()
            fill = calculateDiagramColor(coroutineDiagram)

            width = scaler.scaleWidth(coroutineDiagram.fullBoxWidth - 1)
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

    private fun calculateDiagramColor(coroutineDiagram: CoroutineDiagram) =
        settings.mainColor + Integer.toHexString((coroutineDiagram.nestingLevel + 1) * 35)
}
