package com.email.db.models

import android.arch.persistence.room.*
import android.arch.persistence.room.ForeignKey.CASCADE
import java.util.*
import android.support.annotation.NonNull
import com.email.utils.DateUtils
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
        var emailId : Long

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
            val files = ArrayList<CRFile>()
            for (i in 0 until jsonFiles.length()) {
                val file = jsonFiles.getJSONObject(i)
                files.add(CRFile(
                        0,
                        file.getString("token"),
                        file.getString("name"),
                        file.getLong("size"),
                        1,
                        Date(),
                        false,
                        0))
            }
            return files
        }
    }
}
