package com.criptext.mail.utils

import com.criptext.mail.utils.WebViewUtils.Companion.collapseScript
import org.jsoup.Jsoup

class HTMLUtils {
    companion object {

        fun html2text(html: String): String {
            return Jsoup.parse(html).text()
        }

        fun changedHeaderHtml(htmlText: String): String {

            val head = "<head><meta name=\"viewport\" content=\"width=device-width\"></head><body>"
            val closedTag = "</body></html>"
            return head + htmlText + collapseScript() + closedTag
        }

        fun createEmailPreview(emailBody: String): String {
            val bodyWithoutHTML = HTMLUtils.html2text(emailBody)
            return if (bodyWithoutHTML.length > 100 )
                bodyWithoutHTML.substring(0,100)
            else bodyWithoutHTML
        }
    }
}
