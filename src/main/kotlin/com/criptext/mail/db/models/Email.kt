package com.criptext.mail.db.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.annotation.Nullable
import com.criptext.mail.db.DeliveryTypes
import org.json.JSONObject
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

        @ColumnInfo(name = "unsentDate")
        @Nullable
        var unsentDate : Date?,

        @ColumnInfo(name = "date")
        var date : Date,

        @ColumnInfo(name = "metadataKey")
        var metadataKey : Long,

        @ColumnInfo(name = "isMuted")
        var isMuted: Boolean,

        @ColumnInfo(name = "trashDate")
        @Nullable
        var trashDate: Date?
){
        companion object {
            fun fromJSON(jsonString: String): Email{
                val json = JSONObject(jsonString)
                return Email(
                        id = json.getLong("id"),
                        messageId = json.getString("messageId"),
                        threadId = json.getString("threadId"),
                        unread = json.getBoolean("unread"),
                        secure = json.getBoolean("secure"),
                        content = json.getString("content"),
                        preview = if(json.has("preview")) json.getString("preview") else "",
                        subject = if(json.has("subject")) json.getString("subject") else "",
                        delivered = DeliveryTypes.fromInt(json.getInt("status")),
                        date = com.criptext.mail.utils.DateAndTimeUtils.getDateFromString(
                                json.getString("date"), null),
                        metadataKey = json.getLong("key"),
                        isMuted = json.getBoolean("isMuted"),
                        unsentDate = if(json.has("unsentDate")) com.criptext.mail.utils.DateAndTimeUtils.getDateFromString(
                                json.getString("unsentDate"), null) else null,
                        trashDate = if(json.has("trashDate")) com.criptext.mail.utils.DateAndTimeUtils.getDateFromString(
                                json.getString("trashDate"), null) else null

                )
            }
        }
}
