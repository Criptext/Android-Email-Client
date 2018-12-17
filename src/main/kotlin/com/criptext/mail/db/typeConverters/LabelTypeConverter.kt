package com.criptext.mail.db.typeConverters

import androidx.room.TypeConverter
import com.criptext.mail.db.LabelTypes

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