package dev.anyroad.kcoroutineviz.svg

import com.github.nwillc.ksvg.elements.SVG

class AxesDrawer(
    private val axesSettings: SecondAxesSettings
) {
    fun drawAxes(svg: SVG, millisEnd: Int, mainPartHeight: Int, scaler: HorizontalCoordinateScaler): Int {
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

                stroke = axesSettings.color
                attributes["stroke-dasharray"] = axesSettings.lineDashStroke
            }
            if (axeX % axesSpace == 0) {
                svg.text {
                    x = scaler.scaleHorizontalCoordinate(axeX, if (axeX < 1000) -10 else -20)
                    y = (mainPartHeight + axesSettings.fontSize + axesSettings.margin).toString()
                    fill = axesSettings.titleColor
                    body = "${axeX}ms"
                }
            }
            axeX += axesSettings.spaceBetweenAxes
        }

        return axesSettings.margin * 2 + axesSettings.fontSize
    }
}
