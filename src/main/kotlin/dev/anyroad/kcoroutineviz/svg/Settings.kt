package dev.anyroad.kcoroutineviz.svg

data class SecondAxesSettings(
    val spaceBetweenAxes: Int = 50,
    val maxMillisToTitleEveryAxe: Int = 500,
    val color: String = "#55555555",
    val lineDashStroke: String = "4 1",
    val titleColor: String = "gray",
    val fontSize: Int = 14,
    val margin: Int = 3
)

data class MarksSettings(
    val color: String = "green",
    val radius: Int = 4,
    val titleColor: String = "rgb(217 119 6)",
    val markSectionHeight: Int = 20,
    val inlineSectionHeight: Int = 20,
    val inlineSectionFontSize: Int = 14,
    val inlineSectionLeftMargin: Int = 10,

    val footerLineHeight: Int = 20,
    val footerLineLeftMargin: Int = 5,
    val footerFontSize: Int = 14,
)

data class TitleSettings(
    val margin: Int = 5,
    val noTitleOffset: Int = 5,
    val leftMargin: Int = 5,
    val fontSize: Int = 13,
    val color: String = "black"
)

data class SvgRenderingSettings(
    val margin: Int = 10,
    val darkestColor: Triple<Int, Int, Int> = Triple(64, 192, 64),
    val axesSettings: SecondAxesSettings = SecondAxesSettings(),
    val marksSettings: MarksSettings = MarksSettings(),
    val titleSettings: TitleSettings = TitleSettings(),
    val lastRowMargin: Int = 5,
    val betweenRowMargin: Int = 10,
)
