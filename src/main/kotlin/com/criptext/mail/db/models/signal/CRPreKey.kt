package com.criptext.mail.db.models.signal

import androidx.annotation.NonNull
import androidx.room.*
import com.criptext.mail.db.models.Account

/**
 * Created by gabriel on 3/5/18.
 */

@Entity(tableName = "raw_prekey",
        indices = [ Index(value = ["accountId"], name = "preKey_accountId_index") ],
        foreignKeys = [ForeignKey(entity = Account::class,
            parentColumns = ["id"],
            onDelete = ForeignKey.CASCADE,
            childColumns = ["accountId"])])
class CRPreKey(
        @PrimaryKey(autoGenerate = true)
        var id: Long,

        @ColumnInfo(name = "preKeyId")
        var preKeyId: Int,

        @ColumnInfo(name = "byteString")
        var byteString: String,

        @ColumnInfo(name = "accountId")
        @NonNull
        var accountId : Long
) {
        override fun equals(other: Any?): Boolean {
            return if (other is CRPreKey) preKeyId == other.preKeyId && byteString == other.byteString
            else false
        }

    override fun hashCode(): Int {
        return id.toInt()
    }
}