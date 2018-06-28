package com.email.db

/**
 * Created by sebas on 3/22/18.
 */

enum class DeliveryTypes {
    UNSEND, FAIL, NONE, SENDING, SENT, DELIVERED, READ;

    companion object {
        fun fromInt(int: Int): DeliveryTypes {
            val values = values()
            val n = int - 1
            return if (n > 0 && int < values.size)
                values[int]
            else
                NONE
        }
    }
}
