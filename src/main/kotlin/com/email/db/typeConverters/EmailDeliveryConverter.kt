package com.email.db.typeConverters

import android.arch.persistence.room.TypeConverter
import com.email.db.DeliveryTypes

/**
 * Created by sebas on 3/22/18.
 */

class EmailDeliveryConverter {

    @TypeConverter
    fun getDeliveryType(value: Int) : DeliveryTypes  {
        return DeliveryTypes.fromInt(value)
    }

    @TypeConverter
    fun parseDeliveryType(value: DeliveryTypes): Int {
        return DeliveryTypes.values().indexOf(value) + 1
    }

}
