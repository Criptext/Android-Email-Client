package com.criptext.mail.db.models

import androidx.room.*
import androidx.room.ForeignKey.CASCADE

@Entity(tableName = "email_external_session",
        indices = [ (Index("emailId")) ],
        foreignKeys = [
            (ForeignKey(entity = Email::class,
                parentColumns = ["id"],
                onDelete = CASCADE,
                childColumns = ["emailId"]))
        ])
data class  EmailExternalSession(
        @PrimaryKey(autoGenerate = true)
        var id: Long,
        @ColumnInfo(name = "emailId")
        var emailId: Long,
        @ColumnInfo(name = "iv")
        var iv: String,
        @ColumnInfo(name = "salt")
        var salt: String,
        @ColumnInfo(name = "encryptedSession")
        var encryptedSession: String,
        @ColumnInfo(name = "encryptedBody")
        var encryptedBody: String
)
