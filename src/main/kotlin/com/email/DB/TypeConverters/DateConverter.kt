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
    private val df : SimpleDateFormat =
            SimpleDateFormat( "yyyy-MM-dd HH:mm:dd", Locale.US)

    @TypeConverter
    fun getDate(value: Long) : Date  {
        return Date(value)
    }

    @TypeConverter
    fun parseDate(value: Date): Long {
        return value.time
    }

    init {
        df.timeZone = TimeZone.getDefault()
    }
}
