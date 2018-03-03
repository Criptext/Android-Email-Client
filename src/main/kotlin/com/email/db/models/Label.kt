package com.email.db.models

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

/**
 * Created by sebas on 1/24/18.
 */

@Entity(tableName = "label")
class Label (
        @PrimaryKey(autoGenerate = true)
        var id:Int?,

        @ColumnInfo(name = "color")
        var color: String,

        @ColumnInfo(name = "text")
        var text: String
){

}