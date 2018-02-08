package com.email.DB.TypeConverters

import android.arch.persistence.room.TypeConverter

/**
 * Created by sebas on 2/7/18.
 */

class BooleanConverter {

    @TypeConverter
    fun getVal(value: Byte) : Boolean  {
        when(value) {
            1.toByte() -> {
                return true
            }
            0.toByte() -> {
                return false
            }
        }
        return false
    }

    @TypeConverter
    fun parseBoolean(value: Boolean): Byte {
        if(value == true) return 1.toByte()

        return 0.toByte()
    }

}
