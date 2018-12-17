package com.criptext.mail.db.models

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo

/**
 * Created by sebas on 2/6/18.
 */

@Entity(tableName = "account", indices = [Index(value = "name")] )
class Account(

        @PrimaryKey
        @ColumnInfo(name = "recipientId")
        var recipientId : String,

        @ColumnInfo(name = "deviceId")
        var deviceId : Int,

        @ColumnInfo(name = "name")
        var name : String,

        @ColumnInfo(name = "jwt")
        var jwt : String,

        @ColumnInfo(name = "refreshToken")
        var refreshToken : String,

        @ColumnInfo(name = "identityKeyPairB64")
        var identityKeyPairB64: String,

        @ColumnInfo(name = "registrationId")
        var registrationId : Int,

        @ColumnInfo(name = "signature")
        var signature: String
) {

    override fun toString(): String {
        return "User recipientId='$recipientId', name='$name', " +
                "registrationId='$registrationId'"
    }
}
