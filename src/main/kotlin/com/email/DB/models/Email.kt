package com.email.DB.models

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.Index
import android.arch.persistence.room.PrimaryKey
import java.util.*

/**
 * Created by sebas on 1/24/18.
 */

@Entity(tableName = "email", indices = arrayOf(Index(value = "subject", name = "subject")))
public class Email {

    @PrimaryKey(autoGenerate = true)
    var id:Int? = null

    @ColumnInfo(name = "key")
    var key : String? = null

    @ColumnInfo(name = "threadid")
    var threadid : String? = null

    @ColumnInfo(name = "unread")
    var unread : Byte? = null

    @ColumnInfo(name = "secure")
    var secure : Byte? = null

    @ColumnInfo(name = "content")
    var content : String? = null

    @ColumnInfo(name = "preview")
    var preview : String? = null

    @ColumnInfo(name = "subject")
    var subject : String? = null

    @ColumnInfo(name = "delivered")
    var delivered : Int? = null

    @ColumnInfo(name = "date")
    var date : Date? = null

    @ColumnInfo(name = "isTrash")
    var isTrash : Byte? = null

    @ColumnInfo(name = "isDraft")
    var isDraft : Byte? = null


    override fun toString(): String {
        return "Email key = '$key', isDraft='$isDraft', isTrash='$isTrash', date='$date')"
    }
    constructor(id: Int,
                key: String,
                threadid: String,
                unread: Byte,
                secure: Byte,
                content: String,
                preview: String,
                subject: String,
                delivered: Int,
                date: Date,
                isTrash: Byte,
                isDraft: Byte) {
        this.id = id
        this.key = key
        this.threadid = threadid
        this.unread = unread
        this.secure = secure
        this.content = content
        this.preview = preview
        this.subject = subject
        this.delivered = delivered
        this.date = date
        this.isTrash = isTrash
        this.isDraft = isDraft
    }

}
