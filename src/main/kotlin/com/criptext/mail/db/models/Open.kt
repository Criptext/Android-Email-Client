package com.criptext.mail.db.models

import android.arch.persistence.room.*
import android.arch.persistence.room.ForeignKey.CASCADE
import android.support.annotation.NonNull
import java.util.*

/**
 * Created by sebas on 2/6/18.
 */

@Entity(tableName = "open",
        indices = [Index(value = ["date"]), Index(value = ["fileId"])],
        foreignKeys = [ForeignKey(entity = CRFile::class,
                                          parentColumns = ["id"],
                                          onDelete = CASCADE,
                                          childColumns = ["fileId"])])
class Open(
        @PrimaryKey(autoGenerate = true)
        var id:Int?,

        @ColumnInfo(name = "type")
        var type : Int,

        @ColumnInfo(name = "location")
        var location : String,

        @ColumnInfo(name = "date")
        var date : Date,

        @ColumnInfo(name = "fileId")
        @NonNull
        var fileId : String
) {
    override fun toString(): String {
        return "Open type='$type', location='$location', date='$date'  "
    }
}
