package com.email.db.typeConverters

import android.arch.persistence.room.TypeConverter
import com.email.db.MailFolders

/**
 * Created by sebas on 3/27/18.
 */

class LabelTextConverter {

    @TypeConverter
    fun getLabelTextType(value: String) : MailFolders {
        return when(value) {
            "INBOX" -> {
                MailFolders.INBOX
            }
            "STARRED" -> {
                MailFolders.STARRED
            }
            "ARCHIVED" -> {
                MailFolders.ARCHIVED
            }
            "SENT" -> {
                MailFolders.SENT
            }
            "DRAFT" -> {
                MailFolders.DRAFT
            }
            "TRASH" -> {
                MailFolders.TRASH
            }
            "SPAM" -> {
                MailFolders.SPAM
            }
            else ->
                MailFolders.INBOX
        }
    }

    @TypeConverter
    fun parseLabelTextType(value: MailFolders): String {
        return when(value) {
            MailFolders.INBOX -> {
                "INBOX"
            }
            MailFolders.STARRED -> {
                "STARRED"
            }
            MailFolders.ARCHIVED -> {
                "ARCHIVED"
            }
            MailFolders.SENT -> {
                "SENT"
            }
            MailFolders.DRAFT -> {
                "DRAFT"
            }
            MailFolders.TRASH -> {
                "TRASH"
            }
            MailFolders.SPAM -> {
                "SPAM"
            }
        }
    }
}
