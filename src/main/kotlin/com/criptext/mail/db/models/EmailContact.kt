package com.criptext.mail.db.models

import android.arch.persistence.room.*
import android.arch.persistence.room.ForeignKey.CASCADE
import com.criptext.mail.db.ContactTypes
import org.json.JSONObject

/**
 * Created by sebas on 2/6/18.
 */

@Entity(tableName = "email_contact",
        indices = [ (Index("emailId", "type", "contactId")) ],
        foreignKeys = [
            ForeignKey(entity = Email::class,
                parentColumns = ["id"],
                onDelete = CASCADE,
                childColumns = ["emailId"]
            ),
            ForeignKey(entity = Contact::class,
                parentColumns = ["id"],
                onDelete = CASCADE,
                childColumns = ["contactId"]
            )
        ]
)
data class EmailContact(
        @PrimaryKey(autoGenerate = true) var id: Long,
        @ColumnInfo(name = "emailId") var emailId: Long,
        @ColumnInfo(name = "contactId") var contactId: Long,
        @ColumnInfo(name = "type") var type: ContactTypes){
    companion object {
        fun fromJSON(jsonString: String): EmailContact {
            val json = JSONObject(jsonString)
            return EmailContact(
                    id = json.getLong("id"),
                    emailId = json.getLong("emailId"),
                    contactId = json.getLong("contactId"),
                    type = ContactTypes.valueOf(json.getString("type").toUpperCase())
            )
        }
    }
}
