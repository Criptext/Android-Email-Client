package com.email.db.models

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import com.email.db.LabelTypes
import com.email.db.MailFolders

/**
 * Created by sebas on 1/24/18.
 */

@Entity(tableName = "label")
data class Label (
        @PrimaryKey(autoGenerate = true)
        var id: Long,

        @ColumnInfo(name = "color")
        var color: String,

        @ColumnInfo(name = "text")
        var text: MailFolders,

        @ColumnInfo(name = "type")
        var type: LabelTypes,

        @ColumnInfo(name = "visible")
        var visible: Boolean
) {

    companion object {
        val defaultItems = DefaultItems()
    }

    class DefaultItems {

        val inbox = Label(id = 1, color = "0091ff", text = MailFolders.INBOX,
                type = LabelTypes.SYSTEM, visible = true)
        val spam = Label(id = 2, color = "f1453d", text = MailFolders.SPAM,
                type = LabelTypes.SYSTEM, visible = true)
        val sent = Label(id = 3, color = "a0d06e", text = MailFolders.SENT,
                type = LabelTypes.SYSTEM, visible = true)
        val important = Label(id = 4, color = "ffbb63", text = MailFolders.IMPORTANT,
                type = LabelTypes.SYSTEM, visible = true)
        val starred = Label(id = 5, color = "ffe137", text = MailFolders.STARRED,
                type = LabelTypes.SYSTEM, visible = true)
        val draft = Label(id = 6, color = "626262", text = MailFolders.DRAFT,
                type = LabelTypes.SYSTEM, visible = true)
        val trash = Label(id = 7, color = "ed63ff", text = MailFolders.TRASH,
                type = LabelTypes.SYSTEM, visible = true)

        fun toList() = listOf(draft, inbox, sent, trash, starred, spam, important)

        fun rejectedLabelsByMailbox(label: Label?): List<Label> =
            when (label) {
            sent,
            inbox,
            starred -> listOf(spam, trash)
            spam -> listOf(trash)
            trash -> listOf(spam)
            else -> emptyList()
        }
    }
}