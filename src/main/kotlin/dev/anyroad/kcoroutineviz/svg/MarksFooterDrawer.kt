package dev.anyroad.kcoroutineviz.svg

import com.github.nwillc.ksvg.elements.SVG
import dev.anyroad.kcoroutineviz.diagram.CoroutineDiagram
import dev.anyroad.kcoroutineviz.diagram.CoroutineMark

class MarksFooterDrawer(
    private val marksSettings: MarksSettings,
    private val scaler: HorizontalCoordinateScaler

) {
    companion object {
        const val MARK_TITLE_STYLE = "mark-footer-title"
    }

    fun drawMarksFooter(
        svg: SVG,
        coroutineDiagram: CoroutineDiagram,
        mainPartHeight: Int,
    ): Int {
        val allMarks = coroutineDiagram.allMarksIncludingChildren

        if (allMarks.all(CoroutineMark::drawInlineTitle)) {
            return 0
        }

        var yOffset = mainPartHeight

        allMarks.sortedBy(CoroutineMark::index).forEach { mark ->
            svg.text {
                x = (scaler.margin + marksSettings.footerLineLeftMargin).toString()
                y = (yOffset + marksSettings.footerLineHeight).toString()
                fontSize = marksSettings.footerFontSize.toString()
                body = "[${mark.index}] : ${mark.title}"
                cssClass = MARK_TITLE_STYLE
            }
            yOffset += marksSettings.footerLineHeight
        }

        return yOffset - mainPartHeight
    }
}
