package com.email.db.models

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.ForeignKey
import android.arch.persistence.room.ForeignKey.CASCADE

/**
 * Created by sebas on 1/24/18.
 */

@Entity(tableName = "email_label",
        primaryKeys = [ "emailId", "labelId" ],
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
class EmailLabel(
        @ColumnInfo(name = "emailId") var emailId: Long,
        @ColumnInfo(name = "labelId") var labelId: Long
        )

