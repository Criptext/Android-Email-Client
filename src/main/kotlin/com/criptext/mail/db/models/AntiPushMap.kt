package com.criptext.mail.db.models

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "antiPushMap",
        indices = [Index(value = ["accountId"], name = "antiPushMap_accountId_index")],
        foreignKeys = [androidx.room.ForeignKey(entity = Account::class,
                parentColumns = ["id"],
                onDelete = androidx.room.ForeignKey.CASCADE,
                childColumns = ["accountId"])])
class AntiPushMap(
        @PrimaryKey(autoGenerate = true)
        @ColumnInfo(name = "id")
        var id: Int,

        @ColumnInfo(name = "value")
        var value: String,

        @ColumnInfo(name = "accountId")
        @NonNull
        var accountId : Long
)