package com.criptext.mail.scenes.composer.data

import android.content.Context
import com.criptext.mail.api.toList
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.db.models.Label
import com.criptext.mail.email_preview.EmailPreview
import com.criptext.mail.utils.mailtemplates.CriptextMailTemplate
import com.criptext.mail.utils.mailtemplates.FWMailTemplate
import com.criptext.mail.utils.mailtemplates.REMailTemplate
import com.criptext.mail.utils.mailtemplates.SupportMailTemplate
import org.json.JSONArray
import org.json.JSONObject

/**
 * Created by sebas on 3/27/18.
 */

sealed class ComposerType {
    class Empty : ComposerType() {
        override fun equals(other: Any?): Boolean = other is Empty
    }
    data class Draft(val draftId: Long, val currentLabel: Label,
                     val threadPreview: EmailPreview): ComposerType()
    data class Reply(val originalId: Long, val currentLabel: Label,
                     val threadPreview: EmailPreview, val template: REMailTemplate): ComposerType()
    data class ReplyAll(val originalId: Long, val currentLabel: Label,
                        val threadPreview: EmailPreview, val template: REMailTemplate): ComposerType()
    data class Forward(val originalId: Long, val currentLabel: Label,
                       val threadPreview: EmailPreview, val template: FWMailTemplate): ComposerType()
    data class Support(val template: SupportMailTemplate): ComposerType()
    data class MailTo(val to: String): ComposerType()
    data class Send(val files: List<Pair<String, Long>>): ComposerType()

    companion object {
        fun fromJSON(jsonString: String, context: Context) : ComposerType {
            val json = JSONObject(jsonString)
            val activeAccount = ActiveAccount.loadFromStorage(context)!!
            return when(json.getInt("type")){
                Type.DRAFT.ordinal -> {
                    Draft(draftId = json.getLong("draftId"),
                            threadPreview = EmailPreview.emailPreviewFromJSON(json.getString("threadPreview")!!),
                            currentLabel = Label.fromJSON(json.getString("currentLabel")!!, activeAccount.id))
                }
                Type.REPLY.ordinal -> {
                    Reply(originalId = json.getLong("originalId"),
                            threadPreview = EmailPreview.emailPreviewFromJSON(json.getString("threadPreview")!!),
                            currentLabel = Label.fromJSON(json.getString("currentLabel")!!, activeAccount.id),
                            template = REMailTemplate(context))
                }
                Type.REPLYALL.ordinal -> {
                    ReplyAll(originalId = json.getLong("originalId"),
                            threadPreview = EmailPreview.emailPreviewFromJSON(json.getString("threadPreview")!!),
                            currentLabel = Label.fromJSON(json.getString("currentLabel")!!, activeAccount.id),
                            template = REMailTemplate(context))
                }
                Type.FORWARD.ordinal -> {
                    Forward(originalId = json.getLong("draftId"),
                            threadPreview = EmailPreview.emailPreviewFromJSON(json.getString("threadPreview")!!),
                            currentLabel = Label.fromJSON(json.getString("currentLabel")!!, activeAccount.id),
                            template = FWMailTemplate(context))
                }
                Type.SUPPORT.ordinal -> {
                    Support(SupportMailTemplate(context))
                }
                Type.MAILTO.ordinal -> {
                    MailTo(to = json.getString("to"))
                }
                else -> Empty()
            }
        }

        fun toJSON(c: ComposerType) : String{
            val json = JSONObject()
            when(c){
                is Draft -> {
                    json.put("type", Type.DRAFT.ordinal)
                    json.put("draftId", c.draftId)
                    json.put("currentLabel", Label.toJSON(c.currentLabel))
                    json.put("threadPreview", EmailPreview.emailPreviewToJSON(c.threadPreview))
                }
                is Empty -> json.put("type", Type.EMPTY.ordinal)
                is Reply -> {
                    json.put("type", Type.REPLY.ordinal)
                    json.put("originalId", c.originalId)
                    json.put("currentLabel", Label.toJSON(c.currentLabel))
                    json.put("threadPreview", EmailPreview.emailPreviewToJSON(c.threadPreview))
                }
                is ReplyAll -> {
                    json.put("type", Type.REPLYALL.ordinal)
                    json.put("originalId", c.originalId)
                    json.put("currentLabel", Label.toJSON(c.currentLabel))
                    json.put("threadPreview", EmailPreview.emailPreviewToJSON(c.threadPreview))
                }
                is Forward -> {
                    json.put("type", Type.FORWARD.ordinal)
                    json.put("originalId", c.originalId)
                    json.put("currentLabel", Label.toJSON(c.currentLabel))
                    json.put("threadPreview", EmailPreview.emailPreviewToJSON(c.threadPreview))
                }
                is Support -> json.put("type", Type.SUPPORT.ordinal)
                is MailTo -> {
                    json.put("type", Type.MAILTO.ordinal)
                    json.put("to", c.to)
                }
            }
            return json.toString()
        }

        private enum class Type {
            EMPTY, DRAFT, REPLY, REPLYALL, FORWARD, SUPPORT, MAILTO, SEND;
        }
    }
}