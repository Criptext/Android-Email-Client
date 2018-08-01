package com.criptext.mail.db.models.signal

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

/**
 * Created by gabriel on 3/5/18.
 */

@Entity(tableName = "raw_prekey")
class CRPreKey(
        @PrimaryKey
        var id: Int,
        @ColumnInfo(name = "byteString")
        var byteString: String
) {
        override fun equals(other: Any?): Boolean {
            return if (other is CRPreKey) id == other.id && byteString == other.byteString
            else false
        }

    override fun hashCode(): Int {
        return id
    }
}