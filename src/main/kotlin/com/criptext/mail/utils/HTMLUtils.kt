package com.criptext.mail.utils

import com.criptext.mail.scenes.composer.data.ComposerAttachment
import com.criptext.mail.utils.WebViewUtils.Companion.collapseScript
import com.criptext.mail.utils.file.FileUtils
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

        fun createAttchmentForUnencryptedEmailToNonCriptextUsers(attachment: ComposerAttachment,
                                                                 encodedParams: String,
                                                                 mimeTypeSource: String): String{
            return """
                <div style="margin-top: 6px; float: left;">
                  <a style="cursor: pointer; text-decoration: none;" href="https://services.criptext.com/downloader/$encodedParams?e=1">
                    <div style="align-items: center; border: 1px solid #e7e5e5; border-radius: 6px; display: flex; height: 20px; margin-right: 20px; padding: 10px; position: relative; width: 236px;">
                      <div style="position: relative;">
                        <div style="align-items: center; border-radius: 4px; display: flex; height: 22px; width: 22px;">
                          <img src="https://cdn.criptext.com/External-Email/imgs/$mimeTypeSource.png" style="height: 100%; width: 100%;"/>
                        </div>
                      </div>
                      <div style="padding-top: 1px; display: flex; flex-grow: 1; height: 100%; margin-left: 10px; width: calc(100% - 32px);">
                        <span style="color: black; padding-top: 1px; width: 160px; flex-grow: 1; font-size: 14px; font-weight: 700; overflow: hidden; padding-right: 5px; text-overflow: ellipsis; white-space: nowrap;">${FileUtils.getName(attachment.filepath)}</span>
                        <span style="color: #9b9b9b; flex-grow: 0; font-size: 13px; white-space: nowrap; line-height: 21px;">${FileUtils.readableFileSize(attachment.size)}</span>
                      </div>
                    </div>
                  </a>
                </div>
                """
        }
    }
}
