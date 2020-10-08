package com.criptext.mail.db.models

import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.room.*
import com.criptext.mail.db.DeliveryTypes
import org.json.JSONObject
import java.util.*

/**
 * Created by sebas on 1/24/18.
 */

@Entity(tableName = "email",
        indices = [
                Index(value = ["metadataKey", "accountId"], name = "email_metadataKey_index", unique = true),
                Index(value = ["messageId", "accountId"], name = "email_messageId_index", unique = true),
                Index(value = ["accountId"], name = "email_accountId_index")
        ],
        foreignKeys = [ForeignKey(entity = Account::class,
                parentColumns = ["id"],
                onDelete = ForeignKey.CASCADE,
                childColumns = ["accountId"])])
data class Email(
        @PrimaryKey(autoGenerate = true)
        var id: Long,

        @ColumnInfo(name = "messageId")
        var messageId : String,

        @ColumnInfo(name = "threadId")
        var threadId: String,

        @ColumnInfo(name = "fromAddress")
        var fromAddress: String,

        @ColumnInfo(name = "replyTo")
        var replyTo: String?,

        @ColumnInfo(name = "boundary")
        var boundary: String?,

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

        @ColumnInfo(name = "trashDate")
        @Nullable
        var trashDate: Date?,

        @ColumnInfo(name = "isNewsletter")
        @Nullable
        var isNewsletter: Boolean?,

        @ColumnInfo(name = "accountId")
        @NonNull
        var accountId : Long
){
        @Ignore
        var headers : String? = null

        companion object {
            fun fromJSON(jsonString: String, accountId: Long): Email{
                val json = JSONObject(jsonString)
                val email = Email(
                        id = json.getLong("id"),
                        messageId = json.getString("messageId"),
                        threadId = json.getString("threadId"),
                        fromAddress = if(json.has("fromAddress")) json.getString("fromAddress") else "",
                        replyTo = if(json.has("replyTo")) json.getString("replyTo") else null,
                        unread = json.getBoolean("unread"),
                        secure = json.getBoolean("secure"),
                        boundary = if(json.has("boundary")) json.getString("boundary") else null,
                        content = json.getString("content"),
                        preview = if(json.has("preview")) json.getString("preview") else "",
                        subject = if(json.has("subject")) json.getString("subject") else "",
                        delivered = DeliveryTypes.fromInt(json.getInt("status")),
                        date = com.criptext.mail.utils.DateAndTimeUtils.getDateFromString(
                                json.getString("date"), null),
                        isNewsletter = if(json.has("isNewsletter")) json.getBoolean("isNewsletter") else null,
                        metadataKey = json.getLong("key"),
                        unsentDate = if(json.has("unsentDate")) com.criptext.mail.utils.DateAndTimeUtils.getDateFromString(
                                json.getString("unsentDate"), null) else null,
                        trashDate = if(json.has("trashDate")) com.criptext.mail.utils.DateAndTimeUtils.getDateFromString(
                                json.getString("trashDate"), null) else null,
                        accountId = accountId

                )
                email.headers = if(json.has("headers")) json.getString("headers") else null
                return email
            }
        }
}
