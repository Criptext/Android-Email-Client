package com.criptext.mail.db.models

import androidx.room.*
import androidx.room.ForeignKey.CASCADE
import java.util.*
import androidx.annotation.NonNull
import com.criptext.mail.utils.DateAndTimeUtils
import com.criptext.mail.utils.file.FileUtils
import org.json.JSONObject
import kotlin.collections.ArrayList

/**
 * Created by sebas on 2/6/18.
 */

@Entity(tableName = "file",
        indices = [Index(value = ["name"]), Index(value = ["emailId"])],
        foreignKeys = [ForeignKey(entity = Email::class,
                                          parentColumns = ["id"],
                                          onDelete = CASCADE,
                                          childColumns = ["emailId"])])
class CRFile(

        @PrimaryKey(autoGenerate = true)
        @ColumnInfo(name = "id")
        var id: Long,

        var token: String,

        @ColumnInfo(name = "name")
        var name : String,

        @ColumnInfo(name = "size")
        var size : Long,

        @ColumnInfo(name = "status")
        var status : Int,

        @ColumnInfo(name = "date")
        var date : Date,

        @ColumnInfo(name = "readOnly")
        var readOnly : Boolean,

        @ColumnInfo(name = "emailId")
        @NonNull
        var emailId : Long,

        @ColumnInfo(name = "cid")
        var cid : String?,

        @ColumnInfo(name = "shouldDuplicate")
        var shouldDuplicate : Boolean,

        @ColumnInfo(name = "fileKey")
        var fileKey: String

) {

    override fun toString(): String {
        return "CRFile name='$name', " +
                "size='$size', " +
                "status='$status', " +
                "date='$date', " +
                "readonly: '$readOnly' " +
                "emailId: '$emailId' "
    }

    companion object {
        fun listFromJSON(metadataString: String): List<CRFile>{
            val emailData = JSONObject(metadataString)
            if (!emailData.has("files")) return emptyList()
            val jsonFiles = emailData.getJSONArray("files")
            val jsonKeys = emailData.getJSONArray("fileKeys")
            val files = ArrayList<CRFile>()
            for (i in 0 until jsonFiles.length()) {
                val file = jsonFiles.getJSONObject(i)
                val fileKey = jsonKeys.optString(i)
                files.add(CRFile(
                        0,
                        file.getString("token"),
                        file.getString("name"),
                        file.getLong("size"),
                        1,
                        Date(),
                        false,
                        0,
                        file.optString("cid")?: null,
                        false,
                        fileKey = when (fileKey) {
                            null -> ""
                            else -> fileKey
                        }
                ))
            }
            return files
        }

        fun fromJSON(jsonString: String): CRFile {
            val json = JSONObject(jsonString)
            return CRFile(
                    id = json.getLong("id"),
                    token = json.getString("token"),
                    name = json.getString("name"),
                    size = json.getLong("size"),
                    status = json.getInt("status"),
                    date = DateAndTimeUtils.getDateFromString(json.getString("date"), null),
                    cid = json.optString("cid")?: null,
                    emailId = json.getLong("emailId"),
                    readOnly = json.getBoolean("readOnly"),
                    shouldDuplicate = false,
                    fileKey = when {
                        json.has("key") ->
                            json.getString("key").plus(":".plus(json.getString("iv")))
                        else -> ""
                    }
            )
        }
    }
}
