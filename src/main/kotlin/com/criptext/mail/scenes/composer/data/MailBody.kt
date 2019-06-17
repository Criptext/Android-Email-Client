package com.criptext.mail.scenes.composer.data

import com.criptext.mail.db.models.FullEmail
import com.criptext.mail.utils.DateAndTimeUtils
import com.criptext.mail.utils.mailtemplates.FWMailTemplate
import com.criptext.mail.utils.mailtemplates.REMailTemplate
import java.text.SimpleDateFormat


/**
 * Created by gabriel on 6/1/17.
 */

data class MailBody(val htmlForImage: String, val htmlForPlainText: String)  {

    companion object {

        private const val replyBodyContainerTagStart = """<div></div><br><div class="criptext_quote">"""
        private const val replyBodyContainerTagEnd = "</div>"
        private const val blockQuoteStart = """<blockquote style="margin:0 0 0 .8ex;border-left:1px #0091ff solid;padding-left:1ex">"""
        private const val blockQuoteEnd = "</blockquote>"

        fun createNewTemplateMessageBody(template: String, signature: String): String {
            val builder = StringBuilder(template)
            if(signature.isNotEmpty()) {
                builder.append("<div></div><br><br>")
                builder.append(signature)
            }
            return builder.toString()
        }

        fun createNewForwardMessageBody(fullEmail: FullEmail, template: FWMailTemplate, signature: String): String {

            val builder = StringBuilder("")
            builder.append("<br/><br/>----------${template.message}----------<br/>")
            builder.append("${template.from} " +
                    "${fullEmail.email.fromAddress.replace("<", "&#60;")
                            .replace(">", "&#62;")}<br/>"
            )
            builder.append("${template.date} ${DateAndTimeUtils.getHoraVerdadera(fullEmail.email.date.time,
                    template.at)}<br/>")
            builder.append("${template.subject} ${fullEmail.email.subject}<br/>")
            val to = fullEmail.to + fullEmail.cc
            if(to.isNotEmpty())
                builder.append("${template.to} ${to.joinToString { it.name.plus(" &#60;${it.email}&#62;") }}<br/>")
            builder.append(replyBodyContainerTagStart)
            builder.append(blockQuoteStart)
            builder.append(fullEmail.email.content)
            builder.append(blockQuoteEnd)
            builder.append(replyBodyContainerTagEnd)
            if(signature.isNotEmpty()) {
                builder.append("<div></div><br><br>")
                builder.append(signature)
            }
            return builder.toString()
        }

        fun createNewReplyMessageBody(originMessageHtml: String, date: Long, template: REMailTemplate, senderName: String, signature: String): String {
            val formattedDate = DateAndTimeUtils.getHoraVerdadera(date, template.at)
            val dateString = "<br/>${template.on} $formattedDate $senderName ${template.wrote}<br/>"

            val builder = StringBuilder("")
            builder.append(replyBodyContainerTagStart)
            builder.append(dateString)
            builder.append(blockQuoteStart)
            builder.append(originMessageHtml)
            builder.append(blockQuoteEnd)
            builder.append(replyBodyContainerTagEnd)
            if(signature.isNotEmpty()) {
                builder.append("<div></div><br><br>")
                builder.append(signature)
            }
            return builder.toString()
        }

    }

}