package com.email.db.typeConverters

import android.arch.persistence.room.TypeConverter
import com.email.db.LabelTypes

/**
 * Created by danieltigse on 5/28/18.
 */

class LabelTypeConverter{

    @TypeConverter
    fun getLabelType(value: String) : LabelTypes {
        return when(value) {
            "SYSTEM" -> {
                LabelTypes.SYSTEM
            }
            else -> {
                LabelTypes.CUSTOM
            }
        }
    }

    @TypeConverter
    fun parseLabelType(value: LabelTypes): String {
        return when(value) {
            LabelTypes.SYSTEM -> {
                "SYSTEM"
            }
            LabelTypes.CUSTOM -> {
                "CUSTOM"
            }
        }
    }
}