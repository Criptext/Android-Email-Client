package com.email.DB.models

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.Index
import android.arch.persistence.room.PrimaryKey
import java.util.*

/**
 * Created by sebas on 2/6/18.
 */

@Entity(tableName = "file", indices = arrayOf(Index(value = "name")) )
public class File(
        @PrimaryKey(autoGenerate = true)
        var id:Int,

        @ColumnInfo(name = "name")
        var name : String,

        @ColumnInfo(name = "size")
        var size : Int,

        @ColumnInfo(name = "status")
        var status : Int,

        @ColumnInfo(name = "date")
        var date : Date,

        @ColumnInfo(name = "readOnly")
        var readOnly : Byte
) {
    override fun toString(): String {
        return "File name='$name', " +
                "size='$size', " +
                "status='$status', " +
                "date='$date', " +
                "readonly: '$readOnly' "
    }
}
