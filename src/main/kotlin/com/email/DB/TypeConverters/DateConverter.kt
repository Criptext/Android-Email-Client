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
        var df : DateFormat? = SimpleDateFormat( "yyyy-MM-dd")

        @TypeConverter
        fun getDate(value: String?) : Date?  {
                try {
                    return df?.parse(value)
                }catch (e : Exception) {
                    Log.d("MISSING FIELD: ", "date...")
                    return Date(1992,2,1)
                }
        }

        @TypeConverter
        fun parseDate(value: Date?): String? {
            try {
                return df?.format(value)
            }catch (e: Exception) {
                Log.d("CONVERTER PARSE DATE", if(value != null) value.toString() else "EMPTY")
                e.printStackTrace()
            }
            return null
        }
}
