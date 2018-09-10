package com.criptext.mail.db

/**
 * Created by sebas on 3/22/18.
 */

enum class DeliveryTypes {
    FAIL, UNSEND, NONE, SENDING, SENT, DELIVERED, READ;

    companion object {
        fun fromInt(int: Int): DeliveryTypes {
            val values = values()
            val index = int - 1
            return if (index > -1 && index < values.size)
                values[index]
            else
                NONE
        }

        fun getTrueOrdinal(deliveryTypes: DeliveryTypes): Int {
            return deliveryTypes.ordinal + 1
        }
    }
}
