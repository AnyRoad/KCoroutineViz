package dev.anyroad.kcoroutineviz.svg

import com.github.nwillc.ksvg.elements.SVG

class AxesDrawer(
    private val axesSettings: SecondAxesSettings,
    private val scaler: HorizontalCoordinateScaler
) {
    companion object {
        const val AXE_LINE_STYLE = "seconds-axe-line"
        const val AXE_VALUE_STYLE = "seconds-axe-value"
    }

    fun drawAxes(svg: SVG, millisEnd: Int, mainPartHeight: Int): Int {
        val spaceBetweenSecondAxes = axesSettings.spaceBetweenAxes
        val maxMillisToTitleEveryAxe = axesSettings.maxMillisToTitleEveryAxe
        val axesSpace =
            if (millisEnd < maxMillisToTitleEveryAxe) spaceBetweenSecondAxes else spaceBetweenSecondAxes * 2

        var axeX = 0
        while (axeX < millisEnd) {
            val xValue = scaler.scaleHorizontalCoordinate(axeX)
            svg.line {
                x1 = xValue
                y1 = "0"

                x2 = xValue
                y2 = mainPartHeight.toString()

                cssClass = AXE_LINE_STYLE
            }
            if (axeX % axesSpace == 0) {
                svg.text {
                    x = scaler.scaleHorizontalCoordinate(axeX, if (axeX < 1000) -15 else -20)
                    y = (mainPartHeight + axesSettings.fontSize + axesSettings.margin).toString()
                    body = "${axeX}ms"
                    cssClass = AXE_VALUE_STYLE
                }
            }
            axeX += axesSettings.spaceBetweenAxes
        }

        return axesSettings.margin * 2 + axesSettings.fontSize
    }
}
