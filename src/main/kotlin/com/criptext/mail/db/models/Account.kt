package com.criptext.mail.db.models

import androidx.annotation.Nullable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.criptext.mail.db.AccountTypes
import java.util.*

/**
 * Created by sebas on 2/6/18.
 */

@Entity(tableName = "account", indices = [Index(value = ["name"]),
    Index(value = ["recipientId", "domain"], name = "account_email_index", unique = true)] )
class Account(

        @PrimaryKey(autoGenerate = true)
        @ColumnInfo(name = "id")
        var id : Long,

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
        var signature: String,

        @ColumnInfo(name = "domain")
        var domain: String,

        @ColumnInfo(name = "isActive")
        var isActive: Boolean,

        @ColumnInfo(name = "isLoggedIn")
        var isLoggedIn: Boolean,

        @ColumnInfo(name = "hasCloudBackup")
        var hasCloudBackup: Boolean,

        @ColumnInfo(name = "lastTimeBackup")
        @Nullable
        var lastTimeBackup: Date?,

        @ColumnInfo(name = "autoBackupFrequency")
        var autoBackupFrequency : Int,

        @ColumnInfo(name = "wifiOnly")
        var wifiOnly : Boolean,

        @ColumnInfo(name = "backupPassword")
        var backupPassword : String?,

        @ColumnInfo(name = "type")
        var type: AccountTypes
) {

    override fun toString(): String {
        return "User recipientId='$recipientId', name='$name', " +
                "registrationId='$registrationId'"
    }
}
