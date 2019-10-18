package com.criptext.mail.db

enum class AccountTypes {
    STANDARD, PLUS, ENTERPRISE, LUCKY, REDEEMED;

    companion object {
        fun fromInt(int: Int): AccountTypes {
            val values = values()
            val index = int - 1
            return if (index > -1 && index < values.size)
                values[index]
            else
                STANDARD
        }

        fun getTrueOrdinal(accountTypes: AccountTypes): Int {
            return accountTypes.ordinal + 1
        }
    }
}
