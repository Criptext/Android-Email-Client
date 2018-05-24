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
                MailFolders.IMPORTANT
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
            MailFolders.IMPORTANT -> {
                "IMPORTANT"
            }
            MailFolders.ALL_MAIL -> {
                "ALL MAIL"
            }
        }
    }
}
