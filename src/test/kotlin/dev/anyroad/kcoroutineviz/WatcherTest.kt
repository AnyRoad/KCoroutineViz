package dev.anyroad.kcoroutineviz

import dev.anyroad.kcoroutineviz.diagram.DiagramBuilder
import dev.anyroad.kcoroutineviz.svg.SvgDiagramDrawer
import dev.anyroad.kcoroutineviz.watcher.Watcher.Companion.watcher
import io.kotest.core.spec.style.FunSpec
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import java.nio.file.Files
import java.nio.file.Paths

class WatcherTest : FunSpec({
    test("my first test") {
        val w = watcher("first test") {
            delay(200)
            sleep(200)
            addTracePoint("한국어 지원 테스트")

            watch("test parallel async sleeps") {
                val d1 = async(start = CoroutineStart.LAZY) {
                    delay(100)
                }
                val d2 = async {
                    delay(100)
                }
                addTracePoint("before delay")
                delay(50)
                addTracePoint("after delay")
                awaitAll(d1, d2)
            }
        }

        println(w)
        val diagram = DiagramBuilder().buildDiagram(w)
        println(diagram)
        val drawer = SvgDiagramDrawer()
        val svg = drawer.draw(diagram, 1024)
        println(svg)
        Files.write(Paths.get("src/main/resources/diagram.svg"), listOf(svg))
    }

})
