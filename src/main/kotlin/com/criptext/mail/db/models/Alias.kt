package com.criptext.mail.db.models

import androidx.room.*


@Entity(tableName = "alias", indices = [Index(value = ["accountId"], name = "account_id_alias_index")],
        foreignKeys = [
                ForeignKey(entity = Account::class,
                        parentColumns = ["id"],
                        onDelete = ForeignKey.CASCADE,
                        childColumns = ["accountId"]
                )
        ])
class Alias(

        @PrimaryKey(autoGenerate = true)
        @ColumnInfo(name = "id")
        var id : Long,

        @ColumnInfo(name = "rowId")
        var rowId : Long,

        @ColumnInfo(name = "name")
        var name : String,

        @ColumnInfo(name = "domain")
        var domain : String?,

        @ColumnInfo(name = "accountId")
        var accountId : Long

)
