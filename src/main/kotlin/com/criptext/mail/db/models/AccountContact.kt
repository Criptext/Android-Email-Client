package com.criptext.mail.db.models

import androidx.room.*
import androidx.room.ForeignKey.CASCADE
import com.criptext.mail.db.ContactTypes
import org.json.JSONObject


@Entity(tableName = "account_contact",
        indices = [ (Index("accountId", "contactId", unique = true)) ],
        foreignKeys = [
            ForeignKey(entity = Account::class,
                parentColumns = ["id"],
                onDelete = CASCADE,
                childColumns = ["accountId"]
            ),
            ForeignKey(entity = Contact::class,
                parentColumns = ["id"],
                onDelete = CASCADE,
                childColumns = ["contactId"]
            )
        ]
)
data class AccountContact(
        @PrimaryKey(autoGenerate = true) var id: Long,
        @ColumnInfo(name = "accountId") var accountId: Long,
        @ColumnInfo(name = "contactId") var contactId: Long){
    companion object {
        fun fromJSON(jsonString: String): AccountContact {
            val json = JSONObject(jsonString)
            return AccountContact(
                    id = json.getLong("id"),
                    accountId = json.getLong("accountId"),
                    contactId = json.getLong("contactId")
            )
        }
    }
}
