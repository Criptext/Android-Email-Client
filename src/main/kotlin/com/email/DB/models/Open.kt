package com.email.DB.models

import android.arch.persistence.room.Entity
import android.arch.persistence.room.Index
import android.arch.persistence.room.PrimaryKey
import android.arch.persistence.room.ColumnInfo
import java.util.*

/**
 * Created by sebas on 2/6/18.
 */

@Entity(tableName = "open", indices = arrayOf(Index(value = "date")) )
public class Open(
        @PrimaryKey(autoGenerate = true)
        var id:Int,

        @ColumnInfo(name = "type")
        var type : Int,

        @ColumnInfo(name = "location")
        var location : String,

        @ColumnInfo(name = "field")
        var field : String,

        @ColumnInfo(name = "date")
        var date : Date
) {
    override fun toString(): String {
        return "Open type='$type', location='$location', field='$field', date='$date'  "
    }
}
