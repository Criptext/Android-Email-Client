package com.email.db.models

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.ForeignKey
import android.arch.persistence.room.ForeignKey.CASCADE
import android.arch.persistence.room.Index
import android.support.annotation.NonNull

/**
 * Created by sebas on 1/24/18.
 */

@Entity(tableName = "email_label",
        indices = [
            Index(value = ["emailId"]),
            Index(value = ["labelId"])],
        primaryKeys = [ "emailId", "labelId" ],
        foreignKeys = arrayOf( ForeignKey(entity = Email::class,
                parentColumns = ["id"],
                onDelete = CASCADE,
                childColumns = ["emailId"]),
                ForeignKey(entity = Label::class,
                        parentColumns = ["id"],
                        onDelete = CASCADE,
                        childColumns = ["labelId"])) )
class EmailLabel {

    @ColumnInfo(name = "emailId")
    @NonNull
    var emailId : Int

    @ColumnInfo(name = "labelId")
    @NonNull
    var labelId : Int

    constructor(emailId : Int, labelId: Int) {
        this.emailId = emailId
        this.labelId = labelId
    }
}