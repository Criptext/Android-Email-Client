package com.criptext.mail.db.models

import androidx.room.*
import org.json.JSONObject


@Entity(tableName = "customDomain", indices = [Index(value = ["accountId"], name = "account_id_custom_domain_index"),
    Index(value = ["name", "accountId"], name = "name_accountId_custom_domain_index", unique = true)],
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

        @ColumnInfo(name = "name")
        var name : String,

        @ColumnInfo(name = "validated")
        var validated : Boolean,

        @ColumnInfo(name = "accountId")
        var accountId : Long

){
    companion object{
        fun fromJSON(jsonString: String, accountId: Long): CustomDomain {
            val json = JSONObject(jsonString)
            return CustomDomain(
                    id = 0,
                    name = json.getString("name"),
                    validated = json.getBoolean("validated"),
                    accountId = accountId

            )
        }
    }
}
