package com.email.db.models

import android.arch.persistence.room.*
import android.arch.persistence.room.ForeignKey.CASCADE
import com.email.db.ContactTypes

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
class EmailContact(
        @PrimaryKey(autoGenerate = true) var id: Long,
        @ColumnInfo(name = "emailId") var emailId: Long,
        @ColumnInfo(name = "contactId") var contactId: Long,
        @ColumnInfo(name = "type") var type: ContactTypes)
