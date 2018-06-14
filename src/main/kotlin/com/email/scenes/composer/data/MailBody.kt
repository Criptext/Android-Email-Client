package com.email.scenes.composer.data

import java.text.SimpleDateFormat


/**
 * Created by gabriel on 6/1/17.
 */

data class MailBody(val htmlForImage: String, val htmlForPlainText: String)  {

    companion object {

        private const val replyBodyContainerTagStart = """<div id="criptext55tu7s3"></div>"""
        private const val replyBodyContainerTagEnd = """<div id="criptexth8h5h8f"></div>"""

        fun createNewTemplateMessageBody(template: String, signature: String): String {
            val builder = StringBuilder(template)
            if(signature.isNotEmpty()) {
                builder.append("<div></div><br>")
                builder.append(signature)
            }
            return builder.toString()
        }

        fun createNewForwardMessageBody(originMessageHtml: String, signature: String): String {
            val blockQuoteStart = """<blockquote class="gmail_quote" style="margin:0 0 0 .8ex;border-left:1px #ccc solid;padding-left:1ex">"""
            val blockQuoteEnd = "</blockquote>"
            val builder = StringBuilder("")
            builder.append(replyBodyContainerTagStart)
            builder.append("<br/><br/>Begin forwarded message:<br/>")
            builder.append(blockQuoteStart)
            builder.append(originMessageHtml)
            builder.append(blockQuoteEnd)
            builder.append("<div></div><br>")
            builder.append(signature)
            builder.append(replyBodyContainerTagEnd)

            return builder.toString()
        }

        fun createNewReplyMessageBody(originMessageHtml: String, date: Long, senderName: String, signature: String): String {
            val blockQuoteStart = """<blockquote class="gmail_quote" style="margin:0 0 0 .8ex;border-left:1px #ccc solid;padding-left:1ex">"""
            val blockQuoteEnd = "</blockquote>"
            val formattedDate = SimpleDateFormat("MM/dd/yyyy").format(date)
            val dateString = "<br/><br/>on $formattedDate $senderName wrote:<br/>"

            val builder = StringBuilder("")
            builder.append(replyBodyContainerTagStart)
            builder.append(dateString)
            builder.append(blockQuoteStart)
            builder.append(originMessageHtml)
            builder.append(blockQuoteEnd)
            builder.append("<div></div><br>")
            builder.append(signature)
            builder.append(replyBodyContainerTagEnd)

            return builder.toString()
        }

    }

}