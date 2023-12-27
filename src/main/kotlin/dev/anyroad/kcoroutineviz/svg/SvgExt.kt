package dev.anyroad.kcoroutineviz.svg

import com.github.nwillc.ksvg.elements.TITLE

fun title(text: String) = TITLE().apply {
    body = text
}
