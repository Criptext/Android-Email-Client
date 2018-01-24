package com.email.DB.models

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

/**
 * Created by sebas on 1/24/18.
 */

@Entity(tableName = "label")
class Label {
    @PrimaryKey(autoGenerate = true)
    private var id:Int? = null

    @ColumnInfo(name = "color")
    var color: String? = null

    @ColumnInfo(name = "text")
    var text: String? = null
}