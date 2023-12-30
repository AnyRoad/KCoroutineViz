package dev.anyroad.kcoroutineviz.svg

import com.github.nwillc.ksvg.elements.SVG
import dev.anyroad.kcoroutineviz.diagram.CoroutineDiagram
import kotlin.math.max

class DiagramBodyDrawer(
    private val settings: SvgRenderingSettings,
    private val scaler: HorizontalCoordinateScaler
) {
    companion object {
        const val DIAGRAM_BLOCK_STYLE_PREFIX = "diagram-level-"
        const val DIAGRAM_BORDER_STYLE = "diagram-border"
        const val DIAGRAM_TITLE_STYLE = "diagram-title"
        const val MARK_CIRCLE_STYLE = "mark-circle"
        const val MARK_INLINE_TITLE_STYLE = "mark-title-inline"
    }

    fun drawDiagram(
        svg: SVG,
        coroutineDiagram: CoroutineDiagram,
        yOffset: Int = 0,
    ): Int {
        var endOffset = yOffset

        val parentRect = svg.rect {
            x = scaler.scaleHorizontalCoordinate(coroutineDiagram.startMillis)
            y = yOffset.toString()
            cssClass="$DIAGRAM_BLOCK_STYLE_PREFIX${coroutineDiagram.nestingLevel}"

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
                    cssClass = MARK_CIRCLE_STYLE
                    children.add(title(mark.title))
                }
                val markText = "[${mark.index}] " + if (mark.drawInlineTitle) mark.title else ""
                svg.text {
                    x = scaler.scaleHorizontalCoordinate(mark.timeMillis, marksSettings.inlineSectionLeftMargin)
                    y = (endOffset + marksSettings.inlineSectionFontSize).toString()
                    body = markText
                    children.add(title(mark.title))
                    cssClass = MARK_INLINE_TITLE_STYLE
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
            cssClass = DIAGRAM_BORDER_STYLE
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
                cssClass = DIAGRAM_TITLE_STYLE
            }

            newEndOffset += titleSettings.fontSize + 2 * titleSettings.margin
        } else {
            newEndOffset += titleSettings.noTitleOffset
        }
        return newEndOffset
    }
}
