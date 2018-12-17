package com.criptext.mail.db.models.signal

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Created by gabriel on 3/6/18.
 */

@Entity(tableName = "raw_signedprekey")
class CRSignedPreKey(
        @PrimaryKey
        var id: Int,
        @ColumnInfo(name = "byteString")
        var byteString: String
)
