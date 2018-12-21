package com.criptext.mail.utils

import android.support.v7.app.AppCompatDelegate
import android.text.Html
import com.criptext.mail.utils.WebViewUtils.Companion.collapseScript
import com.criptext.mail.utils.file.FileUtils
import org.jsoup.Jsoup
import org.jsoup.safety.Whitelist

class HTMLUtils {
    companion object {
        private val whiteList = Whitelist.relaxed()
                .addTags("style", "title", "head")
                .addAttributes(":all", "class", "style")

        fun html2text(html: String): String {

            val body = Jsoup.clean(html, whiteList)
            return Jsoup.parse(body).text()
        }

        fun sanitizeHtml(html: String):String {
            val body = Jsoup.clean(html, whiteList)
            return Jsoup.parse(body).html()
        }

        fun changedHeaderHtml(htmlText: String): String {
            val style = if(AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES)
            "<style type=\"text/css\">body{color:#FFFFFF;background-color:#373a45;}</style>"
            else ""
            val head = "<head>$style<meta name=\"viewport\" content=\"width=device-width\"></head><body>"
            val closedTag = "</body></html>"
            return head + htmlText + collapseScript() + closedTag
        }

        fun createEmailPreview(emailBody: String): String {
            val bodyWithoutHTML = HTMLUtils.html2text(emailBody)
            return if (bodyWithoutHTML.length > 300 )
                bodyWithoutHTML.substring(0,300)
            else bodyWithoutHTML
        }

        fun createAttchmentForUnencryptedEmailToNonCriptextUsers(attachmentName: String,
                                                                 attachmentSize: Long,
                                                                 encodedParams: String,
                                                                 mimeTypeSource: String): String{
            return """
                <div style="margin-top: 6px; float: left;">
                  <a style="cursor: pointer; text-decoration: none;" href="https://services.criptext.com/downloader/$encodedParams">
                    <div style="align-items: center; border: 1px solid #e7e5e5; border-radius: 6px; display: flex; height: 20px; margin-right: 20px; padding: 10px; position: relative; width: 236px;">
                      <div style="position: relative;">
                        <div style="align-items: center; border-radius: 4px; display: flex; height: 22px; width: 22px;">
                          <img src="https://cdn.criptext.com/External-Email/imgs/$mimeTypeSource.png" style="height: 100%; width: 100%;"/>
                        </div>
                      </div>
                      <div style="padding-top: 1px; display: flex; flex-grow: 1; height: 100%; margin-left: 10px; width: calc(100% - 32px);">
                        <span style="color: black; padding-top: 1px; width: 160px; flex-grow: 1; font-size: 14px; font-weight: 700; overflow: hidden; padding-right: 5px; text-overflow: ellipsis; white-space: nowrap;">$attachmentName</span>
                        <span style="color: #9b9b9b; flex-grow: 0; font-size: 13px; white-space: nowrap; line-height: 21px;">${FileUtils.readableFileSize(attachmentSize, 1024)}</span>
                      </div>
                    </div>
                  </a>
                </div>
                """
        }

        fun getMimeTypeSourceForUnencryptedEmail(mimeType: String):String{
            return when {
                mimeType.contains("image") -> "fileimage"
                mimeType.contains("powerpoint") || mimeType.contains("presentation") -> "fileppt"
                mimeType.contains("excel") || mimeType.contains("sheet") -> "fileexcel"
                mimeType.contains("pdf") -> "filepdf"
                mimeType.contains("word") -> "fileword"
                mimeType.contains("audio") -> "fileaudio"
                mimeType.contains("video") -> "filevideo"
                mimeType.contains("zip") -> "filezip"
                else -> "filedefault"
            }
        }

        fun addCriptextFooter(body: String): String{
            val watermarkString = "<div></div><br><br>Sent with <a href=\"https://goo.gl/qW4Aks\" " +
                    "style=\"color: rgb(0,145,255)\">Criptext</a> secure email"
            if(body.contains(watermarkString)) return body
            return body.plus(watermarkString)
        }
    }



}
