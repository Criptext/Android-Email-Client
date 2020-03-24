package com.criptext.mail.db.models

import androidx.room.*
import org.json.JSONObject


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

        @ColumnInfo(name = "active")
        var active : Boolean,

        @ColumnInfo(name = "accountId")
        var accountId : Long

)
{
    companion object {
        fun fromJSON(jsonString: String, accountId: Long): Alias{
            val json = JSONObject(jsonString)
            return Alias(
                    id = 0,
                    name = json.getString("name"),
                    rowId = json.getLong("rowId"),
                    domain = if(json.has("domain")) json.getString("domain") else null,
                    active = json.getBoolean("active"),
                    accountId = accountId

            )
        }
    }
}
