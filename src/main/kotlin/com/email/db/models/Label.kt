package com.email.db.models

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import com.email.db.ColorTypes
import com.email.db.MailFolders

/**
 * Created by sebas on 1/24/18.
 */

@Entity(tableName = "label")
data class Label (
        @PrimaryKey(autoGenerate = true)
        var id: Long,

        @ColumnInfo(name = "color")
        var color: ColorTypes,

        @ColumnInfo(name = "text")
        var text: MailFolders
) {

    companion object {
        val defaultItems = DefaultItems()
    }

    class DefaultItems {
        val inbox = Label(id = 1, color = ColorTypes.BLUE, text = MailFolders.INBOX)
        val spam = Label(id = 2, color = ColorTypes.RED, text = MailFolders.SPAM)
        val sent = Label(id = 3, color = ColorTypes.GREEN, text = MailFolders.SENT)
        val important = Label(id = 4, color = ColorTypes.ORANGE, text = MailFolders.IMPORTANT)
        val starred = Label(id = 5, color = ColorTypes.YELLOW, text = MailFolders.STARRED)
        val draft = Label(id = 6, color = ColorTypes.GRAY, text = MailFolders.DRAFT)
        val trash = Label(id = 7, color = ColorTypes.PURPLE, text = MailFolders.TRASH)

        fun toList() = listOf(draft, inbox, sent, trash, starred, spam, important)

        fun rejectedLabelsByMailbox(label: Label): List<Label> =
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