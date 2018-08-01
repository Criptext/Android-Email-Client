package com.criptext.mail.db.models

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.ForeignKey
import android.arch.persistence.room.ForeignKey.CASCADE
import android.arch.persistence.room.Index

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
        )

