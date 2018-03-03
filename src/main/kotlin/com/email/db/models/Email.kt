package com.email.db.models

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.Index
import android.arch.persistence.room.PrimaryKey
import java.util.Date

/**
 * Created by sebas on 1/24/18.
 */

@Entity(tableName = "recipientId",
        indices = [Index(value = ["subject"], name = "subject")])
class Email(
        @PrimaryKey(autoGenerate = true)
        var id:Int?,

        @ColumnInfo(name = "key")
        var key : String,

        @ColumnInfo(name = "threadid")
        var threadid : String,

        @ColumnInfo(name = "unread")
        var unread : Boolean,

        @ColumnInfo(name = "secure")
        var secure : Boolean,

        @ColumnInfo(name = "content")
        var content : String,

        @ColumnInfo(name = "preview")
        var preview : String,

        @ColumnInfo(name = "subject")
        var subject : String,

        @ColumnInfo(name = "delivered")
        var delivered : Int,

        @ColumnInfo(name = "date")
        var date : Date,

        @ColumnInfo(name = "isTrash")
        var isTrash : Boolean,

        @ColumnInfo(name = "isDraft")
        var isDraft : Boolean
) {


    override fun toString(): String {
        return "Email key = '$key', isDraft='$isDraft', " +
                "isTrash='$isTrash', date='$date')"
    }
}
