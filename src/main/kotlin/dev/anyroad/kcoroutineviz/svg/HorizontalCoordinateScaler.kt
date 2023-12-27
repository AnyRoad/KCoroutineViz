package dev.anyroad.kcoroutineviz.svg

class HorizontalCoordinateScaler(
    actualSize: Int,
    drawingSize: Int,
    val margin: Int = 10
) {
    private val scale = (drawingSize - margin * 4).toDouble() / actualSize

    fun scaleHorizontalCoordinate(x: Int, offset: Int = 0): String {
        return (margin + x * scale + offset).toString()
    }

    fun scaleWidth(width: Int): String {
        return (width * scale).toString()
    }

    fun scaleFullWidth(width: Int): String {
        return (width * scale + 4 * margin).toString()
    }
}
