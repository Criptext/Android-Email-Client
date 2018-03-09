package com.email.db.models

import android.arch.persistence.room.*
import android.arch.persistence.room.ForeignKey.CASCADE
import android.support.annotation.NonNull

/**
 * Created by sebas on 2/6/18.
 */

@Entity(tableName = "email_contact",
        primaryKeys = ["emailId", "contactId"],
        foreignKeys = [(ForeignKey(entity = Email::class,
                parentColumns = ["id"],
                onDelete = CASCADE,
                childColumns = ["emailId"])), (ForeignKey(entity = Contact::class,
                parentColumns = ["recipientId"],
                onDelete = CASCADE,
                childColumns = ["contactId"]))]
)
class EmailContact {

    @ColumnInfo(name = "emailId")
    @NonNull
    var emailId: Int

    @ColumnInfo(name = "contactId")
    @NonNull
    var contactId: String

    @ColumnInfo(name = "type")
    var type: String

    constructor(emailId: Int, contactId: String, type: String) {
        this.emailId = emailId
        this.contactId = contactId
        this.type = type
    }
}