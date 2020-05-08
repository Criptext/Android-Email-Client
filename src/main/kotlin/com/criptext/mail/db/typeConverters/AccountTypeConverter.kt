package com.criptext.mail.db.typeConverters

import androidx.room.TypeConverter
import com.criptext.mail.db.AccountTypes
import com.criptext.mail.db.ContactTypes
import com.google.android.gms.common.internal.AccountType

class AccountTypeConverter {

    @TypeConverter
    fun getAccountType(value: Int) : AccountTypes {
        return when(value) {
            0 -> AccountTypes.STANDARD
            1 -> AccountTypes.PLUS
            2 -> AccountTypes.ENTERPRISE
            3 -> AccountTypes.LUCKY
            4 -> AccountTypes.REDEEMED
            else -> AccountTypes.STANDARD
        }
    }

    @TypeConverter
    fun parseAccountType(value: AccountTypes): Int {
        return when(value) {
            AccountTypes.STANDARD -> 0
            AccountTypes.PLUS -> 1
            AccountTypes.ENTERPRISE -> 2
            AccountTypes.LUCKY -> 3
            AccountTypes.REDEEMED -> 4
        }
    }

}
