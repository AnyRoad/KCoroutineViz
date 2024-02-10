package dev.anyroad.kcoroutineviz.svg

import kotlin.math.min

class ColorCalculator(
    maxNestingLevel: Int,
    darkestColor: Triple<Int, Int, Int>
) {
    companion object {
        private const val MAX_COLOR_VALUE = 255
        private const val MIN_TRANSPARENCY = 64
    }

    val colorStep = Triple(
        (MAX_COLOR_VALUE - darkestColor.first) / maxNestingLevel,
        (MAX_COLOR_VALUE - darkestColor.second) / maxNestingLevel,
        (MAX_COLOR_VALUE - darkestColor.third) / maxNestingLevel
    )

    val transparencyStep = (MAX_COLOR_VALUE - MIN_TRANSPARENCY) / maxNestingLevel

    fun colorForNestingLevel(nestingLevel: Int): String {
        val transparency = MIN_TRANSPARENCY + nestingLevel * transparencyStep
        val r = colorToHex(colorComponent(colorStep.first, nestingLevel))
        val g = colorToHex(colorComponent(colorStep.second, nestingLevel))
        val b = colorToHex(colorComponent(colorStep.third, nestingLevel))
        return "#$r$g$b${colorToHex(transparency)}"
    }

    private fun colorComponent(colorStep: Int, nestingLevel: Int): Int {
        val invertValue = if (nestingLevel == 0) {
            colorStep / 2
        } else {
            min(MAX_COLOR_VALUE, colorStep * nestingLevel)
        }
        return MAX_COLOR_VALUE - invertValue
    }

    private fun colorToHex(color: Int): String {
        val transparencyHex = Integer.toHexString(color)

        return (if (transparencyHex.length == 1) "0" else "") + transparencyHex
    }
}
