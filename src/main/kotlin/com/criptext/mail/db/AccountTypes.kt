package com.criptext.mail.db

enum class AccountTypes {
    STANDARD, PLUS, ENTERPRISE, LUCKY, REDEEMED;

    companion object {
        fun fromInt(int: Int): AccountTypes {
            val values = values()
            return if (int > -1 && int < values.size)
                values[int]
            else
                STANDARD
        }

        fun getTrueOrdinal(accountTypes: AccountTypes): Int {
            return accountTypes.ordinal + 1
        }
    }
}
