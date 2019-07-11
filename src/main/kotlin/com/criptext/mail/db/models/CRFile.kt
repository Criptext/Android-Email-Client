package com.criptext.mail.db.models

import androidx.room.*
import androidx.room.ForeignKey.CASCADE
import java.util.*
import androidx.annotation.NonNull
import com.criptext.mail.api.toList
import com.criptext.mail.utils.DateAndTimeUtils
import com.criptext.mail.utils.file.FileUtils
import com.github.kittinunf.result.Result
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

    @Ignore
    var mimeType : String? = null

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
            val getArray = Result.of { emailData.getJSONArray("fileKeys").toList<String>()  }
            val jsonKeys = when(getArray){
                is Result.Success -> getArray.value
                is Result.Failure -> listOf()
            }
            val files = ArrayList<CRFile>()
            for (i in 0 until jsonFiles.length()) {
                val file = jsonFiles.getJSONObject(i)
                val fileKey = if(jsonKeys.isNotEmpty()) jsonKeys[i] else null
                val crFile = CRFile(
                        0,
                        file.getString("token"),
                        file.getString("name"),
                        file.getLong("size"),
                        1,
                        Date(),
                        false,
                        0,
                        if(file.has("cid")) file.getString("cid") else null,
                        false,
                        fileKey = when (fileKey) {
                            null -> ""
                            else -> fileKey
                        }
                )
                crFile.mimeType = file.optString("mimeType")
                files.add(crFile)
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
                    cid = if(json.has("cid")) json.getString("cid") else null,
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
