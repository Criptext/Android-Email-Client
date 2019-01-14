package com.criptext.mail.db.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.CASCADE
import androidx.room.Index
import org.json.JSONObject

/**
 * Created by sebas on 1/24/18.
 */

@Entity(tableName = "email_label",
        primaryKeys = [ "emailId", "labelId" ],
        indices = [ (Index("labelId", "emailId")) ],
        foreignKeys = [
            ForeignKey(entity = Email::class,
                parentColumns = ["id"],
                onDelete = CASCADE,
                childColumns = ["emailId"]
            ), ForeignKey(entity = Label::class,
                parentColumns = ["id"],
                onDelete = CASCADE,
                childColumns = ["labelId"])
        ])
data class EmailLabel(
        @ColumnInfo(name = "emailId") var emailId: Long,
        @ColumnInfo(name = "labelId") var labelId: Long
        ){
    companion object {
        fun fromJSON(jsonString: String): EmailLabel{
            val json = JSONObject(jsonString)
            return EmailLabel(
                    emailId = json.getLong("emailId"),
                    labelId = json.getLong("labelId")
            )
        }
    }
}

