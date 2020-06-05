package com.criptext.mail.scenes.settings.aliases.data

import com.criptext.mail.db.models.Contact
import com.criptext.mail.db.models.CustomDomain
import org.json.JSONArray

data class AliasData(val rowId: Long, val name: String, val domain: String,
                     val isActive: Boolean, val accountId: Long){
    companion object {
        fun fromJSONArray(array: JSONArray, accountId: Long): Pair<List<CustomDomain>, List<AliasData>> {
            val length = array.length()
            val customDomains = (0 until length)
                    .map {
                        val json = array.getJSONObject(it)
                        val domain = json.getJSONObject("domain")
                        CustomDomain(
                                id = 0,
                                name = domain.getString("name"),
                                validated = domain.getInt("confirmed") == 1,
                                accountId = accountId
                        )
                    }
            val aliases = mutableListOf<AliasData>()
            (0 until length)
                    .map {
                        val json = array.getJSONObject(it)
                        val domain = json.getJSONObject("domain")
                        val domainName = domain.getString("name")
                        val aliasArray = json.getJSONArray("aliases")
                        (0 until aliasArray.length())
                                .map { index ->
                                    val alias = aliasArray.getJSONObject(index)
                                    aliases.add(AliasData(
                                            rowId = alias.getLong("addressId"),
                                            name = alias.getString("name"),
                                            isActive = alias.getInt("status") == 1,
                                            domain = domainName,
                                            accountId = accountId
                                    )
                                    )
                                }

                    }
            return Pair(customDomains.filter { it.name != Contact.mainDomain }, aliases)
        }
    }
}