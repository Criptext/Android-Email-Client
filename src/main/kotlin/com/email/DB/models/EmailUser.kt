package com.email.DB.models

import android.arch.persistence.room.*
import android.arch.persistence.room.ForeignKey.CASCADE
import android.support.annotation.NonNull

/**
 * Created by sebas on 2/6/18.
 */

@Entity(tableName = "email_user",
        primaryKeys = ["emailId", "userId"],
        foreignKeys = arrayOf( ForeignKey(entity = Email::class,
                parentColumns = ["id"],
                onDelete = CASCADE,
                childColumns = ["emailId"]),
                ForeignKey(entity = User::class,
                        parentColumns = ["id"],
                        onDelete = CASCADE,
                        childColumns = ["userId"]))
)
class EmailUser {

    @ColumnInfo(name = "emailId")
    @NonNull
    var emailId: Int

    @ColumnInfo(name = "userId")
    @NonNull
    var userId: Int

    @ColumnInfo(name = "type")
    var type: String

    constructor(emailId: Int, userId: Int, type: String) {
        this.emailId = emailId
        this.userId = userId
        this.type = type
    }
}