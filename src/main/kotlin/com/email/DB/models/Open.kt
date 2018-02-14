package com.email.DB.models

import android.arch.persistence.room.*
import android.arch.persistence.room.ForeignKey.CASCADE
import android.support.annotation.NonNull
import java.util.*

/**
 * Created by sebas on 2/6/18.
 */

@Entity(tableName = "open",
        indices = [Index(value = "date")],
        foreignKeys = [ForeignKey(entity = File::class,
                                          parentColumns = ["token"],
                                          onDelete = CASCADE,
                                          childColumns = ["fileId"])])
public class Open(
        @PrimaryKey(autoGenerate = true)
        var id:Int,

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
