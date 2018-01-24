package com.email.DB.models

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.ForeignKey

/**
 * Created by sebas on 1/24/18.
 */

@Entity(tableName = "email_label",
        primaryKeys = [ "emailId", "labelId" ],
        foreignKeys = arrayOf( ForeignKey(entity = Email::class, parentColumns = ["id"], childColumns = ["emailId"]),
                               ForeignKey(entity = Label::class, parentColumns = ["id"], childColumns = ["labelId"])) )
class EmailLabel {

    @ColumnInfo(name = "emailId")
    private var emailId : Int? = null

    @ColumnInfo(name = "labelId")
    private var labelId : Int? = null

    constructor(emailId : Int, labelId: Int) {
        this.emailId = emailId
        this.labelId = labelId
    }
}