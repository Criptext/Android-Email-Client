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
        val draft = Label(id = 1, color = ColorTypes.GREEN, text = MailFolders.DRAFT)
        val inbox = Label(id = 2, color = ColorTypes.BLUE, text = MailFolders.INBOX)
        val sent = Label(id = 3, color = ColorTypes.RED, text = MailFolders.SENT)
        val trash = Label(id = 4, color = ColorTypes.WHITE, text = MailFolders.TRASH)
        val starred = Label(id = 5, color = ColorTypes.WHITE, text = MailFolders.STARRED)
        val spam = Label(id = 6, color = ColorTypes.GREEN, text = MailFolders.SPAM)

        fun toList() = listOf(draft, inbox, sent, trash, starred, spam)

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