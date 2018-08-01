package com.criptext.mail.db.models

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.Index
import android.arch.persistence.room.PrimaryKey
import java.text.Normalizer
import java.util.regex.Pattern

/**
 * Created by gabriel on 2/26/18.
 */
@Entity(tableName = "contact",
        indices = [Index(value = "email", unique = true)] )

open class Contact(

        @PrimaryKey(autoGenerate = true)
        @ColumnInfo(name = "id")
        var id: Long,

        @ColumnInfo(name = "email")
        var email : String,

        @ColumnInfo(name = "name")
        var name : String
) {

    override fun equals(other: Any?): Boolean {
        if (other is Contact) {
            if (other.id != this.id) return false
            if (other.email != this.email) return false
            if (other.name != this.name) return false
            return true
        }
        return false
    }


    override fun toString(): String {
        if(email != name){
            return "${deAccent(name)} <$email>"
        }
        return email
    }

    companion object {

        const val mainDomain = "jigl.com"
        val toAddress: (Contact) -> String = { contact -> contact.email }

        fun deAccent(str: String): String {
            val nfdNormalizedString = Normalizer.normalize(str, Normalizer.Form.NFD)
            val pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+")
            return pattern.matcher(nfdNormalizedString).replaceAll("")
        }
    }

    class Invalid(email: String, name: String): Contact(0, email, name)
}
