package dev.anyroad.kcoroutineviz

import dev.anyroad.kcoroutineviz.diagram.DiagramBuilder
import dev.anyroad.kcoroutineviz.svg.*
import dev.anyroad.kcoroutineviz.watcher.Watcher.Companion.watcher
import io.kotest.core.spec.style.FunSpec
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import java.nio.file.Files
import java.nio.file.Paths

class WatcherTest : FunSpec({
    test("my first test") {
        val w = watcher("async test") {
            addTracePoint("before the start", "blue", true)
            watch("just call delay") {
                delay(100)
                delay(100)
                delay(100)
            }

            watch("call delay using async") {
                val d1 = async {
                    delay(300)
                }
                val d2 = async {
                    delay(300)
                }
                val d3 = async {
                    delay(300)
                }
                awaitAll(d1, d2, d3)
            }

            watch("3") {
                delay(100)
                addTracePoint("another point", "green", true)
                watch("4") {
                    delay(100)
                    watch("5") {
                        delay(100)
                        delay(100)
                        delay(100)
                    }
                }
                addTracePoint("after")
            }
        }

        println(w)
        val diagram = DiagramBuilder().buildDiagram(w)
        println(diagram)
        val scaler = HorizontalCoordinateScaler(diagram.fullBoxWidth, 1024, 10)
        val drawer = SvgDiagramDrawer(
            scaler = scaler,
            axesDrawer = AxesDrawer(SecondAxesSettings()),
            marksFooterDrawer = MarksFooterDrawer(MarksSettings()),
            colorCalculator = ColorCalculator(diagram.maxNestingLevel, Triple(64, 192, 64))
        )
        val svg = drawer.draw(diagram)
        println(svg)
        Files.write(Paths.get("src/main/resources/diagram.svg"), listOf(svg))
    }

})
