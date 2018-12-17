package com.criptext.mail.db.models

import androidx.room.*
import androidx.room.ForeignKey.CASCADE
import androidx.annotation.NonNull
import org.json.JSONObject

@Entity(tableName = "file_key",
        indices = [(Index(value = ["emailId"]))],
        foreignKeys = [(ForeignKey(entity = Email::class,
                parentColumns = ["id"],
                onDelete = CASCADE,
                childColumns = ["emailId"]))])
class FileKey(

        @PrimaryKey(autoGenerate = true)
        @ColumnInfo(name = "id")
        var id: Long,

        @ColumnInfo(name = "key")
        var key : String?,

        @ColumnInfo(name = "emailId")
        @NonNull
        var emailId : Long

) {

    override fun toString(): String {
        return "FileKey id='$id', " +
                "emailId='$emailId', " +
                "key='$key'"
    }

    companion object {
        fun fromJSON(jsonString: String): FileKey {
            val json = JSONObject(jsonString)
            return FileKey(
                    id = json.getLong("id"),
                    key = if(json.has("key"))
                        json.getString("key").plus(":".plus(json.getString("iv")))
                    else null,
                    emailId = json.getLong("emailId")
            )
        }
    }
}