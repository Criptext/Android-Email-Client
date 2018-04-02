package com.email.db.typeConverters

import android.arch.persistence.room.TypeConverter
import com.email.db.LabelTextTypes

/**
 * Created by sebas on 3/27/18.
 */

class LabelTextConverter {

    @TypeConverter
    fun getLabelTextType(value: String) : LabelTextTypes  {
        return when(value) {
            "INBOX" -> {
                LabelTextTypes.INBOX
            }
            "STARRED" -> {
                LabelTextTypes.STARRED
            }
            "ARCHIVED" -> {
                LabelTextTypes.ARCHIVED
            }
            "SENT" -> {
                LabelTextTypes.SENT
            }
            "DRAFT" -> {
                LabelTextTypes.DRAFT
            }
            "TRASH" -> {
                LabelTextTypes.TRASH
            }
            "SPAM" -> {
                LabelTextTypes.SPAM
            }
            else ->
                LabelTextTypes.INBOX
        }
    }

    @TypeConverter
    fun parseLabelTextType(value: LabelTextTypes): String {
        return when(value) {
            LabelTextTypes.INBOX -> {
                "INBOX"
            }
            LabelTextTypes.STARRED -> {
                "STARRED"
            }
            LabelTextTypes.ARCHIVED -> {
                "ARCHIVED"
            }
            LabelTextTypes.SENT -> {
                "SENT"
            }
            LabelTextTypes.DRAFT -> {
                "DRAFT"
            }
            LabelTextTypes.TRASH -> {
                "TRASH"
            }
            LabelTextTypes.SPAM -> {
                "SPAM"
            }
        }
    }
}
