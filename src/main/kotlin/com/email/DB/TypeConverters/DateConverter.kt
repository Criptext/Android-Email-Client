package com.email.DB.TypeConverters

import android.arch.persistence.room.TypeConverter
import android.util.Log
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.*


/**
 * Created by sebas on 1/22/18.
 */

class DateConverter {
        var df : SimpleDateFormat = SimpleDateFormat( "yyyy-MM-dd HH:mm:dd")

        @TypeConverter
        fun getDate(value: Long) : Date?  {
                try {
                    return Date(value)
                }catch (e : Exception) {
                    return Date(1992,2,1)
                }
        }

        @TypeConverter
        fun parseDate(value: Date): Long {
                return value.time
        }

        init {
                df.timeZone = TimeZone.getDefault()
        }
}
