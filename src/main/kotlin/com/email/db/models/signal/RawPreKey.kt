package com.email.db.models.signal

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

/**
 * Created by gabriel on 3/5/18.
 */

@Entity(tableName = "raw_prekey")
class RawPreKey(
        @PrimaryKey
        var id: Int,
        @ColumnInfo(name = "byteString")
        var byteString: String
)