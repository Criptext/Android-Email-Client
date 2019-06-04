package com.criptext.mail.db.models.signal

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Created by gabriel on 3/6/18.
 */

@Entity(tableName = "raw_signedprekey",
        indices = [ Index(value = ["accountId"], name = "signedprekey_accountId_index") ],
        foreignKeys = [androidx.room.ForeignKey(entity = com.criptext.mail.db.models.Account::class,
        parentColumns = ["id"],
        onDelete = androidx.room.ForeignKey.CASCADE,
        childColumns = ["accountId"])])
class CRSignedPreKey(
        @PrimaryKey
        var id: Int,
        @ColumnInfo(name = "byteString")
        var byteString: String,

        @ColumnInfo(name = "accountId")
        @NonNull
        var accountId : Long
)
