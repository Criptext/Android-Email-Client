package com.email.db.models.signal

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity

/**
 * Created by gabriel on 3/6/18.
 */
@Entity(tableName = "raw_identitykey", primaryKeys = [ "recipientId", "deviceId" ])
class RawIdentityKey(
        @ColumnInfo(name = "recipientId")
        var recipientId : String,

        @ColumnInfo(name = "deviceId")
        var deviceId : Int,

        @ColumnInfo(name = "identityKey")
        var identityKey: String
)
