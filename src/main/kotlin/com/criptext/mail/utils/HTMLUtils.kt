package com.criptext.mail.utils

import android.text.Html
import androidx.appcompat.app.AppCompatDelegate
import com.criptext.mail.utils.WebViewUtils.Companion.collapseScript
import com.criptext.mail.utils.WebViewUtils.Companion.replaceCIDScript
import com.criptext.mail.utils.file.FileUtils
import org.jsoup.Jsoup
import org.jsoup.safety.Whitelist
import java.lang.StringBuilder
import java.util.*

class HTMLUtils {
    companion object {
        private val whiteList = Whitelist.relaxed()
                .addTags("style", "title", "head")
                .addAttributes(":all", "class", "style")
                .addProtocols("img", "src", "cid", "data")

        fun html2text(html: String): String {

            val body = Jsoup.clean(html, whiteList)
            return Jsoup.parse(body).text()
        }

        fun isHtmlEmpty(html: String): Boolean {
            return Jsoup.parse(html).text().isEmpty() && !html.contains("<img")
        }

        fun sanitizeHtml(html: String):String {
            val body = Jsoup.clean(html, whiteList)
            return Jsoup.parse(body).html()
        }

        fun changedHeaderHtml(htmlText: String, isForward: Boolean): String {
            val style = if(AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES)
            "<style type=\"text/css\">body{color:#FFFFFF;background-color:#34363c;} a{color:#009EFF;}</style>"
            else "<style type=\"text/css\">body{color:#000;background-color:#fff;} </style>"
            val head = "<head>$style<meta name=\"viewport\" content=\"width=device-width\"></head><body>"
            val closedTag = "</body></html>"
            return head + htmlText + replaceCIDScript() + collapseScript(isForward) + closedTag
        }

        fun headerForPrinting(htmlText: String, printData: PrintHeaderInfo, to: String, at: String,
                              message: String, isForward: Boolean): String {
            val head = "<head><meta name=\"viewport\" content=\"width=device-width\"></head><body>"
            val fileUrl = "file:///android_asset/logo.png"
            val printHeader = "<div><img src=\"$fileUrl\" alt=\"Criptext Logo\" style=\"width:120px !important\"></div> <hr> <div><p><b>${printData.subject}</b></br>1 $message</p></div> <hr>" +
                    "<table style=\"width:100%\">\n" +
                    "  <td><b>${printData.fromName}</b> &lt;${printData.fromMail}&gt;</td>\n" +
                    "    <td style=\"text-align:right\">${DateAndTimeUtils.getHoraVerdadera(printData.date.time, at)}</td>\n" +
                    "  </tr>\n" +
                    "  <tr>\n" +
                    "    <td>$to: ${printData.toList}</td>\n" +
                    "  </tr>\n" +
                    "</table> <br>"
            val closedTag = "</body></html>"
            return head + printHeader + htmlText + collapseScript(isForward) + closedTag
        }

        fun headerForPrintingAll(htmlText: List<String>, printData: List<PrintHeaderInfo>, to: String,
                                 at: String, message: String, isForward: Boolean): String {
            val head = "<head><meta name=\"viewport\" content=\"width=device-width\"></head><body>"
            val fileUrl = "file:///android_asset/logo.png"
            val printHeader = "<div><img src=\"$fileUrl\" alt=\"Criptext Logo\" style=\"width:120px !important\"></div> <hr> <div><p><b>${printData[0].subject}</b> </br>${printData.size} $message</p></div> <hr>" +
                    "<table style=\"width:100%\">\n" +
                    "  <td><b>${printData[0].fromName}</b> &lt;${printData[0].fromMail}&gt;</td>\n" +
                    "    <td style=\"text-align:right\">${DateAndTimeUtils.getHoraVerdadera(printData[0].date.time, at)}</td>\n" +
                    "  </tr>\n" +
                    "  <tr>\n" +
                    "    <td>$to: ${printData[0].toList}</td>\n" +
                    "  </tr>\n" +
                    "</table> <br>"
            val closedTag = "</body></html>"
            val concatOtherMails = StringBuilder()
            for(i in 1..(htmlText.size - 1)){
                val secondaryHeader = "<hr>" +
                        "<table style=\"width:100%\">\n" +
                        "  <td><b>${printData[i].fromName}</b> &lt;${printData[i].fromMail}&gt;</td>\n" +
                        "    <td style=\"text-align:right\">${DateAndTimeUtils.getHoraVerdadera(printData[i].date.time, at)}</td>\n" +
                        "  </tr>\n" +
                        "  <tr>\n" +
                        "    <td>$to: ${printData[i].toList}</td>\n" +
                        "  </tr>\n" +
                        "</table> <br>"
                concatOtherMails.append(secondaryHeader + htmlText[i] + "<br>")
            }
            
            return head + printHeader + htmlText[0] + concatOtherMails.toString() + collapseScript(isForward) + closedTag
        }

        fun createEmailPreview(emailBody: String): String {
            val bodyWithoutHTML = HTMLUtils.html2text(emailBody)
            return if (bodyWithoutHTML.length > 300 )
                bodyWithoutHTML.substring(0,300)
            else bodyWithoutHTML
        }

        fun addCriptextFooter(body: String): String{
            val watermarkString = "<div></div><br><br>Sent with <a href=\"https://goo.gl/qW4Aks\" " +
                    "style=\"color: rgb(0,145,255)\">Criptext</a> secure email"
            if(body.contains(watermarkString)) return body
            return body.plus(watermarkString)
        }
    }

    data class PrintHeaderInfo(val subject: String, val toList: String,
                               val fromName: String, val fromMail: String, val date: Date)

}
