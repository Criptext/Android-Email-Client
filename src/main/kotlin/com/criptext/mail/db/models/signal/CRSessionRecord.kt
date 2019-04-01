package com.criptext.mail.db.models.signal

import androidx.annotation.NonNull
import androidx.room.*
import com.criptext.mail.db.models.Account

/**
 * Created by gabriel on 3/5/18.
 */
@Entity(tableName = "raw_session",
        foreignKeys = [ForeignKey(entity = Account::class,
                parentColumns = ["id"],
                onDelete = ForeignKey.CASCADE,
                childColumns = ["accountId"])])
 class CRSessionRecord(
        @PrimaryKey(autoGenerate = true)
        @ColumnInfo(name = "id")
        var id: Long,

        @ColumnInfo(name = "recipientId")
        var recipientId : String,

        @ColumnInfo(name = "deviceId")
        var deviceId : Int,

        @ColumnInfo(name = "byteString")
        var byteString : String,

        @ColumnInfo(name = "accountId")
        @NonNull
        var accountId : Long
)