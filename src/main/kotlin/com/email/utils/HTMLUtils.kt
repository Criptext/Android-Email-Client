package com.email.utils

import com.email.utils.WebViewUtils.Companion.collapseScript
import org.jsoup.Jsoup


class HTMLUtils {
    companion object {

        fun html2text(html: String): String {
            return Jsoup.parse(html).text()
        }

        fun changedHeaderHtml(htmlText: String): String {

            val head = "<head><meta name=\"viewport\" content=\"width=device-width\"></head>"
            val closedTag = "</body></html>"
            val changeFontHtml = head + htmlText + collapseScript() + closedTag
            return changeFontHtml
        }
    }
}
