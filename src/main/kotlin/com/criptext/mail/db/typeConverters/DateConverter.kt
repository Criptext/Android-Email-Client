package com.criptext.mail.db.typeConverters

import android.arch.persistence.room.TypeConverter
import java.text.SimpleDateFormat
import java.util.*


/**
 * Created by sebas on 1/22/18.
 */

class DateConverter {
    private val df : SimpleDateFormat =
            SimpleDateFormat(
                    "yyyy-MM-dd HH:mm:ss",
                    Locale.US)

    @TypeConverter
    fun getDate(value: Long?) : Date?  {
        return if(value != null) Date(value) else null
    }

    @TypeConverter
    fun parseDate(value: Date?): Long? {
        return value?.time
    }

    init {
        df.timeZone = TimeZone.getTimeZone("UTC")
        df.isLenient = false
    }
}
