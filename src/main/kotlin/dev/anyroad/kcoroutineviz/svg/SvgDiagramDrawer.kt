package dev.anyroad.kcoroutineviz.svg

import com.github.nwillc.ksvg.elements.SVG
import dev.anyroad.kcoroutineviz.diagram.CoroutineDiagram

internal class SvgDiagramDrawer(
    private val settings: SvgRenderingSettings = SvgRenderingSettings(),
    private val drawingSize: Int,
    private val axesDrawer: AxesDrawer,
    private val marksFooterDrawer: MarksFooterDrawer,
    private val diagramBodyDrawer: DiagramBodyDrawer,
    private val colorCalculator: ColorCalculator,
    private val coroutineDiagram: CoroutineDiagram
) {
    companion object {
        fun buildDrawer(
            diagram: CoroutineDiagram,
            drawingSize: Int,
            svgRenderingSettings: SvgRenderingSettings,
        ): SvgDiagramDrawer {
            val scaler = HorizontalCoordinateScaler(diagram.fullBoxWidth, drawingSize, svgRenderingSettings.margin)
            val colorCalculator = ColorCalculator(diagram.maxNestingLevel, svgRenderingSettings.darkestColor)
            return SvgDiagramDrawer(
                drawingSize = drawingSize,
                axesDrawer = AxesDrawer(SecondAxesSettings(), scaler),
                marksFooterDrawer = MarksFooterDrawer(MarksSettings(), scaler),
                diagramBodyDrawer = DiagramBodyDrawer(
                    settings = svgRenderingSettings,
                    scaler = scaler
                ),
                colorCalculator = colorCalculator,
                coroutineDiagram = diagram
            )
        }
    }


    fun draw(): String {
        val svg = SVG.svg(true) {
            val resultWidth = coroutineDiagram.fullBoxWidth
            width = drawingSize.toString()
            style {
                body = "__STYLES__"
            }

            var totalHeight = diagramBodyDrawer.drawDiagram(this, coroutineDiagram)
            totalHeight += axesDrawer.drawAxes(this, resultWidth, totalHeight)
            totalHeight += marksFooterDrawer.drawMarksFooter(this, coroutineDiagram, totalHeight)
            height = (totalHeight + settings.margin).toString()
        }

        val sb = StringBuilder()
        svg.render(sb, SVG.RenderMode.FILE)
        return sb.toString()
            // ksvg does not support emojis well so for now just manually replace
            // from encoded value to unicode string
            .replace("&#55357;&#56688;", "&#128368;")
            .replace("&#55357;&#56656;", "&#128336;")
            // to avoid unnecessary escaping in the styles
            .replace("__STYLES__", createStyles())
    }

    private fun createStyles(): String {
        val nestedLevelStyles = 0.rangeTo(coroutineDiagram.maxNestingLevel).map { level ->
            """
                rect.${DiagramBodyDrawer.DIAGRAM_BLOCK_STYLE_PREFIX}$level {
                  fill: ${colorCalculator.colorForNestingLevel(level)};
                }
            """.trimIndent()
        }

        return """
            <![CDATA[
            circle.${DiagramBodyDrawer.MARK_CIRCLE_STYLE} {
               fill: ${settings.marksSettings.color};
            }
            
            rect.${DiagramBodyDrawer.DIAGRAM_BORDER_STYLE} {
               stroke: black;
               strokeWidth: 1;
               fill: none;
            }
            
            ${nestedLevelStyles.joinToString("\n")}
            
            line.${AxesDrawer.AXE_LINE_STYLE} {
               stroke: ${settings.axesSettings.color};
               stroke-dasharray: ${settings.axesSettings.lineDashStroke};
            }
            
            text.${AxesDrawer.AXE_VALUE_STYLE} {
              fill: ${settings.axesSettings.titleColor};
            }
            
            text.${MarksFooterDrawer.MARK_TITLE_STYLE} {
               fill: ${settings.marksSettings.titleColor};
            }
            
            text.${DiagramBodyDrawer.DIAGRAM_TITLE_STYLE} {
               fill: ${settings.titleSettings.color}
            }
            
            text.${DiagramBodyDrawer.MARK_INLINE_TITLE_STYLE} {
               fill: ${settings.marksSettings.titleColor};
            }
            
            ]]>
        """.trimIndent()
    }
}
