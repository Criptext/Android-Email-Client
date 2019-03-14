package com.criptext.mail.db.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.criptext.mail.BuildConfig
import com.criptext.mail.utils.EmailAddressUtils
import org.json.JSONObject
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
        var name : String,

        @ColumnInfo(name = "isTrusted")
        var isTrusted : Boolean,

        @ColumnInfo(name = "score")
        var score : Int
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

        val mainDomain = "jigl.com"
        val toAddress: (Contact) -> String = { contact -> contact.email }

        fun deAccent(str: String): String {
            val nfdNormalizedString = Normalizer.normalize(str, Normalizer.Form.NFD)
            val pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+")
            return pattern.matcher(nfdNormalizedString).replaceAll("")
        }

        fun fromJSON(jsonString: String): Contact {
            val json = JSONObject(jsonString)
            val id = json.getLong("id")
            val email = json.getString("email")
            val name = if(json.has("name")) json.getString("name")
                                else EmailAddressUtils.extractName(email)
            val isTrusted = if(json.has("isTrusted")) json.getBoolean("isTrusted")
            else false


            return Contact(
                    id =  id,
                    email = email,
                    name = name,
                    isTrusted = isTrusted,
                    score = 0
            )
        }

        fun toJSON(contact: Contact): JSONObject{
            val json = JSONObject()
            json.put("id", contact.id)
            json.put("email", contact.email)
            json.put("name", contact.name)
            json.put("isTrusted", contact.isTrusted)
            json.put("score", contact.score)
            return json
        }
    }

    class Invalid(email: String, name: String): Contact(0, email, name, false, 0)
}
