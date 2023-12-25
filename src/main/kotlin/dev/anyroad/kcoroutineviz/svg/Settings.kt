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
    val radius: Int = 4,
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
)

data class SvgRenderingSettings(
    val mainColor: String = "#00DD00",
    val marksSettings: MarksSettings = MarksSettings(),
    val titleSettings: TitleSettings = TitleSettings(),
    val lastRowMargin: Int = 5,
    val betweenRowMargin: Int = 10,
    val bodyStyle: String = """
         svg .black-stroke { stroke: black; stroke-width: 2; }
         svg .fur-color { fill: white; }
     """.trimIndent()
)
