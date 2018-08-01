package com.criptext.mail.db.models.signal

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

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
