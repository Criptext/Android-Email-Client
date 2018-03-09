package com.email.db.models

import android.arch.persistence.room.Entity
import android.arch.persistence.room.Index
import android.arch.persistence.room.PrimaryKey
import android.arch.persistence.room.ColumnInfo

/**
 * Created by sebas on 2/6/18.
 */

@Entity(tableName = "user", indices = [Index(value = "name")] )
class User(
        @ColumnInfo(name = "email")
        var email : String,

        @ColumnInfo(name = "name")
        var name : String,

        @ColumnInfo(name = "nickname")
        var nickname : String,

        @ColumnInfo(name = "jwtoken")
        var jwtoken : String,

        @PrimaryKey
        var registrationId : Int,

        @ColumnInfo(name = "rawIdentityKeyPair")
        var rawIdentityKeyPair : String
) {


    override fun toString(): String {
        return "User email='$email', name='$name', nickname='$nickname'"
    }
}
