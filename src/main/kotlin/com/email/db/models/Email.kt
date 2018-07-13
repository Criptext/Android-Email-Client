package com.email.db.models

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.Index
import android.arch.persistence.room.PrimaryKey
import com.email.db.DeliveryTypes
import java.util.Date

/**
 * Created by sebas on 1/24/18.
 */

@Entity(tableName = "email",
        indices = [
                Index(value = "metadataKey", name = "email_metadataKey_index", unique = true),
                Index(value = "messageId", name = "email_messageId_index", unique = true)])
data class Email(
        @PrimaryKey(autoGenerate = true)
        var id: Long,

        @ColumnInfo(name = "messageId")
        var messageId : String,

        @ColumnInfo(name = "threadId")
        var threadId: String,

        @ColumnInfo(name = "unread")
        var unread : Boolean,

        @ColumnInfo(name = "secure")
        var secure : Boolean,

        @ColumnInfo(name = "content")
        var content : String,

        @ColumnInfo(name = "bodyPreview")
        var preview : String,

        @ColumnInfo(name = "subject")
        var subject : String,

        @ColumnInfo(name = "delivered")
        var delivered : DeliveryTypes,

        @ColumnInfo(name = "date")
        var date : Date,

        @ColumnInfo(name = "metadataKey")
        var metadataKey : Long,

        @ColumnInfo(name = "isMuted")
        var isMuted: Boolean
)
