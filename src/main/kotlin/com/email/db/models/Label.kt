package com.email.db.models

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import com.email.db.ColorTypes

/**
 * Created by sebas on 1/24/18.
 */

@Entity(tableName = "label")
class Label (
        @PrimaryKey(autoGenerate = true)
        var id:Int?,

        @ColumnInfo(name = "color")
        var color: ColorTypes,

        @ColumnInfo(name = "text")
        var text: String
){
        override fun equals(other: Any?): Boolean {

                other as Label
                if (id != other.id) return false
                if (color != other.color) return false
                if (text != other.text) return false

                return true
        }

        override fun hashCode(): Int {
            return id!!
        }
}