package dev.anyroad.kcoroutineviz.svg

import com.github.nwillc.ksvg.elements.SVG
import dev.anyroad.kcoroutineviz.diagram.CoroutineDiagram

internal class SvgDiagramDrawer(
    private val settings: SvgRenderingSettings = SvgRenderingSettings(),
    private val drawingSize: Int,
    private val axesDrawer: AxesDrawer,
    private val marksFooterDrawer: MarksFooterDrawer,
    private val diagramBodyDrawer: DiagramBodyDrawer,
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
                    colorCalculator = colorCalculator,
                    scaler = scaler
                ),
                coroutineDiagram = diagram
            )
        }
    }


    fun draw(): String {
        val svg = SVG.svg(true) {
            val resultWidth = coroutineDiagram.fullBoxWidth
            width = drawingSize.toString()
            style {
                body = settings.bodyStyle
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
            .replace("&#55357;&#56688;", "🕰")
            .replace("&#55357;&#56656;", "🕐")
    }
}
