package com.criptext.mail.db.typeConverters

import androidx.room.TypeConverter
import com.criptext.mail.db.ContactTypes

/**
 * Created by sebas on 3/27/18.
 */

class ContactTypeConverter {

    @TypeConverter
    fun getContactType(value: Int) : ContactTypes {
        return when(value) {
            0 -> ContactTypes.BCC
            1 -> ContactTypes.CC
            2 -> ContactTypes.FROM
            3 -> ContactTypes.TO
            else -> ContactTypes.FROM
        }
    }

    @TypeConverter
    fun parseDeliveryType(value: ContactTypes): Int {
        return when(value) {
            ContactTypes.BCC -> 0
            ContactTypes.CC -> 1
            ContactTypes.FROM -> 2
            ContactTypes.TO -> 3
        }
    }

}
