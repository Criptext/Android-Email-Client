package com.email.db.models

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
        indices = [Index(value = ["recipientId"]), Index(value = ["name"])] )
open class Contact(

        @PrimaryKey
        @ColumnInfo(name = "recipientId")
        var email : String,

        @ColumnInfo(name = "name")
        var name : String
) {


    override fun toString(): String {
        if(email != name){
            return "${deAccent(name)} <$email>"
        }
        return email
    }

    companion object {
        val supportContact = Contact("support@criptext.com", "Support")

        fun deAccent(str: String): String {
            val nfdNormalizedString = Normalizer.normalize(str, Normalizer.Form.NFD)
            val pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+")
            return pattern.matcher(nfdNormalizedString).replaceAll("")
        }
    }

    class Invalid(email: String, name: String): Contact(email, name)
}
