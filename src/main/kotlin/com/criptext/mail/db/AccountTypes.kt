package com.criptext.mail.db

enum class AccountTypes {
    STANDARD, PLUS, ENTERPRISE, LUCKY, REDEEMED, FAN, HERO, LEGEND;

    companion object {
        fun fromInt(int: Int): AccountTypes {
            val values = values()
            return if (int > -1 && int < values.size)
                values[int]
            else
                STANDARD
        }
    }
}
