package com.criptext.mail.scenes.settings.aliases.data

import org.json.JSONArray

data class AliasData(val rowId: Long, val name: String, val domain: String,
                     val isActive: Boolean, val accountId: Long){
    companion object {
        fun fromJSONArray(array: JSONArray, accountId: Long): Pair<List<String>, List<AliasData>> {
            val length = array.length()
            val customDomains = (0 until length)
                    .map {
                        val json = array.getJSONObject(it)
                        json.getString("domain")
                    }
            val aliases = mutableListOf<AliasData>()
            (0 until length)
                    .map {
                        val json = array.getJSONObject(it)
                        val domain = json.getString("domain")
                        val aliasArray = json.getJSONArray("aliases")
                        (0 until aliasArray.length())
                                .map { index ->
                                    val alias = aliasArray.getJSONObject(index)
                                    aliases.add(AliasData(
                                            rowId = alias.getLong("addressId"),
                                            name = alias.getString("name"),
                                            isActive = alias.getInt("status") == 1,
                                            domain = domain,
                                            accountId = accountId
                                    )
                                    )
                                }

                    }
            return Pair(customDomains, aliases)
        }
    }
}