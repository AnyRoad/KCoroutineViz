package dev.anyroad.kcoroutineviz

import dev.anyroad.kcoroutineviz.diagram.DiagramBuilder
import dev.anyroad.kcoroutineviz.svg.*
import dev.anyroad.kcoroutineviz.watcher.Watcher.Companion.watcher
import io.kotest.core.spec.style.FunSpec
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Paths

class WatcherTest : FunSpec({
    test("my first test") {
        val w = watcher("async test") {
            addTracePoint("before the start", true)
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
                    sleep(300)
                }
                awaitAll(d1, d2, d3)
            }

            watch("3") {
                delay(100)
                addTracePoint("another point", true)
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
        val drawer = SvgDiagramDrawer.buildDrawer(
            diagram,
            800,
            SvgRenderingSettings()
        )
        val svg = drawer.draw()
        println(svg)
        Files.write(
            Paths.get("src/main/resources/diagram.html"), listOf(
                "<html>",
                "<body>",
                svg,
                "</body>",
                "</html>",
            ),
            Charset.forName("UTF-8")
        )
    }

})
