package com.email.db.typeConverters

import android.arch.persistence.room.TypeConverter
import com.email.db.DeliveryTypes

/**
 * Created by sebas on 3/22/18.
 */

class EmailDeliveryConverter {

    @TypeConverter
    fun getDeliveryType(value: Int) : DeliveryTypes  {
        return when(value) {
            0 -> DeliveryTypes.NONE
            1 -> DeliveryTypes.OPENED
            2 -> DeliveryTypes.SENT
            3 -> DeliveryTypes.DELIVERED
            else -> DeliveryTypes.UNSENT
        }
    }

    @TypeConverter
    fun parseDeliveryType(value: DeliveryTypes): Int {
        return when(value) {
            DeliveryTypes.NONE -> 0
            DeliveryTypes.OPENED -> 1
            DeliveryTypes.SENT -> 2
            DeliveryTypes.DELIVERED -> 3
            else -> 3 // UNSENT
        }
    }

}
