package com.criptext.mail.db.models

import android.arch.persistence.room.*
import android.arch.persistence.room.ForeignKey.CASCADE
import android.support.annotation.NonNull
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
        fun fromJSON(metadataString: String): FileKey {
            val emailData = JSONObject(metadataString)
            if (!emailData.has("fileKey")) return FileKey(0,"", 0)
            val jsonFileKey = emailData.get("fileKey").toString()
            return FileKey(0, jsonFileKey,0)
        }
    }
}