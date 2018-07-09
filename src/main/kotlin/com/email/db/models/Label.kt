package com.email.db.models

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import com.email.SecureEmail
import com.email.db.LabelTypes

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
        var text: String,

        @ColumnInfo(name = "type")
        var type: LabelTypes,

        @ColumnInfo(name = "visible")
        var visible: Boolean
) {

    companion object {
        val defaultItems = DefaultItems()
    }

    class DefaultItems {

        val inbox = Label(id = 1, color = "0091ff", text = SecureEmail.LABEL_INBOX,
                type = LabelTypes.SYSTEM, visible = true)
        val spam = Label(id = 2, color = "f1453d", text = SecureEmail.LABEL_SPAM,
                type = LabelTypes.SYSTEM, visible = true)
        val sent = Label(id = 3, color = "a0d06e", text = SecureEmail.LABEL_SENT,
                type = LabelTypes.SYSTEM, visible = true)
        val starred = Label(id = 5, color = "ffe137", text = SecureEmail.LABEL_STARRED,
                type = LabelTypes.SYSTEM, visible = true)
        val draft = Label(id = 6, color = "626262", text = SecureEmail.LABEL_DRAFT,
                type = LabelTypes.SYSTEM, visible = true)
        val trash = Label(id = 7, color = "ed63ff", text = SecureEmail.LABEL_TRASH,
                type = LabelTypes.SYSTEM, visible = true)

        fun toList() = listOf(draft, inbox, sent, trash, starred, spam)

        fun rejectedLabelsByMailbox(label: Label?): List<Label> =
            when (label) {
            sent,
            inbox,
            starred -> listOf(spam, trash)
            spam -> listOf(trash)
            trash -> listOf(spam)
            else -> emptyList()
        }

        fun rejectedLabelsByFolder(folder: String): List<Label> {
            return when(folder) {
                SecureEmail.LABEL_SENT,
                SecureEmail.LABEL_INBOX,
                SecureEmail.LABEL_STARRED,
                SecureEmail.LABEL_ALL_MAIL -> listOf(spam, trash)
                SecureEmail.LABEL_SPAM -> listOf(trash)
                SecureEmail.LABEL_TRASH -> listOf(spam)
                else ->  emptyList()
            }
        }
    }
}