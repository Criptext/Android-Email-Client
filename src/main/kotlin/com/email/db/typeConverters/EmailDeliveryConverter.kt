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
            0 -> DeliveryTypes.RECEIVED
            1 -> DeliveryTypes.SENT
            else -> DeliveryTypes.UNSENT
        }
    }

    @TypeConverter
    fun parseDeliveryType(value: DeliveryTypes): Int {
        return when(value) {
            DeliveryTypes.SENT -> 1
            DeliveryTypes.RECEIVED -> 0
            else -> 2 // UNSENT
        }
    }

}
