package com.criptext.mail.db.models

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.Index
import android.arch.persistence.room.PrimaryKey
import com.criptext.mail.db.LabelTypes
import org.json.JSONObject

/**
 * Created by sebas on 1/24/18.
 */

@Entity(tableName = "label",
        indices = [(Index(value = "text", unique = true))])
data class Label (
        @PrimaryKey(autoGenerate = true)
        var id: Long,

        @ColumnInfo(name = "color")
        var color: String,

        @ColumnInfo(name = "text")
        var text: String,

        @ColumnInfo(name = "type")
        var type: LabelTypes,

        @ColumnInfo(name = "visible")
        var visible: Boolean
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

        fun fromJSON(jsonString: String): Label {
            val json = JSONObject(jsonString)
            val id = json.getLong("id")
            val color = json.getString("color")
            val text = json.getString("text")
            val type = json.getString("type")
            val visible = json.getBoolean("visible")
            return Label(
                id =  id,
                color = color,
                text = text,
                type = LabelTypes.valueOf(type.toUpperCase()),
                visible = visible
            )
        }
    }

    class DefaultItems {

        val inbox = Label(id = 1, color = "0091ff", text = LABEL_INBOX,
                type = LabelTypes.SYSTEM, visible = true)
        val spam = Label(id = 2, color = "f1453d", text = LABEL_SPAM,
                type = LabelTypes.SYSTEM, visible = true)
        val sent = Label(id = 3, color = "a0d06e", text = LABEL_SENT,
                type = LabelTypes.SYSTEM, visible = true)
        val starred = Label(id = 5, color = "ffe137", text = LABEL_STARRED,
                type = LabelTypes.SYSTEM, visible = true)
        val draft = Label(id = 6, color = "626262", text = LABEL_DRAFT,
                type = LabelTypes.SYSTEM, visible = true)
        val trash = Label(id = 7, color = "ed63ff", text = LABEL_TRASH,
                type = LabelTypes.SYSTEM, visible = true)

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
                LABEL_ALL_MAIL -> listOf(spam, trash)
                LABEL_SPAM -> listOf(trash)
                LABEL_TRASH -> listOf(spam)
                else ->  emptyList()
            }
        }
    }
}