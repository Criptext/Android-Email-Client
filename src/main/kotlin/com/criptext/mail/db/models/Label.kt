package com.criptext.mail.db.models

import androidx.annotation.NonNull
import androidx.room.*
import com.criptext.mail.db.LabelTypes
import org.json.JSONObject

/**
 * Created by sebas on 1/24/18.
 */

@Entity(tableName = "label",
        indices = [(Index(value = "text", unique = true)),(Index(value = "uuid", unique = true))],
        foreignKeys = [ForeignKey(entity = Account::class,
                parentColumns = ["id"],
                onDelete = ForeignKey.CASCADE,
                childColumns = ["accountId"])])
data class Label (
        @PrimaryKey(autoGenerate = true)
        var id: Long,

        @ColumnInfo(name = "uuid")
        var uuid: String,

        @ColumnInfo(name = "color")
        var color: String,

        @ColumnInfo(name = "text")
        var text: String,

        @ColumnInfo(name = "type")
        var type: LabelTypes,

        @ColumnInfo(name = "visible")
        var visible: Boolean,

        @ColumnInfo(name = "accountId")
        var accountId : Long?
) {

    companion object {
        val defaultItems = DefaultItems()

        const val LABEL_SENT = "Sent"
        const val LABEL_INBOX = "Inbox"
        const val LABEL_TRASH = "Trash"
        const val LABEL_ALL_MAIL = "All Mail"
        const val LABEL_DRAFT = "Draft"
        const val LABEL_SPAM = "Spam"
        const val LABEL_STARRED = "Starred"

        fun getLabelIdWildcard(labelName: String, labels: List<Label>): String{
            return if(labelName == Label.LABEL_ALL_MAIL) "%" else
                "%${labels.findLast {
                    label ->label.text == labelName
                }?.id}%"
        }

        fun fromJSON(jsonString: String, accountId: Long): Label {
            val json = JSONObject(jsonString)
            val id = json.getLong("id")
            val color = json.getString("color")
            val text = json.getString("text")
            val type = LabelTypes.valueOf(json.getString("type").toUpperCase())
            val visible = json.getBoolean("visible")
            val uuid = json.getString("uuid")
            val finalAccountId = if(type == LabelTypes.SYSTEM) null else accountId
            return Label(
                id =  id,
                color = color,
                text = text,
                type = type,
                visible = visible,
                uuid = uuid,
                accountId = finalAccountId
            )
        }

        fun toJSON(label: Label): JSONObject {
            val json = JSONObject()
            json.put("id", label.id)
            json.put("color", label.color)
            json.put("text", label.text)
            json.put("type", label.type)
            json.put("visible", label.visible)
            json.put("uuid", label.uuid)
            return json
        }
    }

    class DefaultItems {

        val inbox = Label(id = 1, color = "0091ff", text = LABEL_INBOX,
                type = LabelTypes.SYSTEM, visible = true, uuid = "00000000-0000-0000-0000-000000000001",
                accountId = null)
        val spam = Label(id = 2, color = "f1453d", text = LABEL_SPAM,
                type = LabelTypes.SYSTEM, visible = true, uuid = "00000000-0000-0000-0000-000000000002",
                accountId = null)
        val sent = Label(id = 3, color = "a0d06e", text = LABEL_SENT,
                type = LabelTypes.SYSTEM, visible = true, uuid = "00000000-0000-0000-0000-000000000003",
                accountId = null)
        val starred = Label(id = 5, color = "ffe137", text = LABEL_STARRED,
                type = LabelTypes.SYSTEM, visible = true, uuid = "00000000-0000-0000-0000-000000000005",
                accountId = null)
        val draft = Label(id = 6, color = "626262", text = LABEL_DRAFT,
                type = LabelTypes.SYSTEM, visible = true, uuid = "00000000-0000-0000-0000-000000000006",
                accountId = null)
        val trash = Label(id = 7, color = "ed63ff", text = LABEL_TRASH,
                type = LabelTypes.SYSTEM, visible = true, uuid = "00000000-0000-0000-0000-000000000007",
                accountId = null)

        fun toList() = listOf(draft, inbox, sent, trash, starred, spam)

        fun rejectedLabelsByMailbox(label: Label?): List<Label> =
            when (label) {
            sent,
            inbox,
            starred,
            draft -> listOf(spam, trash)
            spam -> listOf(trash)
            trash -> listOf(spam)
            else -> emptyList()
        }

        fun rejectedLabelsByFolder(folder: String): List<Label> {
            return when(folder) {
                LABEL_SENT,
                LABEL_INBOX,
                LABEL_STARRED,
                LABEL_DRAFT,
                LABEL_ALL_MAIL -> listOf(spam, trash)
                LABEL_SPAM -> listOf(trash)
                LABEL_TRASH -> listOf(spam)
                else ->  emptyList()
            }
        }
    }
}