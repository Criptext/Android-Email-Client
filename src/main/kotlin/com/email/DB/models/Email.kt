package com.email.DB.models

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.Index
import android.arch.persistence.room.PrimaryKey
import java.time.LocalDate
import java.util.*

/**
 * Created by sebas on 1/24/18.
 */

@Entity(tableName = "email", indices = arrayOf(Index(value = "subject", name = "subject")))
public class Email(
        @PrimaryKey(autoGenerate = true)
        var id:Int,

        @ColumnInfo(name = "key")
        var key : String,

        @ColumnInfo(name = "threadid")
        var threadid : String,

        @ColumnInfo(name = "unread")
        var unread : Byte,

        @ColumnInfo(name = "secure")
        var secure : Byte,

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
        var isTrash : Byte,

        @ColumnInfo(name = "isDraft")
        var isDraft : Byte
) {


    override fun toString(): String {
        return "Email key = '$key', isDraft='$isDraft', isTrash='$isTrash', date='$date')"
    }
}
