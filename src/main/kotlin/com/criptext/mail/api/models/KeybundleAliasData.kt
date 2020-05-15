package com.criptext.mail.api.models

import com.criptext.mail.db.models.Contact
import org.json.JSONArray

class KeybundleAliasData {
    companion object{
        fun fromJSONArray(jsonArray: JSONArray): Map<String, String> {
            val map = mutableMapOf<String, String>()
            for (i in 0 until jsonArray.length()){
                val jsonObject = jsonArray.getJSONObject(i)
                val domain = jsonObject["domain"]
                val users = jsonObject.getJSONArray("users")
                for(j in 0 until users.length()){
                    val user = users.getJSONObject(j)
                    val originalDomain = user["originalDomain"]
                    if(originalDomain == Contact.mainDomain){
                        if(originalDomain != domain) {
                            map[user.getString("alias").plus("@$domain")] = user.getString("username")
                        } else {
                            map[user.getString("alias")] = user.getString("username")
                        }
                    } else {
                        map[user.getString("alias").plus("@$domain")] = user.getString("username").plus("@$domain")
                    }
                }
            }
            return map
        }
    }
}