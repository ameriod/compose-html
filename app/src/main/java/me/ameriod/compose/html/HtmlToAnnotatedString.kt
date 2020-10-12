package me.ameriod.compose.html

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextIndent
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.nodes.Node
import org.jsoup.nodes.TextNode
import org.jsoup.safety.Whitelist

data class HtmlToAnnotatedString(
    private val html: String,
) {
    private var _annotatedString: AnnotatedString? = null

    fun toAnnotatedString(textStyle: TextStyle): AnnotatedString =
        _annotatedString ?: run {
            val cleanHtml = Jsoup.clean(html, Whitelist.basicWithImages())
            val main = Jsoup.parse(cleanHtml).body()
            val annotatedString = annotatedString {
                appendHtmlNode(main, textStyle)
            }.trim()
            _annotatedString = annotatedString
            annotatedString
        }

    private fun AnnotatedString.Builder.appendHtmlNode(
        node: Node,
        textStyle: TextStyle
    ) {
        when (node) {
            is TextNode -> append(node.text())
            is Element -> when (node.tagName()) {
                Tag.A -> appendLink(node, textStyle)
                Tag.B -> appendBold(node, textStyle)
                Tag.BLOCKQUOTE -> appendBlockquote(node, textStyle)
                Tag.BODY -> appendElementOrChildNode(node, textStyle)
                Tag.BR -> appendBreakLine()
                Tag.CITE -> appendItalic(node, textStyle)
                Tag.CODE -> appendCode(node, textStyle)
                Tag.DL -> appendDescriptionList(node, textStyle)
                Tag.EM -> appendItalic(node, textStyle)
                Tag.I -> appendItalic(node, textStyle)
                Tag.U -> appendUnderline(node, textStyle)
                Tag.P -> appendParagraph(node, textStyle)
                Tag.Q -> appendShortQuotation(node, textStyle)
                Tag.SMALL -> appendSmall(node, textStyle)
                Tag.STRIKE -> appendStrikeThrough(node, textStyle)
                Tag.STRONG -> appendBold(node, textStyle)
                Tag.SPAN -> appendSpan(node, textStyle)
                Tag.SUB -> appendSuperscript(node, textStyle)
                Tag.SUP -> appendSubscript(node, textStyle)
                Tag.OL -> appendOrderedList(node, textStyle)
                Tag.PRE -> appendPre(node, textStyle)
                Tag.UL -> appendUnorderedList(node, textStyle)
                else -> append(node.text())
            }
        }
    }

    fun AnnotatedString.Builder.appendElementOrChildNode(
        element: Element,
        textStyle: TextStyle
    ) {
        element.childNodes()?.takeIf { it.size > 0 }
            ?.forEach {
                appendHtmlNode(it, textStyle)
            } ?: append(element.text())
    }

    private fun AnnotatedString.Builder.appendBold(
        element: Element,
        textStyle: TextStyle
    ) {
        pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
        appendElementOrChildNode(element, textStyle)
        pop()
    }

    private fun AnnotatedString.Builder.appendItalic(
        element: Element,
        textStyle: TextStyle
    ) {
        pushStyle(SpanStyle(fontStyle = FontStyle.Italic))
        appendElementOrChildNode(element, textStyle)
        pop()
    }

    private fun AnnotatedString.Builder.appendUnderline(
        element: Element,
        textStyle: TextStyle
    ) {
        pushStyle(SpanStyle(textDecoration = TextDecoration.Underline))
        appendElementOrChildNode(element, textStyle)
        pop()
    }

    private fun AnnotatedString.Builder.appendStrikeThrough(
        element: Element,
        textStyle: TextStyle
    ) {
        pushStyle(SpanStyle(textDecoration = TextDecoration.LineThrough))
        appendElementOrChildNode(element, textStyle)
        pop()
    }

    private fun AnnotatedString.Builder.appendCode(
        element: Element,
        textStyle: TextStyle
    ) {
        pushStyle(
            SpanStyle(
                fontFamily = FontFamily.Monospace
            )
        )
        appendElementOrChildNode(element, textStyle)
        pop()
    }

    private fun AnnotatedString.Builder.appendPre(
        element: Element,
        textStyle: TextStyle
    ) {
        pushStyle(
            SpanStyle(
                fontFamily = FontFamily.Monospace
            )
        )
        appendElementOrChildNode(element, textStyle)
        pop()
    }

    private fun AnnotatedString.Builder.appendLink(
        element: Element,
        textStyle: TextStyle
    ) {
        pushStyle(
            SpanStyle(
                color = Color.Blue,
                textDecoration = TextDecoration.Underline
            )
        )
        pushStringAnnotation(
            Tag.LINK,
            element.attr(Tag.HREF) ?: ""
        )
        appendElementOrChildNode(element, textStyle)
        pop()
        pop()
    }

    private fun AnnotatedString.Builder.appendUnorderedList(
        element: Element,
        textStyle: TextStyle
    ) {
        element.allElements.forEach { child ->
            if (child.tagName() == Tag.LI) {
                pushStyle(
                    ParagraphStyle(
                        textIndent = TextIndent(
                            restLine = textStyle.toTextIndentSize()
                        )
                    )
                )
                append("• ")
                appendElementOrChildNode(child, textStyle)
                pop()
            }
        }
    }

    private fun AnnotatedString.Builder.appendOrderedList(
        element: Element,
        textStyle: TextStyle
    ) {
        element.allElements.forEachIndexed { index, child ->
            if (child.tagName() == Tag.LI) {
                pushStyle(
                    ParagraphStyle(
                        textIndent = TextIndent(
                            restLine = textStyle.toTextIndentSize()
                        )
                    )
                )
                append("${index}. ")
                appendElementOrChildNode(child, textStyle)
                pop()
            }
        }
    }

    private fun AnnotatedString.Builder.appendDescriptionList(
        element: Element,
        textStyle: TextStyle
    ) {
        element.allElements.forEach { child ->
            when (child.tagName()) {
                Tag.DT -> {
                    pushStyle(
                        ParagraphStyle(
                            textIndent = TextIndent(
                                restLine = textStyle.toTextIndentSize()
                            )
                        )
                    )
                    appendElementOrChildNode(child, textStyle)
                    pop()
                }
                Tag.DD -> {
                    pushStyle(
                        ParagraphStyle(
                            textIndent = TextIndent(
                                firstLine = textStyle.toTextIndentSize().times(2),
                                restLine = textStyle.toTextIndentSize().times(3)
                            )
                        )
                    )
                    appendElementOrChildNode(child, textStyle)
                    pop()
                }
            }
        }
    }

    private fun AnnotatedString.Builder.appendParagraph(
        element: Element,
        textStyle: TextStyle
    ) {
        // Make sure there is actually stuff in the paragraph tag
        if (!element.text().trim().isBlank()) {
            appendElementOrChildNode(element, textStyle)
            append("\n\n")
        }
    }

    private fun AnnotatedString.Builder.appendBreakLine() {
        append("\n")
    }

    private fun AnnotatedString.Builder.appendShortQuotation(
        element: Element,
        textStyle: TextStyle
    ) {
        append("\"")
        appendElementOrChildNode(element, textStyle)
        append("\"")
    }

    private fun AnnotatedString.Builder.appendBlockquote(
        element: Element,
        textStyle: TextStyle
    ) {
        pushStyle(
            ParagraphStyle(
                textIndent = TextIndent(
                    firstLine = textStyle.fontSize,
                    restLine = textStyle.fontSize
                )
            )
        )
        appendElementOrChildNode(element, textStyle)
        pop()
    }

    private fun AnnotatedString.Builder.appendSubscript(
        element: Element,
        textStyle: TextStyle
    ) {
        pushStyle(
            SpanStyle(
                baselineShift = BaselineShift.Subscript,
                fontSize = textStyle.fontSize.times(BASELINE_SHIFT_SCALE_FACTOR)
            )
        )
        appendElementOrChildNode(element, textStyle)
        pop()
    }

    private fun AnnotatedString.Builder.appendSuperscript(
        element: Element,
        textStyle: TextStyle
    ) {
        pushStyle(
            SpanStyle(
                baselineShift = BaselineShift.Superscript,
                fontSize = textStyle.fontSize.times(BASELINE_SHIFT_SCALE_FACTOR)
            )
        )
        appendElementOrChildNode(element, textStyle)
        pop()
    }

    private fun AnnotatedString.Builder.appendSpan(
        element: Element,
        textStyle: TextStyle
    ) {
        appendElementOrChildNode(element, textStyle)
    }

    private fun AnnotatedString.Builder.appendSmall(
        element: Element,
        textStyle: TextStyle
    ) {
        pushStyle(
            SpanStyle(
                fontSize = textStyle.fontSize.times(SMALL_SCALE_FACTOR)
            )
        )
        appendElementOrChildNode(element, textStyle)
        pop()
    }

    object Tag {
        const val A = "a"
        const val B = "b"
        const val BLOCKQUOTE = "blockquote"
        const val BR = "br"
        const val CITE = "cite"
        const val CODE = "code"
        const val DD = "dd"
        const val DL = "dl"
        const val DT = "dt"
        const val EM = "em"
        const val I = "i"
        const val LI = "li"
        const val OL = "ol"
        const val P = "p"
        const val PRE = "pre"
        const val Q = "q"
        const val SMALL = "small"
        const val SPAN = "span"
        const val STRIKE = "strike"
        const val STRONG = "strong"
        const val SUB = "sub"
        const val SUP = "sup"
        const val U = "u"
        const val UL = "ul"
        const val IMG = "img"
        const val BODY = "body"
        const val HREF = "href"
        const val LINK = "link"
    }

    companion object {
        private const val BASELINE_SHIFT_SCALE_FACTOR = 0.66
        private const val SMALL_SCALE_FACTOR = 0.75
    }
}

private fun AnnotatedString.trim(): AnnotatedString {
    var startIndex = 0
    var endIndex = length - 1
    var startFound = false

    while (startIndex <= endIndex) {
        val index = if (!startFound) startIndex else endIndex
        val match = subSequence(index, index + 1).text.isBlank()

        if (!startFound) {
            if (!match)
                startFound = true
            else
                startIndex += 1
        } else {
            if (!match)
                break
            else
                endIndex -= 1
        }
    }

    return subSequence(startIndex, endIndex + 1)
}

private fun TextStyle.toTextIndentSize() = fontSize / 2

fun String.htmlToSpannableString(textStyle: TextStyle): AnnotatedString =
    HtmlToAnnotatedString(this).toAnnotatedString(textStyle)