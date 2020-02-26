package com.criptext.mail.db.models

import androidx.room.*


@Entity(tableName = "customDomain", indices = [Index(value = ["accountId"], name = "account_id_custom_domain_index")],
        foreignKeys = [
                ForeignKey(entity = Account::class,
                        parentColumns = ["id"],
                        onDelete = ForeignKey.CASCADE,
                        childColumns = ["accountId"]
                )
        ])
class CustomDomain(

        @PrimaryKey(autoGenerate = true)
        @ColumnInfo(name = "id")
        var id : Long,

        @ColumnInfo(name = "rowId")
        var rowId : Long,

        @ColumnInfo(name = "name")
        var name : String,

        @ColumnInfo(name = "validated")
        var validated : Boolean,

        @ColumnInfo(name = "accountId")
        var accountId : Long

)
