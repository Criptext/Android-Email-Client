package com.email.db.models

import android.arch.persistence.room.Entity
import android.arch.persistence.room.Index
import android.arch.persistence.room.PrimaryKey
import android.arch.persistence.room.ColumnInfo

/**
 * Created by sebas on 2/6/18.
 */

@Entity(tableName = "account", indices = [Index(value = "name")] )
class Account(

        @PrimaryKey
        @ColumnInfo(name = "recipientId")
        var recipientId : String,

        @ColumnInfo(name = "name")
        var name : String,

        @ColumnInfo(name = "jwt")
        var jwt : String,

        @ColumnInfo(name = "identityKeyPairB64")
        var identityKeyPairB64: String,

        @ColumnInfo(name = "registrationId")
        var registrationId : Int
) {

    override fun toString(): String {
        return "User recipientId='$recipientId', name='$name', " +
                "registrationId='$registrationId'"
    }
}
