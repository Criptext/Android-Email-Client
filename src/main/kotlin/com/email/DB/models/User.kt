package com.email.DB.models

import android.arch.persistence.room.Entity
import android.arch.persistence.room.Index
import android.arch.persistence.room.PrimaryKey
import android.arch.persistence.room.ColumnInfo

/**
 * Created by sebas on 2/6/18.
 */

@Entity(tableName = "user", indices = [Index(value = "name")] )
public class User(
        @PrimaryKey(autoGenerate = true)
        var id:Int,

        @ColumnInfo(name = "email")
        var email : String,

        @ColumnInfo(name = "name")
        var name : String,

        @ColumnInfo(name = "nickname")
        var nickname : String
) {


    override fun toString(): String {
        return "User email='$email', name='$name', nickname='$nickname'"
    }
}
